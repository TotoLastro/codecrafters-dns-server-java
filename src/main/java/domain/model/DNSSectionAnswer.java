package domain.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record DNSSectionAnswer(List<DNSRecord> records) {
    public record DNSRecord(String name, DNSMessageType dataType, DNSMessageClassType dataClass, int ttl, byte[] data) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DNSRecord dnsRecord = (DNSRecord) o;
            return ttl == dnsRecord.ttl
                && Objects.equals(name, dnsRecord.name)
                && dataType == dnsRecord.dataType
                && dataClass == dnsRecord.dataClass
                && Arrays.equals(data, dnsRecord.data);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(name, dataType, dataClass, ttl);
            result = 31 * result + Arrays.hashCode(data);
            return result;
        }

        @Override
        public String toString() {
            return STR."""
            DNSRecord{
                name='\{name}',
                dataType=\{dataType},
                dataClass=\{dataClass},
                ttl=\{ttl},
                data=\{Arrays.toString(data)}
            }""";
        }
    }
}
