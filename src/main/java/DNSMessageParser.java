import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DNSMessageParser {
    public static DNSMessage decode(byte[] buffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        final int packetIdentifier = byteBuffer.getShort();
        final int questionCount = byteBuffer.getShort(4);
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
        return new DNSMessage(packetIdentifier, questionCount, sb.toString());
    }

    public static byte[] encode(DNSMessage message) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[512]);
        // Packet Identifer
        byteBuffer.putShort((short) message.getPacketIdentifier());
        // Question/Response : Always response
        int byteToStore = (byte) 0b10000000;
        // OPCODE
        byteToStore &= (byte) 0b10000111;
        // Authoritative Answer
        byteToStore &= (byte) 0b11111011;
        // Truncation
        byteToStore &= (byte) 0b11111101;
        // Recursion Desired
        byteToStore &= (byte) 0b11111110;
        byteBuffer.put((byte) byteToStore);
        // Recursion Available
        byteToStore = (byte) 0b01111111;
        // Reserved
        byteToStore &= (byte) 0b10001111;
        // Error
        byteToStore &= (byte) 0b11110000;
        byteBuffer.put((byte) byteToStore);
        // Question Count
        byteBuffer.putShort((short) message.getQuestionCount());
        // Answer Record Count
        byteBuffer.putShort((short) 0);
        // Authority Record Count
        byteBuffer.putShort((short) 0);
        // Additional Record Count
        byteBuffer.putShort((short) 0);
        // Labels
        for (String label : message.getLabels().split("\\.")) {
            // Label Length
            byteBuffer.put((byte) label.length());
            // Label
            byteBuffer.put(label.getBytes());
        }
        byteBuffer.put((byte) 0);
        // Type A (1) / CNAME (5)
        byteBuffer.putShort((short) 1);
        // Class IN(ternet)
        byteBuffer.putShort((short) 1);
        return byteBuffer.array();
    }
}
