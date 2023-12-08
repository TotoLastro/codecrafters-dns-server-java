import java.nio.charset.StandardCharsets;

public class DNSMessageParser {
    public static DNSMessage parse(byte[] buffer) {
        final int packetIdentifier = buffer[0] << 8 + buffer[1];
        int i = 12;
        int labelLength = buffer[i++];
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(new String(buffer, i, labelLength, StandardCharsets.UTF_8));
            i += labelLength;
            labelLength = buffer[i++];
            if (0 < labelLength) {
                sb.append(".");
            }
        } while (0 < labelLength);
        return new DNSMessage(packetIdentifier, sb.toString());
    }
}
