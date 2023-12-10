import java.util.List;

public record DNSSectionAnswer(List<DNSRecord> records) {
    public record DNSRecord(DNSMessage.Type type, String name, int ttl, byte[] data) {}
}
