public class DNSMessage {

    private final int packetIdentifier;
    private final int questionCount;
    private final String labels;

    public DNSMessage(int packetIdentifer, int questionCount, String labels) {
        this.packetIdentifier = packetIdentifer;
        this.questionCount = questionCount;
        this.labels = labels;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public String getLabels() {
        return labels;
    }

    public String toString() {
        return DNSMessage.class.getSimpleName() + "(" +
            "ID=" + getPacketIdentifier() + "," +
            "label=" + getLabels() + ")";
    }
}
