public record DNSSectionHeader(
    int packetIdentifier,
    int operationCode,
    int recursionDesired,
    int questionCount,
    int answerCount
) {}
