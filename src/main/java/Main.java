import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {
    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while(true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                System.out.println("Received data");

                final byte[] bufResponse = new byte[512];
                // Packet Identifer
                bufResponse[0] = buf[0];
                bufResponse[1] = buf[1];
                // QuestoinResponse
                bufResponse[2] |= (byte) 0b10000000;
                // OPCODE
                bufResponse[2] &= (byte) 0b10000111;
                // Authoritative Answer
                bufResponse[2] &= (byte) 0b11111011;
                // Truncation
                bufResponse[2] &= (byte) 0b11111101;
                // Recursion Desired
                bufResponse[2] &= (byte) 0b11111110;
                // Recursion Available
                bufResponse[3] &= (byte) 0b01111111;
                // Reserved
                bufResponse[3] &= (byte) 0b10001111;
                // Error
                bufResponse[3] &= (byte) 0b11110000;
                // Question Count
                bufResponse[4] = 0;
                bufResponse[5] = 0;
                // Answer Record Count
                bufResponse[6] = 0;
                bufResponse[7] = 0;
                // Authority Record Count
                bufResponse[8] = 0;
                bufResponse[9] = 0;
                // Additional Record Count
                bufResponse[10] = 0;
                bufResponse[11] = 0;
                final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
