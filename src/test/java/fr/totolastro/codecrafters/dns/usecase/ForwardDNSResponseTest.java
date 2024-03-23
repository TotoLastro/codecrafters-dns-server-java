package fr.totolastro.codecrafters.dns.usecase;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import fr.totolastro.codecrafters.dns.adapter.DNSReceiverGateway;
import fr.totolastro.codecrafters.dns.adapter.DNSRequesterGateway;
import fr.totolastro.codecrafters.dns.builders.DNSAnswerBuilder;
import fr.totolastro.codecrafters.dns.builders.DNSMessageBuilder;
import fr.totolastro.codecrafters.dns.builders.DNSQuestionBuilder;
import fr.totolastro.codecrafters.dns.domain.model.DNSMessage;
import fr.totolastro.codecrafters.dns.domain.model.DNSMessageClassType;
import fr.totolastro.codecrafters.dns.domain.model.DNSMessageType;
import fr.totolastro.codecrafters.dns.domain.model.DNSSectionAnswer;
import fr.totolastro.codecrafters.dns.domain.model.DNSSectionHeader;
import fr.totolastro.codecrafters.dns.domain.model.DNSSectionQuestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ForwardDNSResponseTest {

    private final DNSRequesterGateway dnsRequesterGateway = mock(DNSRequesterGateway.class);
    private final DNSReceiverGateway dnsReceiverGateway = mock(DNSReceiverGateway.class);
    private final ForwardedDNSResponse systemUnderTest =
        new ForwardedDNSResponse(dnsRequesterGateway, dnsReceiverGateway);

    @AfterEach
    void tearDown() {
        clearInvocations(dnsRequesterGateway, dnsReceiverGateway);
    }

    @Test
    void shouldNotCallRequesterOrReceiverIfEmptyQuery() throws IOException {
        DNSMessage query = DNSMessageBuilder.createQuery(0xBABE, Collections.emptyList());
        DNSMessage responseMessage = systemUnderTest.getResponseMessage(query);
        verifyNoInteractions(dnsRequesterGateway, dnsReceiverGateway);
        assertThat(responseMessage).isEqualTo(
            new DNSMessage(
                DNSSectionHeader.builder()
                    .packetIdentifier(0xBABE)
                    .queryOrResponse(DNSSectionHeader.QueryOrResponse.RESPONSE)
                    .recursionDesired(1)
                    .build(),
                new DNSSectionQuestion(Collections.emptyList()),
                new DNSSectionAnswer(Collections.emptyList())
            )
        );
    }

    @Test
    void shouldCallOnceRequesterAndReceiverForOneQuestion() throws IOException {
        when(dnsReceiverGateway.receive())
            .thenReturn(DNSMessageBuilder.createResponse(0x0001, Collections.emptyList()));
        DNSMessage query = DNSMessageBuilder.createQuery(
            0x0101,
            Collections.singletonList(DNSQuestionBuilder.createDNSQuestion("abc.com"))
        );
        systemUnderTest.getResponseMessage(query);
        verify(dnsRequesterGateway).send(query);
        verify(dnsReceiverGateway).receive();
        verifyNoMoreInteractions(dnsRequesterGateway, dnsReceiverGateway);
    }

    @Test
    void shouldReturnEmptyResponseIfReceiverRespondsEmpty() throws IOException {
        when(dnsReceiverGateway.receive())
            .thenReturn(DNSMessageBuilder.createResponse(0xDEAD, Collections.emptyList()));
        DNSMessage query = DNSMessageBuilder.createQuery(
            0xBAFE,
            Collections.singletonList(DNSQuestionBuilder.createDNSQuestion("abc.com"))
        );
        DNSMessage responseMessage = systemUnderTest.getResponseMessage(query);
        verify(dnsRequesterGateway).send(query);
        verify(dnsReceiverGateway).receive();
        assertThat(responseMessage).isEqualTo(
            new DNSMessage(
                DNSSectionHeader.builder()
                    .packetIdentifier(0xBAFE)
                    .queryOrResponse(DNSSectionHeader.QueryOrResponse.RESPONSE)
                    .recursionDesired(1)
                    .questionCount(1)
                    .build(),
                new DNSSectionQuestion(Collections.singletonList(DNSQuestionBuilder.createDNSQuestion("abc.com"))),
                new DNSSectionAnswer(Collections.emptyList())
            )
        );
    }

    @Test
    void shouldReturnAllResponsesGivenByReceiverForOneQuestion() throws IOException {
        List<DNSSectionAnswer.DNSRecord> mockedAnswers = List.of(
            DNSAnswerBuilder.createDNSRecord(
                "abc1.com",
                DNSMessageType.A,
                DNSMessageClassType.INTERNET,
                0,
                "\u0000\u0001\u0002\u0003".getBytes()
            ),
            DNSAnswerBuilder.createDNSRecord(
                "def.com",
                DNSMessageType.CNAME,
                DNSMessageClassType.CHAOS,
                10,
                "\u0005\u0006\u0007\u0008".getBytes()
            )
        );
        when(dnsReceiverGateway.receive())
            .thenReturn(DNSMessageBuilder.createResponse(0xDADA, mockedAnswers));
        DNSMessage query = DNSMessageBuilder.createQuery(
            0xEDAD,
            Collections.singletonList(DNSQuestionBuilder.createDNSQuestion("abc.com"))
        );
        DNSMessage responseMessage = systemUnderTest.getResponseMessage(query);
        assertThat(responseMessage).isEqualTo(
            new DNSMessage(
                DNSSectionHeader.builder()
                    .packetIdentifier(0xEDAD)
                    .queryOrResponse(DNSSectionHeader.QueryOrResponse.RESPONSE)
                    .recursionDesired(1)
                    .questionCount(1)
                    .answerCount(2)
                    .build(),
                query.question(),
                new DNSSectionAnswer(mockedAnswers)
            )
        );
    }

    @Test
    void shouldCallRequesterAndReceiverForEachQuestions() throws IOException {
        when(dnsReceiverGateway.receive())
            .thenReturn(DNSMessageBuilder.createResponse(0x0101, Collections.emptyList()));
        DNSMessage query = DNSMessageBuilder.createQuery(
            0xFEAF,
            List.of(
                DNSQuestionBuilder.createDNSQuestion("aaa.com"),
                DNSQuestionBuilder.createDNSQuestion("bbb.com"),
                DNSQuestionBuilder.createDNSQuestion("ccc.com")
            )
        );
        systemUnderTest.getResponseMessage(query);
        for(DNSSectionQuestion.DNSQuestion question : query.question().questions()) {
            verify(dnsRequesterGateway)
                .send(DNSMessageBuilder.createQuery(0xFEAF, Collections.singletonList(question)));
        }
        verify(dnsReceiverGateway, times(3)).receive();
        verifyNoMoreInteractions(dnsRequesterGateway, dnsReceiverGateway);
    }

    @Test
    void shouldReturnAggregatedResponseGivenByReceiverForEachQuestions() throws IOException {
        List<DNSSectionAnswer.DNSRecord> doubleAnswers = List.of(
            DNSAnswerBuilder.createDNSRecord(
                "abc1.com",
                DNSMessageType.A,
                DNSMessageClassType.INTERNET,
                0,
                "\u0000\u0001\u0002\u0003".getBytes()
            ),
            DNSAnswerBuilder.createDNSRecord(
                "def.com",
                DNSMessageType.CNAME,
                DNSMessageClassType.CHAOS,
                10,
                "\u0005\u0006\u0007\u0008".getBytes()
            )
        );
        List<DNSSectionAnswer.DNSRecord> simpleAnswer = Collections.singletonList(
            DNSAnswerBuilder.createDNSRecord("ghi.com", 100)
        );
        when(dnsReceiverGateway.receive())
            .thenReturn(DNSMessageBuilder.createResponse(0xDADA, doubleAnswers))
            .thenReturn(DNSMessageBuilder.createResponse(0xDEAD, Collections.emptyList()))
            .thenReturn(DNSMessageBuilder.createResponse(0xACE0, simpleAnswer));
        DNSMessage query = DNSMessageBuilder.createQuery(
            0xFEAF,
            List.of(
                DNSQuestionBuilder.createDNSQuestion("aaa.com"),
                DNSQuestionBuilder.createDNSQuestion("bbb.com"),
                DNSQuestionBuilder.createDNSQuestion("ccc.com")
            )
        );
        DNSMessage responseMessage = systemUnderTest.getResponseMessage(query);
        assertThat(responseMessage).isEqualTo(
            new DNSMessage(
                DNSSectionHeader.builder()
                    .packetIdentifier(0xFEAF)
                    .queryOrResponse(DNSSectionHeader.QueryOrResponse.RESPONSE)
                    .recursionDesired(1)
                    .questionCount(3)
                    .answerCount(3)
                    .build(),
                query.question(),
                new DNSSectionAnswer(Stream.concat(doubleAnswers.stream(), simpleAnswer.stream()).toList())
            )
        );
    }
}
