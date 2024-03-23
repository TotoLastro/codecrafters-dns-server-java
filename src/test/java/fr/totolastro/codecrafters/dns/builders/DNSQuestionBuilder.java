package fr.totolastro.codecrafters.dns.builders;

import fr.totolastro.codecrafters.dns.domain.model.DNSMessageClassType;
import fr.totolastro.codecrafters.dns.domain.model.DNSMessageType;
import fr.totolastro.codecrafters.dns.domain.model.DNSSectionQuestion;

public class DNSQuestionBuilder {
    public static DNSSectionQuestion.DNSQuestion createDNSQuestion(String label) {
        return createDNSQuestion(label, DNSMessageType.A, DNSMessageClassType.INTERNET);
    }

    public static DNSSectionQuestion.DNSQuestion createDNSQuestion(String label, DNSMessageType type, DNSMessageClassType classType) {
        return new DNSSectionQuestion.DNSQuestion(label, type, classType);
    }
}
