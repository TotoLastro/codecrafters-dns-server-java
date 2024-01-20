package builders;

import domain.model.DNSMessageClassType;
import domain.model.DNSMessageType;
import domain.model.DNSSectionQuestion;

public class DNSQuestionBuilder {
    public static DNSSectionQuestion.DNSQuestion createDNSQuestion(String label) {
        return createDNSQuestion(label, DNSMessageType.A, DNSMessageClassType.INTERNET);
    }

    public static DNSSectionQuestion.DNSQuestion createDNSQuestion(String label, DNSMessageType type, DNSMessageClassType classType) {
        return new DNSSectionQuestion.DNSQuestion(label, type, classType);
    }
}
