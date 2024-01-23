package main;

import java.io.IOException;
import java.net.DatagramSocket;

import adapter.SocketDNSReceiverGateway;
import adapter.SocketDNSRequesterGateway;
import domain.model.DNSMessage;
import usecase.SimpleDNSResponse;

public class Respond {

    public static void main(String[] args) {
        try (final DatagramSocket serverSocket = new DatagramSocket(2054)) {
            serverSocket.setReuseAddress(true);
            final SocketDNSReceiverGateway dnsReceiverGateway = new SocketDNSReceiverGateway(serverSocket);
            while(true) {
                final DNSMessage questionMessage = dnsReceiverGateway.receive();
                System.out.println(STR."Received data : \{questionMessage}");

                final DNSMessage responseMessage = new SimpleDNSResponse().getResponseMessage(questionMessage);
                System.out.println(STR."Response data : \{responseMessage}");
                new SocketDNSRequesterGateway(serverSocket, dnsReceiverGateway.getSenderAddress())
                    .send(responseMessage);
            }
        } catch (IOException e) {
            System.out.println(STR."IOException: \{e.getMessage()}");
        }
    }
}
