package builders;

import domain.model.DNSMessage.ClassType;
import domain.model.DNSMessage.Type;
import domain.model.DNSSectionAnswer;

public class DNSAnswerBuilder {
    public static DNSSectionAnswer.DNSRecord createDNSRecord(String label, int ttl) {
        return createDNSRecordWithResource(label, ttl, "");
    }

    public static DNSSectionAnswer.DNSRecord createDNSRecordWithResource(String label, int ttl, String resource) {
        return createDNSRecord(label, Type.A, ClassType.INTERNET, ttl, resource.getBytes());
    }

    public static DNSSectionAnswer.DNSRecord createDNSRecord(String label, Type type, ClassType classType, int ttl, byte[] data) {
        return new DNSSectionAnswer.DNSRecord(label, type, classType, ttl, data);
    }
}
