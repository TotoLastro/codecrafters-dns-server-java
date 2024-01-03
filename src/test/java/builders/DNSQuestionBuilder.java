package builders;

import domain.model.DNSMessage.ClassType;
import domain.model.DNSMessage.Type;
import domain.model.DNSSectionQuestion;

public class DNSQuestionBuilder {
    public static DNSSectionQuestion.DNSQuestion createDNSQuestion(String label) {
        return createDNSQuestion(label, Type.A, ClassType.INTERNET);
    }

    public static DNSSectionQuestion.DNSQuestion createDNSQuestion(String label, Type type, ClassType classType) {
        return new DNSSectionQuestion.DNSQuestion(label, type, classType);
    }
}
