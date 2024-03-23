package fr.totolastro.codecrafters.dns.builders;

import fr.totolastro.codecrafters.dns.domain.model.DNSMessageClassType;
import fr.totolastro.codecrafters.dns.domain.model.DNSMessageType;
import fr.totolastro.codecrafters.dns.domain.model.DNSSectionAnswer;

public class DNSAnswerBuilder {
    public static DNSSectionAnswer.DNSRecord createDNSRecord(String label, int ttl) {
        return createDNSRecordWithResource(label, ttl, "");
    }

    public static DNSSectionAnswer.DNSRecord createDNSRecordWithResource(String label, int ttl, String resource) {
        return createDNSRecord(label, DNSMessageType.A, DNSMessageClassType.INTERNET, ttl, resource.getBytes());
    }

    public static DNSSectionAnswer.DNSRecord createDNSRecord(
        String label,
        DNSMessageType type,
        DNSMessageClassType classType,
        int ttl,
        byte[] data
    ) {
        return new DNSSectionAnswer.DNSRecord(label, type, classType, ttl, data);
    }
}
