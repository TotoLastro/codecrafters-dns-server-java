import java.nio.ByteBuffer;

public class DNSMessageEncoder {

    public static byte[] encode(DNSMessage message) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[512]);
        encodeHeaderSection(byteBuffer, message.header());
        encodeQuestionSection(byteBuffer, message.question());
        encodeAnswerSection(byteBuffer, message.answer());
        byte[] result = new byte[byteBuffer.position()];
        byteBuffer.rewind().get(result);
        return result;
    }

    private static void encodeHeaderSection(ByteBuffer byteBuffer, DNSSectionHeader header) {
        byteBuffer.putShort((short) header.packetIdentifier());
        int byteToStore = header.queryOrResponse().value << 7;
        byteToStore |= (header.operationCode() << 3);
        byteToStore |= (header.authoritativeAnswer() << 2);
        byteToStore |= (header.truncation() << 1);
        byteToStore |= header.recursionDesired();
        byteBuffer.put((byte) byteToStore);
        byteToStore = header.recursionAvailable() << 7;
        byteToStore |= (header.reserved() << 4);
        byteToStore |= header.error();
        byteBuffer.put((byte) byteToStore);
        byteBuffer.putShort((short) header.questionCount());
        byteBuffer.putShort((short) header.answerCount());
        byteBuffer.putShort((short) header.nameserverCount());
        byteBuffer.putShort((short) header.additionalRecordCount());
    }

    private static void encodeQuestionSection(ByteBuffer byteBuffer, DNSSectionQuestion questionSection) {
        for (DNSSectionQuestion.DNSQuestion question : questionSection.questions()) {
            encodeLabels(byteBuffer, question.labels());
            byteBuffer.putShort((short) question.type().value);
            byteBuffer.putShort((short) question.classType().value);
        }
    }

    private static void encodeLabels(ByteBuffer byteBuffer, String question) {
        for (String label : question.split("\\.")) {
            byteBuffer.put((byte) label.length());
            byteBuffer.put(label.getBytes());
        }
        byteBuffer.put((byte) 0);
    }

    private static void encodeAnswerSection(ByteBuffer byteBuffer, DNSSectionAnswer answer) {
        for (DNSSectionAnswer.DNSRecord record : answer.records()) {
            encodeLabels(byteBuffer, record.name());
            byteBuffer.putShort((short) record.dataType().value);
            byteBuffer.putShort((short) record.dataClass().value);
            byteBuffer.putInt(record.ttl());
            byteBuffer.putShort((short) record.data().length);
            byteBuffer.put(record.data());
        }
    }
}
