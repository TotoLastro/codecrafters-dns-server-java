import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    public static final int NO_ERROR = 0;
    public static final int NOT_IMPLEMENTED = 4;

    public static void main(String[] args) {
        final InetSocketAddress forwardAddress = retrieveForwardAddress(args);
        try (final DatagramSocket serverSocket = new DatagramSocket(2053)) {
            serverSocket.setReuseAddress(true);
            while(true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                System.out.println("Received from " + packet.getSocketAddress() + " : " + Arrays.toString(Arrays.copyOf(buf, packet.getLength())));

                final DNSMessage questionMessage = DNSMessageDecoder.decode(buf);
                System.out.println("Received data : " + questionMessage);

                final DatagramPacket responsePacket;
                if (forwardAddress != null) {
                    DatagramPacket responseFromForward = getResponsePacketFromForwardServer(packet, serverSocket, forwardAddress);
                    responsePacket = new DatagramPacket(responseFromForward.getData(), responseFromForward.getLength(), packet.getSocketAddress());
                } else {
                    final DNSMessage responseMessage = getResponseMessage(questionMessage);
                    byte[] bufResponse = DNSMessageEncoder.encode(responseMessage);
                    responsePacket = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                    System.out.println("Response data : " + responseMessage);
                }
                serverSocket.send(responsePacket);
                System.out.println("Sent to " + responsePacket.getSocketAddress() + " : " + Arrays.toString(Arrays.copyOf(responsePacket.getData(), responsePacket.getLength())));
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static InetSocketAddress retrieveForwardAddress(String[] args) {
        Map<String, String> arguments = parseArgs(args);
        System.out.println("Arguments = " + arguments);
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

    private static DatagramPacket getResponsePacketFromForwardServer(DatagramPacket packet, DatagramSocket serverSocket, InetSocketAddress forwardAddress) throws IOException {
        DatagramPacket forwardedPacked = new DatagramPacket(packet.getData(), packet.getLength(), forwardAddress);
        serverSocket.send(forwardedPacked);
        System.out.println("Sent to forward server : " + Arrays.toString(Arrays.copyOf(forwardedPacked.getData(), forwardedPacked.getLength())));

        final byte[] buf = new byte[512];
        final DatagramPacket responseFromForward = new DatagramPacket(buf, buf.length);
        serverSocket.receive(responseFromForward);
        System.out.println("Received from forward server : " + Arrays.toString(Arrays.copyOf(responseFromForward.getData(), responseFromForward.getLength())));
        System.out.println("Receive(" + responseFromForward.getSocketAddress() + ") : " + DNSMessageDecoder.decode(responseFromForward.getData()));
        return responseFromForward;
    }

    private static DNSMessage getResponseMessage(DNSMessage questionMessage) {
        DNSSectionAnswer responseAnswer = new DNSSectionAnswer(
            questionMessage.question().questions().stream()
                .map(question -> new DNSSectionAnswer.DNSRecord(
                    question.labels(),
                    DNSMessage.Type.A,
                    DNSMessage.ClassType.INTERNET,
                    60,
                    new byte[]{8, 8, 8, 8}
                ))
                .collect(Collectors.toList())
        );
        DNSSectionHeader responseHeader = new DNSSectionHeader(
            questionMessage.header().packetIdentifier(),
            DNSSectionHeader.QueryOrResponse.RESPONSE,
            questionMessage.header().operationCode(),
            0,
            0,
            0,
            0,
            0,
            isStandardQuery(questionMessage) ? NO_ERROR : NOT_IMPLEMENTED,
            questionMessage.header().questionCount(),
            responseAnswer.records().size(),
            0,
            0
        );
        return new DNSMessage(responseHeader, questionMessage.question(), responseAnswer);
    }

    private static boolean isStandardQuery(DNSMessage questionMessage) {
        return questionMessage.header().operationCode() == 0;
    }
}
