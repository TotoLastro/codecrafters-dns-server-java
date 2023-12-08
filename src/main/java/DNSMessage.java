
public class DNSMessage {

    private final byte[] buffer = new byte[512];
    private final int packetIdentifier;
    private final String labels;

    public DNSMessage(int packetIdentifer, String labels) {
        this.packetIdentifier = packetIdentifer;
        this.labels = labels;
        fillBuffer();
    }

    private void fillBuffer() {
        int i = 0;
        // Packet Identifer
        buffer[i++] = (byte) ((packetIdentifier >> 8) & 0xFF);
        buffer[i++] = (byte) (packetIdentifier & 0xFF);
        // Question/Response : Always response
        buffer[i] |= (byte) 0b10000000;
        // OPCODE
        buffer[i] &= (byte) 0b10000111;
        // Authoritative Answer
        buffer[i] &= (byte) 0b11111011;
        // Truncation
        buffer[i] &= (byte) 0b11111101;
        // Recursion Desired
        buffer[i++] &= (byte) 0b11111110;
        // Recursion Available
        buffer[i] &= (byte) 0b01111111;
        // Reserved
        buffer[i] &= (byte) 0b10001111;
        // Error
        buffer[i++] &= (byte) 0b11110000;
        // Question Count
        buffer[i++] = 0;
        buffer[i++] = 0;
        // Answer Record Count
        buffer[i++] = 0;
        buffer[i++] = 0;
        // Authority Record Count
        buffer[i++] = 0;
        buffer[i++] = 0;
        // Additional Record Count
        buffer[i++] = 0;
        buffer[i++] = 0;
        // Labels
        for (String label : labels.split("\\.")) {
            // Label Length
            buffer[i++] = (byte) label.length();
            // Label
            for (int j = 0; j < label.length(); j++) {
                buffer[i++] = (byte) label.charAt(j);
            }
        }
        buffer[i++] = 0;
        // Type A (1) / CNAME (5)
        buffer[i++] = 0;
        buffer[i++] = 1;
        // Class IN(ternet)
        buffer[i++] = 0;
        buffer[i] = 1;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public String getLabel() {
        return labels;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public String toString() {
        return DNSMessage.class.getSimpleName() + "(" +
            "ID=" + getPacketIdentifier() + "," +
            "label=" + getLabel() + ")";
    }
}
