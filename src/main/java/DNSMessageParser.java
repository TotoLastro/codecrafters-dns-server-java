import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DNSMessageParser {
    public static DNSMessage parse(byte[] buffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        final int packetIdentifier = byteBuffer.getShort();
        byteBuffer.position(12);
        int labelLength = byteBuffer.get();
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(new String(buffer, byteBuffer.position(), labelLength, StandardCharsets.UTF_8));
            byteBuffer.position(byteBuffer.position() + labelLength);
            labelLength = byteBuffer.get();
            if (0 < labelLength) {
                sb.append(".");
            }
        } while (0 < labelLength);
        return new DNSMessage(packetIdentifier, sb.toString());
    }
}
