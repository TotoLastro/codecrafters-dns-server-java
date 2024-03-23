package fr.totolastro.codecrafters.dns.usecase;

import java.io.IOException;

import fr.totolastro.codecrafters.dns.domain.model.DNSMessage;

@FunctionalInterface
public interface DNSResponseRetriever {
    DNSMessage getResponseMessage(DNSMessage questionMessage) throws IOException;
}
