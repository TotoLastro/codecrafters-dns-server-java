package fr.totolastro.codecrafters.dns.domain.parsers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import fr.totolastro.codecrafters.dns.domain.model.DNSMessage;
import fr.totolastro.codecrafters.dns.domain.model.DNSMessageClassType;
import fr.totolastro.codecrafters.dns.domain.model.DNSMessageType;
import fr.totolastro.codecrafters.dns.domain.model.DNSSectionAnswer;
import fr.totolastro.codecrafters.dns.domain.model.DNSSectionHeader;
import fr.totolastro.codecrafters.dns.domain.model.DNSSectionQuestion;

public class DNSMessageDecoder {

    private DNSMessageDecoder() {}

    public static DNSMessage decode(byte[] buffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        DNSSectionHeader header = decodeHeader(byteBuffer);
        DNSSectionQuestion question = decodeQuestion(byteBuffer, header.questionCount());
        DNSSectionAnswer answer = decodeAnswer(byteBuffer, header.answerCount());
        return new DNSMessage(header, question, answer);
    }

    private static DNSSectionHeader decodeHeader(ByteBuffer byteBuffer) {
        final int packetIdentifier = byteBuffer.getShort() & 0xFFFF;
        byte firstBitMask = byteBuffer.get();
        final int questionOrReponse = (firstBitMask >> 7) & 1;
        final int operationCode = (firstBitMask >> 3) & 0b1111;
        final int authoritativeAnswer = (firstBitMask >> 2) & 1;
        final int truncation = (firstBitMask >> 1) & 1;
        final int recursionDesired = firstBitMask & 1;
        byte secondBitMask = byteBuffer.get();
        final int recursionAvailable = (secondBitMask >> 7) & 1;
        final int reserved = (secondBitMask >> 4) & 0b111;
        final int error = secondBitMask & 0b1111;
        final int questionCount = byteBuffer.getShort();
        final int answerCount = byteBuffer.getShort();
        final int nameserverCount = byteBuffer.getShort();
        final int additionalRecordCount = byteBuffer.getShort();
        return new DNSSectionHeader(
            packetIdentifier,
            DNSSectionHeader.QueryOrResponse.fromValue(questionOrReponse).orElseThrow(),
            operationCode,
            authoritativeAnswer,
            truncation,
            recursionDesired,
            recursionAvailable,
            reserved,
            error,
            questionCount,
            answerCount,
            nameserverCount,
            additionalRecordCount
        );
    }

    private static DNSSectionQuestion decodeQuestion(ByteBuffer byteBuffer, int numberOfQuestions) {
        return new DNSSectionQuestion(
            IntStream.range(0, numberOfQuestions)
                .mapToObj(_ -> {
                    String labels = decodeLabels(byteBuffer);
                    final int queryType = byteBuffer.getShort();
                    final int queryClass = byteBuffer.getShort();
                    return new DNSSectionQuestion.DNSQuestion(
                        labels,
                        DNSMessageType.fromValue(queryType).orElseThrow(),
                        DNSMessageClassType.fromValue(queryClass).orElseThrow()
                    );
                }).toList()
        );
    }

    private static String decodeLabels(ByteBuffer byteBuffer) {
        List<String> labels = new ArrayList<>();
        int labelLength;
        do {
            labelLength = byteBuffer.get() & 0b11111111;
            if ((labelLength >> 6) == 0b11) {
                labels.add(decodeCompressedLabel(byteBuffer, labelLength));
            } else if (0 < labelLength) {
                labels.add(decodeRawLabel(byteBuffer, labelLength));
            }
        } while (0 < labelLength && (labelLength >> 6) != 0b11);

        return String.join(".", labels);
    }

    private static String decodeRawLabel(ByteBuffer byteBuffer, int labelLength) {
        String label = new String(byteBuffer.array(), byteBuffer.position(), labelLength, StandardCharsets.UTF_8);
        byteBuffer.position(byteBuffer.position() + label.length());
        return label;
    }

    private static String decodeCompressedLabel(ByteBuffer byteBuffer, int labelLength) {
        int position = ((labelLength & 0b00111111) << 8) | (byteBuffer.get() & 0b11111111);
        return decodeLabels(byteBuffer.duplicate().position(position));
    }

    private static DNSSectionAnswer decodeAnswer(ByteBuffer byteBuffer, int numberOfAnswers) {
        List<DNSSectionAnswer.DNSRecord> records = IntStream.range(0, numberOfAnswers)
            .mapToObj(_ -> {
                String labels = decodeLabels(byteBuffer);
                int queryType = byteBuffer.getShort();
                int queryClass = byteBuffer.getShort();
                int ttl = byteBuffer.getInt();
                int dataLength = byteBuffer.getShort();
                byte[] data = new byte[dataLength];
                byteBuffer.get(data);
                return new DNSSectionAnswer.DNSRecord(
                    labels,
                    DNSMessageType.fromValue(queryType).orElseThrow(),
                    DNSMessageClassType.fromValue(queryClass).orElseThrow(),
                    ttl,
                    data
                );
            })
            .toList();
        return new DNSSectionAnswer(records);
    }
}
