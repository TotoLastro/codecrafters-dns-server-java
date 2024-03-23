package fr.totolastro.codecrafters.dns.domain.model;

import java.util.List;

public record DNSSectionQuestion(List<DNSQuestion> questions) {
    public record DNSQuestion(String labels, DNSMessageType type, DNSMessageClassType classType) {}
}
