import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DNSMessageDecoder {

    private static final int INDEX_SECTION_HEADER = 0;
    private static final int INDEX_SECTION_QUESTION = 12;

    public static DNSMessage decode(byte[] buffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        DNSSectionHeader header = decodeHeader(byteBuffer);
        DNSSectionQuestion question = decodeQuestion(byteBuffer);
        DNSSectionAnswer answer = decodeAnswer(byteBuffer, question.labels());
        return new DNSMessage(header, question, answer);
    }

    private static DNSSectionHeader decodeHeader(ByteBuffer byteBuffer) {
        byteBuffer.position(INDEX_SECTION_HEADER);
        final int packetIdentifier = byteBuffer.getShort();
        final int questionCount = byteBuffer.getShort(4);
        final int answerCount = byteBuffer.getShort(6);
        return new DNSSectionHeader(packetIdentifier, questionCount, answerCount);
    }

    private static DNSSectionQuestion decodeQuestion(ByteBuffer byteBuffer) {
        byteBuffer.position(INDEX_SECTION_QUESTION);
        String labels = decodeLabels(byteBuffer);
        final int queryType = byteBuffer.getShort();
        return new DNSSectionQuestion(labels, DNSMessage.Type.fromValue(queryType).orElseThrow());
    }

    private static String decodeLabels(ByteBuffer byteBuffer) {
        StringBuilder labelBuilder = new StringBuilder();
        int labelLength = byteBuffer.get();
        while (0 < labelLength) {
            labelBuilder.append(new String(byteBuffer.array(), byteBuffer.position(), labelLength, StandardCharsets.UTF_8));
            byteBuffer.position(byteBuffer.position() + labelLength);
            labelLength = byteBuffer.get();
            if (0 < labelLength) {
                labelBuilder.append(".");
            }
        }
        return labelBuilder.toString();
    }

    private static DNSSectionAnswer decodeAnswer(ByteBuffer byteBuffer, String questionLabels) {
        byteBuffer.position(INDEX_SECTION_QUESTION + questionLabels.length() + 6);
        final int answerCount = byteBuffer.getShort();
        List<DNSSectionAnswer.DNSRecord> records = IntStream.range(0, answerCount)
            .mapToObj(i -> {
                String labels = decodeLabels(byteBuffer);
                int queryType = byteBuffer.getShort();
                int queryClass = byteBuffer.getShort();
                int ttl = byteBuffer.getInt();
                int dataLength = byteBuffer.getShort();
                byte[] data = new byte[dataLength];
                byteBuffer.get(data);
                return new DNSSectionAnswer.DNSRecord(DNSMessage.Type.fromValue(queryType).orElseThrow(), labels, ttl, data);
            })
            .collect(Collectors.toList());
        return new DNSSectionAnswer(records);
    }
}
