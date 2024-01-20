package adapter;

import java.io.IOException;

import domain.model.DNSMessage;

public interface DNSReceiverGateway {
    DNSMessage receive() throws IOException;
}
