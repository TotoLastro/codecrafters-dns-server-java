package fr.totolastro.codecrafters.dns.usecase;

import fr.totolastro.codecrafters.dns.domain.model.DNSMessage;
import fr.totolastro.codecrafters.dns.domain.model.DNSMessageClassType;
import fr.totolastro.codecrafters.dns.domain.model.DNSMessageType;
import fr.totolastro.codecrafters.dns.domain.model.DNSSectionAnswer;
import fr.totolastro.codecrafters.dns.domain.model.DNSSectionHeader;

public class SimpleDNSResponse implements DNSResponseRetriever {

    public static final int NO_ERROR = 0;
    public static final int NOT_IMPLEMENTED = 4;

    @Override
    public DNSMessage getResponseMessage(DNSMessage questionMessage) {
        DNSSectionAnswer responseAnswer = new DNSSectionAnswer(
            questionMessage.question().questions().stream()
                .map(question -> new DNSSectionAnswer.DNSRecord(
                    question.labels(),
                    DNSMessageType.A,
                    DNSMessageClassType.INTERNET,
                    60,
                    new byte[]{8, 8, 8, 8}
                ))
                .toList()
        );
        DNSSectionHeader responseHeader = new DNSSectionHeader(
            questionMessage.header().packetIdentifier(),
            DNSSectionHeader.QueryOrResponse.RESPONSE,
            questionMessage.header().operationCode(),
            0,
            0,
            0,
            0,
            0,
            questionMessage.isStandardQuery() ? NO_ERROR : NOT_IMPLEMENTED,
            questionMessage.header().questionCount(),
            responseAnswer.records().size(),
            0,
            0
        );
        return new DNSMessage(responseHeader, questionMessage.question(), responseAnswer);
    }
}
