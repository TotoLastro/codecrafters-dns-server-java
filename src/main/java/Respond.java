import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Respond {

    public static final int NO_ERROR = 0;
    public static final int NOT_IMPLEMENTED = 4;

    public static void main(String[] args) {
        try (final DatagramSocket serverSocket = new DatagramSocket(2054)) {
            serverSocket.setReuseAddress(true);
            while(true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                final DNSMessage questionMessage = DNSMessageDecoder.decode(buf);
                System.out.println("Received data : " + questionMessage);

                final DNSMessage responseMessage = new SimpleDNSResponse().getResponseMessage(questionMessage);

                System.out.println("Response data : " + responseMessage);
                byte[] bufResponse = DNSMessageEncoder.encode(responseMessage);

                final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
