import java.nio.charset.StandardCharsets;

public class DNSMessage {

    private final byte[] buffer = new byte[512];

    public DNSMessage(int packetIdentifer, String label) {
        // Packet Identifer
        buffer[0] = (byte) (packetIdentifer & 0xFF);
        buffer[1] = (byte) ((packetIdentifer >> 8) & 0xFF);
        // Question/Response : Always response
        buffer[2] |= (byte) 0b10000000;
        // OPCODE
        buffer[2] &= (byte) 0b10000111;
        // Authoritative Answer
        buffer[2] &= (byte) 0b11111011;
        // Truncation
        buffer[2] &= (byte) 0b11111101;
        // Recursion Desired
        buffer[2] &= (byte) 0b11111110;
        // Recursion Available
        buffer[3] &= (byte) 0b01111111;
        // Reserved
        buffer[3] &= (byte) 0b10001111;
        // Error
        buffer[3] &= (byte) 0b11110000;
        // Question Count
        buffer[4] = 0;
        buffer[5] = 0;
        // Answer Record Count
        buffer[6] = 0;
        buffer[7] = 0;
        // Authority Record Count
        buffer[8] = 0;
        buffer[9] = 0;
        // Additional Record Count
        buffer[10] = 0;
        buffer[11] = 0;
        // Label Length
        buffer[12] = (byte) label.length();
        // Label
        for (int i = 0; i < label.length(); i++) {
            buffer[13 + i] = (byte) label.charAt(i);
        }
        buffer[13 + label.length()] = 0;
        // Type A (1) / CNAME (5)
        buffer[14 + label.length()] = 0;
        buffer[15 + label.length()] = 1;
        // Class IN(ternet)
        buffer[16 + label.length()] = 0;
        buffer[17 + label.length()] = 1;
    }

    public int getPacketIdentifier() {
        return buffer[0] << 8 + buffer[1];
    }

    public String getLabel() {
        return new String(buffer, 13, buffer[12], StandardCharsets.UTF_8);
    }

    public byte[] getBuffer() {
        return buffer;
    }
}
