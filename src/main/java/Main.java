import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while(true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                final DNSMessage questionMessage = DNSMessageDecoder.decode(buf);
                System.out.println("Received data : " + questionMessage);
                final DNSMessage responseMessage = getResponseMessage(questionMessage);
                System.out.println("Response data : " + responseMessage);
                byte[] bufResponse = DNSMessageEncoder.encode(responseMessage);

                final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static DNSMessage getResponseMessage(DNSMessage questionMessage) {
        DNSSectionAnswer responseAnswer = new DNSSectionAnswer(List.of(
            new DNSSectionAnswer.DNSRecord(
                DNSMessage.Type.A,
                questionMessage.question().labels(),
                60,
                new byte[]{8, 8, 8, 8}
            )
        ));
        DNSSectionHeader responseHeader = new DNSSectionHeader(
            questionMessage.header().packetIdentifier(),
            questionMessage.header().operationCode(),
            questionMessage.header().recursionDesired(),
            questionMessage.header().questionCount(),
            responseAnswer.records().size()
        );
        return new DNSMessage(responseHeader, questionMessage.question(), responseAnswer);
    }
}
