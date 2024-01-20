package usecase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import adapter.DNSReceiverGateway;
import adapter.DNSRequesterGateway;
import domain.model.DNSMessage;
import domain.model.DNSSectionAnswer;
import domain.model.DNSSectionHeader;
import domain.model.DNSSectionQuestion;

public class ForwardedDNSResponse implements DNSResponseRetriever {

    private final DNSRequesterGateway dnsRequesterGateway;
    private final DNSReceiverGateway dnsReceiverGateway;

    public ForwardedDNSResponse(DNSRequesterGateway dnsRequesterGateway, DNSReceiverGateway dnsReceiverGateway) {
        this.dnsRequesterGateway = dnsRequesterGateway;
        this.dnsReceiverGateway = dnsReceiverGateway;
    }

    @Override
    public DNSMessage getResponseMessage(DNSMessage questionMessage) throws IOException {
        List<DNSMessage> responses = new ArrayList<>();
        for (DNSSectionQuestion.DNSQuestion question : questionMessage.question().questions()) {
            DNSMessage responseMessage = getResponseForQuestion(questionMessage.header(), question);
            responses.add(responseMessage);
        }

        List<DNSSectionAnswer.DNSRecord> answers = responses.stream()
            .flatMap(r -> r.answer().records().stream())
            .collect(Collectors.toList());
        return new DNSMessage(
            cloneWithSpecifiedAnswers(questionMessage.header(), responses),
            questionMessage.question(),
            new DNSSectionAnswer(answers)
        );
    }

    private DNSMessage getResponseForQuestion(DNSSectionHeader headerQuestion, DNSSectionQuestion.DNSQuestion question) throws IOException {
        DNSMessage message = new DNSMessage(
            cloneForOneQuestion(headerQuestion),
            new DNSSectionQuestion(Collections.singletonList(question)),
            new DNSSectionAnswer(Collections.emptyList())
        );
        dnsRequesterGateway.send(message);

        return dnsReceiverGateway.receive();
    }

    private DNSSectionHeader cloneForOneQuestion(DNSSectionHeader header) {
        return new DNSSectionHeader(
            header.packetIdentifier(),
            DNSSectionHeader.QueryOrResponse.QUERY,
            header.operationCode(),
            header.authoritativeAnswer(),
            header.truncation(),
            header.recursionDesired(),
            header.recursionAvailable(),
            header.reserved(),
            header.error(),
            1,
            0,
            0,
            0
        );
    }

    private DNSSectionHeader cloneWithSpecifiedAnswers(DNSSectionHeader header, List<DNSMessage> answers) {
        List<DNSSectionHeader> responseHeaders = answers.stream().map(DNSMessage::header).toList();
        return new DNSSectionHeader(
            header.packetIdentifier(),
            DNSSectionHeader.QueryOrResponse.RESPONSE,
            header.operationCode(),
            responseHeaders.stream().map(DNSSectionHeader::authoritativeAnswer).findFirst().orElse(header.authoritativeAnswer()),
            responseHeaders.stream().map(DNSSectionHeader::truncation).findFirst().orElse(header.truncation()),
            header.recursionDesired(),
            responseHeaders.stream().map(DNSSectionHeader::recursionAvailable).findFirst().orElse(header.recursionAvailable()),
            responseHeaders.stream().map(DNSSectionHeader::reserved).findFirst().orElse(header.reserved()),
            responseHeaders.stream().map(DNSSectionHeader::error).findFirst().orElse(header.error()),
            header.questionCount(),
            (int) answers.stream().mapToLong(m -> m.answer().records().size()).sum(),
            header.nameserverCount(),
            header.additionalRecordCount()
        );
    }
}
