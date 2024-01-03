package usecase;

import java.io.IOException;

import domain.model.DNSMessage;

@FunctionalInterface
public interface DNSResponseRetriever {
    DNSMessage getResponseMessage(DNSMessage questionMessage) throws IOException;
}
