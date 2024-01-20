package parsers;

import java.util.Collections;
import java.util.List;

import builders.DNSMessageBuilder;
import domain.model.DNSMessage;
import domain.model.DNSMessageClassType;
import domain.model.DNSMessageType;
import domain.parsers.DNSMessageEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static builders.DNSAnswerBuilder.createDNSRecord;
import static builders.DNSAnswerBuilder.createDNSRecordWithResource;
import static builders.DNSMessageBuilder.createQuery;
import static builders.DNSMessageBuilder.createResponse;
import static builders.DNSMessageBuilder.createResponseWithQuestion;
import static builders.DNSQuestionBuilder.createDNSQuestion;
import static org.assertj.core.api.Assertions.assertThat;

class DNSMessageEncoderTest {

    @Test
    @DisplayName("should encode a query without question neither answer")
    void shouldEncodeDNSMessageWithoutQuestionNeitherAnswer() {
        DNSMessage dnsMessage = DNSMessageBuilder.createQuery(0x85AB, Collections.emptyList());

        byte[] actualEncodedMessage = DNSMessageEncoder.encode(dnsMessage);

        assertThat(actualEncodedMessage).isEqualTo(
            new byte[]{
                // Packet Identifer
                (byte) 0x85, (byte) 0xAB,
                // Question / Standard query / Not truncated / Recursion Desired
                (byte) 0x01, (byte) 0x00,
                // Zero question
                (byte) 0x00, (byte) 0x00,
                // Zero answer
                (byte) 0x00, (byte) 0x00,
                // Zero Authority RR
                (byte) 0x00, (byte) 0x00,
                // Zero Additional RR
                (byte) 0x00, (byte) 0x00,
            }
        );
    }

