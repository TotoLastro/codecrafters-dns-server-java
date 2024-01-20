package adapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import domain.model.DNSMessage;
import domain.parsers.DNSMessageEncoder;

public class SocketDNSRequesterGateway implements DNSRequesterGateway {

    private final DatagramSocket senderSocket;
    private final SocketAddress targetAddress;

    public SocketDNSRequesterGateway(DatagramSocket senderSocket, SocketAddress targetAddress) {
        this.senderSocket = senderSocket;
        this.targetAddress = targetAddress;
    }

    @Override
    public void send(DNSMessage message) throws IOException {
        final byte[] queryBuffer = DNSMessageEncoder.encode(message);
        DatagramPacket packet = new DatagramPacket(queryBuffer, queryBuffer.length, targetAddress);
        senderSocket.send(packet);
    }
}
