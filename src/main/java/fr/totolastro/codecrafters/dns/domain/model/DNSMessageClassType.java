package fr.totolastro.codecrafters.dns.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum DNSMessageClassType {
    INTERNET(1),
    CSNET(2),
    CHAOS(3),
    HESIOD(4);
    public final int value;

    DNSMessageClassType(int value) {
        this.value = value;
    }

    public static Optional<DNSMessageClassType> fromValue(int value) {
        return Arrays.stream(values())
            .filter(type -> type.value == value)
            .findFirst();
    }
}
