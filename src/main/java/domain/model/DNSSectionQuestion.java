package domain.model;

import java.util.List;

public record DNSSectionQuestion(List<DNSQuestion> questions) {
    public record DNSQuestion(String labels, DNSMessage.Type type, DNSMessage.ClassType classType) {}
}
