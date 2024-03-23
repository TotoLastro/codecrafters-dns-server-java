package fr.totolastro.codecrafters.dns.domain.model;

public record DNSMessage(
    DNSSectionHeader header,
    DNSSectionQuestion question,
    DNSSectionAnswer answer
) {
    public boolean isStandardQuery() {
        return header().operationCode() == 0;
    }
}
