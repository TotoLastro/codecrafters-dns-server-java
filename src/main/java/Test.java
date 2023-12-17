import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Test {
    public static void main(String[] args) {
        // -123, 11, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 3, 97, 98, 99, 17, 108, 111, 110, 103, 97, 115, 115, 100, 111, 109, 97, 105, 110, 110, 97, 109, 101, 3, 99, 111, 109, 0, 0, 1, 0, 1, 3, 100, 101, 102, -64, 16, 0, 1, 0, 1
        byte[] query = new byte[]{
            // Packet Identifer
            (byte) 0x85, (byte) 0x0B,
            // Question / Standard query / Not truncated / Recursion Desired
            (byte) 0x01, (byte) 0x00,
            // Two questions
            (byte) 0x00, (byte) 0x02,
            // Zero answer
            (byte) 0x00, (byte) 0x00,
            // Zero Authority RR
            (byte) 0x00, (byte) 0x00,
            // One Additional RR
            (byte) 0x00, (byte) 0x00,
            // Label: abc.longassdomainname.com
            (byte) 0x03, (byte) 0x61, (byte) 0x62, (byte) 0x63,
            (byte) 0x11, (byte) 0x6C, (byte) 0x6F, (byte) 0x6E, (byte) 0x67, (byte) 0x61,
            (byte) 0x73, (byte) 0x73, (byte) 0x64, (byte) 0x6F, (byte) 0x6D, (byte) 0x61,
            (byte) 0x69, (byte) 0x6E, (byte) 0x6E, (byte) 0x61, (byte) 0x6D, (byte) 0x65,
            (byte) 0x03, (byte) 0x63, (byte) 0x6F, (byte) 0x6D,
            (byte) 0x00,
            // Type A (1) / CNAME (5)
            (byte) 0x00, (byte) 0x01,
            // Class IN(ternet)
            (byte) 0x00, (byte) 0x01,
            // Label: def.longassdomainname.com
            (byte) 0x03, (byte) 0x64, (byte) 0x65, (byte) 0x66,
            (byte) 0xC0, (byte) 0x10,
            // Type A (1) / CNAME (5)
            (byte) 0x00, (byte) 0x01,
            // Class IN(ternet)
            (byte) 0x00, (byte) 0x01
        };
        try (DatagramSocket socket = new DatagramSocket()) {
            InetSocketAddress destination = new InetSocketAddress(InetAddress.getLocalHost(), 2053);
            DatagramPacket packet = new DatagramPacket(query, query.length, destination);
            socket.send(packet);

            byte[] buf = new byte[512];
            DatagramPacket response = new DatagramPacket(buf, buf.length);
            socket.receive(response);

            System.out.println(DNSMessageDecoder.decode(buf));
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static final byte[] EXAMPLE_1 = new byte[]{
        // Packet Identifer
        (byte) 0xF2, (byte) 0x00,
        // Question / Standard query / Not truncated / Recursion Desired
        (byte) 0x01, (byte) 0x20,
        // Two questions
        (byte) 0x00, (byte) 0x02,
        // Zero answer
        (byte) 0x00, (byte) 0x00,
        // Zero Authority RR
        (byte) 0x00, (byte) 0x00,
        // One Additional RR
        (byte) 0x00, (byte) 0x01,
        // Label: codecrafters.io.tt
        (byte) 0x0c, (byte) 0x63, (byte) 0x6f, (byte) 0x64, (byte) 0x65, (byte) 0x63, (byte) 0x72,
        (byte) 0x61, (byte) 0x66, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x73,
        (byte) 0x02, (byte) 0x69, (byte) 0x6f,
        (byte) 0x00,
        // Type A (1) / CNAME (5)
        (byte) 0x00, (byte) 0x01,
        // Class IN(ternet)
        (byte) 0x00, (byte) 0x01,
        // Label: truc.io.tt
        (byte) 0x04, (byte) 0x74, (byte) 0x72, (byte) 0x75, (byte) 0x63,
        (byte) 0xc0, (byte) 0x19,
        // Type A (1) / CNAME (5)
        (byte) 0x00, (byte) 0x01,
        // Class IN(ternet)
        (byte) 0x00, (byte) 0x01,
        // Additional Record
        (byte) 0x00, (byte) 0x00, (byte) 0x29, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };
}