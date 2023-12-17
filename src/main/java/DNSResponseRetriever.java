import java.io.IOException;

@FunctionalInterface
public interface DNSResponseRetriever {
    DNSMessage getResponseMessage(DNSMessage questionMessage) throws IOException;
}
