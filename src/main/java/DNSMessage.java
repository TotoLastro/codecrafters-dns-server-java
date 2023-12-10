import java.util.Arrays;
import java.util.Optional;

public record DNSMessage(DNSSectionHeader header, DNSSectionQuestion question, DNSSectionAnswer answer) {

    public enum Type {
        A(1),
        CNAME(5);
        public final int value;

        Type(int value) {
            this.value = value;
        }

        public static Optional<Type> fromValue(int value) {
            return Arrays.stream(values())
                .filter(type -> type.value == value)
                .findFirst();
        }
    }
}
