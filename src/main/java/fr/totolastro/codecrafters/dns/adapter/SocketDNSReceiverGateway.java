package fr.totolastro.codecrafters.dns.adapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import fr.totolastro.codecrafters.dns.domain.model.DNSMessage;
import fr.totolastro.codecrafters.dns.domain.parsers.DNSMessageDecoder;
import lombok.Getter;

public class SocketDNSReceiverGateway implements DNSReceiverGateway {

    private final DatagramSocket serverSocket;
    @Getter
    private SocketAddress senderAddress;

    public SocketDNSReceiverGateway(DatagramSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public DNSMessage receive() throws IOException {
        final byte[] buffer = new byte[512];
        final DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
        serverSocket.receive(receivedPacket);
        senderAddress = receivedPacket.getSocketAddress();
        return DNSMessageDecoder.decode(receivedPacket.getData());
    }
}
