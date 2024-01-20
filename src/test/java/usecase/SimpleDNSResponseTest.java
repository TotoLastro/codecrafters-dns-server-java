package usecase;

import java.util.Collections;
import java.util.List;

import domain.model.DNSMessage;
import domain.model.DNSMessageClassType;
import domain.model.DNSMessageType;
import domain.model.DNSSectionAnswer;
import domain.model.DNSSectionHeader;
import domain.model.DNSSectionQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static builders.DNSAnswerBuilder.createDNSRecordWithResource;
import static builders.DNSMessageBuilder.createQuery;
import static builders.DNSQuestionBuilder.createDNSQuestion;
import static org.assertj.core.api.Assertions.assertThat;

class SimpleDNSResponseTest {

    private final SimpleDNSResponse systemUnderTest = new SimpleDNSResponse();

    @Test
    @DisplayName("should return empty response if empty query")
    void shouldReturnEmptyResponseIfEmptyQuery() {
        DNSMessage query = createQuery(0xCAFE, Collections.emptyList());
        DNSMessage actualResponse = systemUnderTest.getResponseMessage(query);
        assertThat(actualResponse).isEqualTo(
            new DNSMessage(
                DNSSectionHeader.builder()
                    .packetIdentifier(0xCAFE)
                    .queryOrResponse(DNSSectionHeader.QueryOrResponse.RESPONSE)
                    .build(),
                new DNSSectionQuestion(Collections.emptyList()),
                new DNSSectionAnswer(Collections.emptyList())
            )
        );
    }

    @Test
    @DisplayName("should return 8.8.8.8 as response for each query")
    void shouldReturnDefaultResponseForEachQuery() {
        List<DNSSectionQuestion.DNSQuestion> dnsQuestions = List.of(
            createDNSQuestion("some.things", DNSMessageType.CNAME, DNSMessageClassType.HESIOD),
            createDNSQuestion("", DNSMessageType.A, DNSMessageClassType.CHAOS)
        );
        DNSMessage query = createQuery(0xBEEF, dnsQuestions);
        DNSMessage actualResponse = systemUnderTest.getResponseMessage(query);
        assertThat(actualResponse).isEqualTo(
            new DNSMessage(
                DNSSectionHeader.builder()
                    .packetIdentifier(0xBEEF)
                    .queryOrResponse(DNSSectionHeader.QueryOrResponse.RESPONSE)
                    .questionCount(2)
                    .answerCount(2)
                    .build(),
                new DNSSectionQuestion(dnsQuestions),
                new DNSSectionAnswer(
                    List.of(
                        createDNSRecordWithResource("some.things", 60, "\u0008\u0008\u0008\u0008"),
                        createDNSRecordWithResource("", 60, "\u0008\u0008\u0008\u0008")
                    )
                )
            )
        );
    }

    @ParameterizedTest
    @DisplayName("should return error NotYetImplemented if query is not a standard query")
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})
    void shouldReturnDefaultResponseForEachQuery(int operationCode) {
        List<DNSSectionQuestion.DNSQuestion> dnsQuestions = List.of(
            createDNSQuestion("some.things", DNSMessageType.CNAME, DNSMessageClassType.HESIOD),
            createDNSQuestion("", DNSMessageType.A, DNSMessageClassType.CHAOS)
        );
        DNSMessage query = createQuery(0xDEAD, operationCode, dnsQuestions);
        DNSMessage actualResponse = systemUnderTest.getResponseMessage(query);
        assertThat(actualResponse).isEqualTo(
            new DNSMessage(
                DNSSectionHeader.builder()
                    .packetIdentifier(query.header().packetIdentifier())
                    .queryOrResponse(DNSSectionHeader.QueryOrResponse.RESPONSE)
                    .operationCode(query.header().operationCode())
                    .error(4)
                    .questionCount(query.header().questionCount())
                    .answerCount(2)
                    .build(),
                new DNSSectionQuestion(dnsQuestions),
                new DNSSectionAnswer(
                    List.of(
                        createDNSRecordWithResource("some.things", 60, "\u0008\u0008\u0008\u0008"),
                        createDNSRecordWithResource("", 60, "\u0008\u0008\u0008\u0008")
                    )
                )
            )
        );
    }
}
