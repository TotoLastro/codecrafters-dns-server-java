import java.nio.charset.StandardCharsets;

public class DNSMessageParser {
    public static DNSMessage parse(byte[] buffer) {
        final int packetIdentifier = buffer[0] << 8 + buffer[1];
        final int labelLength = buffer[12];
        final String label = new String(buffer, 13, labelLength, StandardCharsets.UTF_8);
        return new DNSMessage(packetIdentifier, label);
    }
}
