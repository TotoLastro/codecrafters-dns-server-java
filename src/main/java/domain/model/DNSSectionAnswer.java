package domain.model;

import java.util.List;

public record DNSSectionAnswer(List<DNSRecord> records) {
    public record DNSRecord(String name, DNSMessage.Type dataType, DNSMessage.ClassType dataClass, int ttl, byte[] data) {}
}
