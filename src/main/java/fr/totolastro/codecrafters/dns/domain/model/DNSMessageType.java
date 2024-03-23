package fr.totolastro.codecrafters.dns.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum DNSMessageType {
    A(1),
    CNAME(5);
    public final int value;

    DNSMessageType(int value) {
        this.value = value;
    }

    public static Optional<DNSMessageType> fromValue(int value) {
        return Arrays.stream(values())
            .filter(type -> type.value == value)
            .findFirst();
    }
}
