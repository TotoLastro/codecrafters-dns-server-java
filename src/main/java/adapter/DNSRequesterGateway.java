package adapter;

import java.io.IOException;

import domain.model.DNSMessage;

public interface DNSRequesterGateway {
    void send(DNSMessage message) throws IOException;
}