    @Test
    @DisplayName("should encode a query with 2 questions and 0 answer")
    void shouldEncodeDNSMessageWithTwoQuestionsAndZeroAnswer() {
        DNSMessage dnsMessage = createQuery(
            0xFFFF,
            List.of(createDNSQuestion("abcd.com"), createDNSQuestion("sub.abcd.com"))
        );

        byte[] actualEncodedMessage = DNSMessageEncoder.encode(dnsMessage);

        assertThat(actualEncodedMessage).isEqualTo(
            new byte[]{
                // Packet Identifer
                (byte) 0xFF, (byte) 0xFF,
                // Question / Standard query / Not truncated / Recursion Desired
                (byte) 0x01, (byte) 0x00,
                // Two questions
                (byte) 0x00, (byte) 0x02,
                // Zero answer
                (byte) 0x00, (byte) 0x00,
                // Zero Authority RR
                (byte) 0x00, (byte) 0x00,
                // Zero Additional RR
                (byte) 0x00, (byte) 0x00,
                // Label1: abcd.com
                (byte) 0x04, (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
                (byte) 0x03, (byte) 'c', (byte) 'o', (byte) 'm',
                (byte) 0x00,
                // Type A (1) / CNAME (5)
                (byte) 0x00, (byte) 0x01,
                // Class IN(ternet)
                (byte) 0x00, (byte) 0x01,
                // Label2: sub.abcd.com
                (byte) 0x03, (byte) 's', (byte) 'u', (byte) 'b',
                (byte) 0x04, (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
                (byte) 0x03, (byte) 'c', (byte) 'o', (byte) 'm',
                (byte) 0x00,
                // Type A (1) / CNAME (5)
                (byte) 0x00, (byte) 0x01,
                // Class IN(ternet)
                (byte) 0x00, (byte) 0x01
            }
        );
    }

    @Test
    @DisplayName("should encode a response with 0 question and 1 answer")
    void shouldEncodeDNSMessageWithZeroQuestionAndOneAnswer() {
        DNSMessage dnsMessage = createResponse(
            0x850B,
            List.of(createDNSRecord("abcd.com", 0x12345678))
        );

        byte[] actualEncodedMessage = DNSMessageEncoder.encode(dnsMessage);

        assertThat(actualEncodedMessage).isEqualTo(
            new byte[]{
                // Packet Identifer
                (byte) 0x85, (byte) 0x0B,
                // Answer / Standard query / Not truncated / Recursion Desired
                (byte) 0x81, (byte) 0x00,
                // Zero question
                (byte) 0x00, (byte) 0x00,
                // One answer
                (byte) 0x00, (byte) 0x01,
                // Zero Authority RR
                (byte) 0x00, (byte) 0x00,
                // Zero Additional RR
                (byte) 0x00, (byte) 0x00,
                // Label: abcd.com
                (byte) 0x04, (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
                (byte) 0x03, (byte) 'c', (byte) 'o', (byte) 'm',
                (byte) 0x00,
                // Type A (1) / CNAME (5)
                (byte) 0x00, (byte) 0x01,
                // Class IN(ternet)
                (byte) 0x00, (byte) 0x01,
                // TTL
                (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
                // RData length
                (byte) 0x00, (byte) 0x00,
            }
        );
    }

    @Test
    @DisplayName("should encode a response with 1 question and 2 answers")
    void shouldEncodeDNSMessageWithOneQuestionAndTwoAnswers() {
        DNSMessage dnsMessage = createResponseWithQuestion(
            0xF5FB,
            List.of(createDNSQuestion("abcd.com")),
            List.of(
                createDNSRecordWithResource("abcd.com", 0x7FFFFFFF, "Hello"),
                createDNSRecord("sub.abcd.com", DNSMessageType.CNAME, DNSMessageClassType.INTERNET, 0xF, "World!".getBytes())
            )
        );

        byte[] actualEncodedMessage = DNSMessageEncoder.encode(dnsMessage);

        assertThat(actualEncodedMessage).isEqualTo(
            new byte[]{
                // Packet Identifer
                (byte) 0xF5, (byte) 0xFB,
                // Answer / Standard query / Not truncated / Recursion Desired
                (byte) 0x81, (byte) 0x00,
                // One question
                (byte) 0x00, (byte) 0x01,
                // Two answers
                (byte) 0x00, (byte) 0x02,
                // Zero Authority RR
                (byte) 0x00, (byte) 0x00,
                // Zero Additional RR
                (byte) 0x00, (byte) 0x00,
                // Label: abcd.com
                (byte) 0x04, (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
                (byte) 0x03, (byte) 'c', (byte) 'o', (byte) 'm',
                (byte) 0x00,
                // Type A (1) / CNAME (5)
                (byte) 0x00, (byte) 0x01,
                // Class IN(ternet)
                (byte) 0x00, (byte) 0x01,
                // Label: abcd.com
                (byte) 0x04, (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
                (byte) 0x03, (byte) 'c', (byte) 'o', (byte) 'm',
                (byte) 0x00,
                // Type A (1) / CNAME (5)
                (byte) 0x00, (byte) 0x01,
                // Class IN(ternet)
                (byte) 0x00, (byte) 0x01,
                // TTL
                (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                // RData length
                (byte) 0x00, (byte) 0x05,
                // RData
                (byte) 'H', (byte) 'e', (byte) 'l', (byte) 'l', (byte) 'o',
                // Label: sub.abcd.com
                (byte) 0x03, (byte) 's', (byte) 'u', (byte) 'b',
                (byte) 0x04, (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
                (byte) 0x03, (byte) 'c', (byte) 'o', (byte) 'm',
                (byte) 0x00,
                // Type A (1) / CNAME (5)
                (byte) 0x00, (byte) 0x05,
                // Class IN(ternet)
                (byte) 0x00, (byte) 0x01,
                // TTL
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0F,
                // RData length
                (byte) 0x00, (byte) 0x06,
                // RData
                (byte) 'W', (byte) 'o', (byte) 'r', (byte) 'l', (byte) 'd', (byte) '!'
            }
        );
    }
}
