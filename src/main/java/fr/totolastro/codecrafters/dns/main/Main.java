package fr.totolastro.codecrafters.dns.main;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import fr.totolastro.codecrafters.dns.adapter.SocketDNSReceiverGateway;
import fr.totolastro.codecrafters.dns.adapter.SocketDNSRequesterGateway;
import fr.totolastro.codecrafters.dns.domain.model.DNSMessage;
import fr.totolastro.codecrafters.dns.usecase.DNSResponseRetriever;
import fr.totolastro.codecrafters.dns.usecase.ForwardedDNSResponse;
import fr.totolastro.codecrafters.dns.usecase.SimpleDNSResponse;

import static java.lang.StringTemplate.STR;

public class Main {

    public static void main(String[] args) {
        final InetSocketAddress forwardAddress = retrieveForwardAddress(args);
        try (final DatagramSocket serverSocket = new DatagramSocket(2053)) {
            serverSocket.setReuseAddress(true);
            final SocketDNSReceiverGateway dnsReceiverGateway = new SocketDNSReceiverGateway(serverSocket);
            final DNSResponseRetriever responseRetriever = createDNSResponseRetriever(forwardAddress, serverSocket);
            while(true) {
                final DNSMessage questionMessage = dnsReceiverGateway.receive();
                SocketAddress senderAddress = dnsReceiverGateway.getSenderAddress();
                System.out.println(STR."OriginalRequest(\{senderAddress}) : \{questionMessage}");

                final DNSMessage responseMessage = responseRetriever.getResponseMessage(questionMessage);
                System.out.println(STR."FinalResponse(\{senderAddress}) : \{responseMessage}");

                new SocketDNSRequesterGateway(serverSocket, senderAddress).send(responseMessage);
            }
        } catch (IOException e) {
            System.out.println(STR."IOException: \{e.getMessage()}");
        }
    }

    private static InetSocketAddress retrieveForwardAddress(String[] args) {
        Map<String, String> arguments = parseArgs(args);
        System.out.println(STR."Arguments = \{arguments}");
        if (arguments.containsKey("--resolver")) {
            String[] resolverAddress = arguments.get("--resolver").split(":");
            return new InetSocketAddress(
                resolverAddress[0],
                Integer.parseInt(resolverAddress[1])
            );
        }
        return null;
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                arguments.put(args[i], args[i + 1]);
                i++;
            } else {
                arguments.put(args[i], "");
            }
        }
        return arguments;
    }

    private static DNSResponseRetriever createDNSResponseRetriever(InetSocketAddress forwardAddress, DatagramSocket serverSocket) {
        if (forwardAddress != null) {
            return new ForwardedDNSResponse(
                new SocketDNSRequesterGateway(serverSocket, forwardAddress),
                new SocketDNSReceiverGateway(serverSocket)
            );
        } else {
            return new SimpleDNSResponse();
        }
    }
}
