import java.nio.ByteBuffer;

public class DNSMessageEncoder {

    public static byte[] encode(DNSMessage message) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[512]);
        encodeHeaderSection(byteBuffer, message.header());
        encodeQuestionSection(byteBuffer, message.question());
        encodeAnswerSection(byteBuffer, message.answer());
        return byteBuffer.array();
    }

    private static void encodeHeaderSection(ByteBuffer byteBuffer, DNSSectionHeader header) {
        // Packet Identifer
        byteBuffer.putShort((short) header.packetIdentifier());
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
        byteBuffer.putShort((short) header.questionCount());
        // Answer Record Count
        byteBuffer.putShort((short) 0);
        // Authority Record Count
        byteBuffer.putShort((short) 0);
        // Additional Record Count
        byteBuffer.putShort((short) 0);
    }

    private static void encodeQuestionSection(ByteBuffer byteBuffer, DNSSectionQuestion question) {
        encodeLabels(byteBuffer, question.labels());
        // Type A (1) / CNAME (5)
        byteBuffer.putShort((short) question.type().value);
        // Class IN(ternet)
        byteBuffer.putShort((short) 1);
    }

    private static void encodeLabels(ByteBuffer byteBuffer, String question) {
        // Labels
        for (String label : question.split("\\.")) {
            // Label Length
            byteBuffer.put((byte) label.length());
            // Label
            byteBuffer.put(label.getBytes());
        }
        byteBuffer.put((byte) 0);
    }

    private static void encodeAnswerSection(ByteBuffer byteBuffer, DNSSectionAnswer answer) {
        // Answer Record Count
        byteBuffer.putShort((short) answer.records().size());
        for (DNSSectionAnswer.DNSRecord record : answer.records()) {
            // Labels
            encodeLabels(byteBuffer, record.name());
            // Type A (1) / CNAME (5)
            byteBuffer.putShort((short) record.type().value);
            // Class IN(ternet)
            byteBuffer.putShort((short) 1);
            // TTL
            byteBuffer.putInt(record.ttl());
            // Data Length
            byteBuffer.putShort((short) record.data().length);
            // Data
            byteBuffer.put(record.data());
        }
    }
}