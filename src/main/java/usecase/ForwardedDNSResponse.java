package usecase;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import domain.model.DNSMessage;
import domain.model.DNSSectionAnswer;
import domain.model.DNSSectionHeader;
import domain.model.DNSSectionQuestion;
import domain.parsers.DNSMessageDecoder;
import domain.parsers.DNSMessageEncoder;

public class ForwardedDNSResponse implements DNSResponseRetriever {

    private final DatagramSocket serverSocket;
    private final InetSocketAddress forwardAddress;

    public ForwardedDNSResponse(DatagramSocket serverSocket, InetSocketAddress forwardAddress) {
        this.serverSocket = serverSocket;
        this.forwardAddress = forwardAddress;
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
        System.out.println("Forward(" + forwardAddress + ") : " + message);
        final byte[] queryBuffer = DNSMessageEncoder.encode(message);
        DatagramPacket packet = new DatagramPacket(queryBuffer, queryBuffer.length, forwardAddress);
        serverSocket.send(packet);

        final byte[] responseBuffer = new byte[512];
        final DatagramPacket responseFromForward = new DatagramPacket(responseBuffer, responseBuffer.length);
        serverSocket.receive(responseFromForward);
        DNSMessage responseMessage = DNSMessageDecoder.decode(responseFromForward.getData());
        System.out.println("Receive(" + responseFromForward.getSocketAddress() + ") : " + responseMessage);
        return responseMessage;
    }

    private DNSSectionHeader cloneForOneQuestion(DNSSectionHeader header) {
        return new DNSSectionHeader(
            new Random().nextInt(1, Short.MAX_VALUE * 2),
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
