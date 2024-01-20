package builders;

import java.util.Collections;
import java.util.List;

import domain.model.DNSMessage;
import domain.model.DNSSectionAnswer;
import domain.model.DNSSectionHeader;
import domain.model.DNSSectionQuestion;

public class DNSMessageBuilder {

    private DNSMessageBuilder() {}

    public static DNSMessage createQuery(int packetIdentifier, List<DNSSectionQuestion.DNSQuestion> questions) {
        return createQuery(packetIdentifier, 0, questions);
    }

    public static DNSMessage createQuery(int packetIdentifier, int operationCode, List<DNSSectionQuestion.DNSQuestion> questions) {
        DNSSectionHeader header = DNSSectionHeader.builder()
            .packetIdentifier(packetIdentifier)
            .queryOrResponse(DNSSectionHeader.QueryOrResponse.QUERY)
            .recursionDesired(1)
            .operationCode(operationCode)
            .questionCount(questions.size())
            .build();
        DNSSectionQuestion question = new DNSSectionQuestion(questions);
        DNSSectionAnswer answer = new DNSSectionAnswer(Collections.emptyList());
        return new DNSMessage(header, question, answer);
    }

    public static DNSMessage createResponse(int packetIdentifier, List<DNSSectionAnswer.DNSRecord> answers) {
        return createResponseWithQuestion(packetIdentifier, Collections.emptyList(), answers);
    }

    public static DNSMessage createResponseWithQuestion(
        int packetIdentifier,
        List<DNSSectionQuestion.DNSQuestion> questions,
        List<DNSSectionAnswer.DNSRecord> answers
    ) {
        DNSSectionHeader header = DNSSectionHeader.builder()
            .packetIdentifier(packetIdentifier)
            .queryOrResponse(DNSSectionHeader.QueryOrResponse.RESPONSE)
            .recursionDesired(1)
            .questionCount(questions.size())
            .answerCount(answers.size())
            .build();
        DNSSectionQuestion question = new DNSSectionQuestion(questions);
        DNSSectionAnswer answer = new DNSSectionAnswer(answers);
        return new DNSMessage(header, question, answer);
    }
}
