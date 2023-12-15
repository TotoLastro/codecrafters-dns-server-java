import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        final InetSocketAddress forwardAddress = retrieveForwardAddress(args);
        try (final DatagramSocket serverSocket = new DatagramSocket(2053)) {
            serverSocket.setReuseAddress(true);
            while(true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                final DNSMessage questionMessage = DNSMessageDecoder.decode(buf);
                System.out.println("Received data from " + packet.getSocketAddress() + " : " + questionMessage);

                final DNSMessage responseMessage;
                if (forwardAddress != null) {
                    responseMessage = getResponseMessageFromForwardServer(questionMessage, serverSocket, forwardAddress);
                } else {
                    responseMessage = getResponseMessage(questionMessage);
                }

                byte[] bufResponse = DNSMessageEncoder.encode(responseMessage);
                final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                System.out.println("Response data to " + packetResponse.getSocketAddress() + " : " + responseMessage);

                serverSocket.send(packetResponse);
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

    private static DNSMessage getResponseMessageFromForwardServer(
        DNSMessage questionMessage,
        DatagramSocket serverSocket,
        InetSocketAddress forwardAddress
    ) throws IOException {
        byte[] question = DNSMessageEncoder.encode(questionMessage);
        DatagramPacket questionPacket = new DatagramPacket(question, question.length, forwardAddress);
        System.out.println("Forward(" + questionPacket.getSocketAddress() + ") : " + questionMessage);
        serverSocket.send(questionPacket);

        final byte[] buf = new byte[512];
        final DatagramPacket responsePacket = new DatagramPacket(buf, buf.length);
        serverSocket.receive(responsePacket);

        DNSMessage responseMessage = DNSMessageDecoder.decode(buf);
        System.out.println("Receive(" + responsePacket.getSocketAddress() + ") : " + responseMessage);

        return responseMessage;
    }

    private static DNSMessage getResponseMessage(DNSMessage questionMessage) {
        DNSSectionAnswer responseAnswer = new DNSSectionAnswer(
            questionMessage.question().questions().stream()
                .map(question -> new DNSSectionAnswer.DNSRecord(
                    DNSMessage.Type.A,
                    question.labels(),
                    60,
                    new byte[]{8, 8, 8, 8}
                ))
                .collect(Collectors.toList())
        );
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
