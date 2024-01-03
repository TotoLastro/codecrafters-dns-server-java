package domain.model;

import java.util.Arrays;
import java.util.Optional;

public record DNSSectionHeader(
    int packetIdentifier,
    QueryOrResponse queryOrResponse,
    int operationCode,
    int authoritativeAnswer,
    int truncation,
    int recursionDesired,
    int recursionAvailable,
    int reserved,
    int error,
    int questionCount,
    int answerCount,
    int nameserverCount,
    int additionalRecordCount
) {
    public enum QueryOrResponse {
        QUERY(0),
        RESPONSE(1);
        public final int value;
        QueryOrResponse(int value) {
            this.value = value;
        }
        public static Optional<QueryOrResponse> fromValue(int value) {
            return Arrays.stream(values())
                .filter(type -> type.value == value)
                .findFirst();
        }
    }
}
