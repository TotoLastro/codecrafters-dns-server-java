package fr.totolastro.codecrafters.dns.adapter;

import java.io.IOException;

import fr.totolastro.codecrafters.dns.domain.model.DNSMessage;

public interface DNSRequesterGateway {
    void send(DNSMessage message) throws IOException;
}
