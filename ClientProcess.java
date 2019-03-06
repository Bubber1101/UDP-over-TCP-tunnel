import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class ClientProcess extends Thread {
    private DatagramSocket datagramSocket;
    private HashMap<Integer, String> pendingMessages;
    private Integer agentPort;
    private InetAddress agentAddress;

    public ClientProcess(Integer agentPort, String agentIp, String... args) throws UnknownHostException {
        this.agentPort = agentPort;
        agentAddress = InetAddress.getByName(agentIp);
        pendingMessages = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            pendingMessages.put(i, args[i]);
            System.out.println("CLIENT: ADDED TO PENDING " + args[i]);

        }

        try {
            datagramSocket = new DatagramSocket();
            System.out.println("ClientProcess opened an UDP Socket on port: " + datagramSocket.getLocalPort());
        } catch (SocketException e) {
            System.out.println("Client process couldn't create na UDP socket");
        }
    }

    @Override
    public void run() {
        System.out.println("CLIENT STARTED, GOING TO SEND TO " + agentPort);
        pendingMessages.forEach((key, message) -> {
                    String formattedMessage = key + "#" + message;
                    byte[] sendingBuff = formattedMessage.getBytes();
                    DatagramPacket sendingPacket = new DatagramPacket(sendingBuff, sendingBuff.length, agentAddress, agentPort);
                    try {
                        datagramSocket.send(sendingPacket);
                        System.out.println("CLIENT SENT " + formattedMessage + " TO " + agentPort);

                    } catch (IOException e) {
                        System.out.println("Client couldn't send: " + sendingPacket.toString());
                        e.printStackTrace();
                    }
                }

        );

        while (true) {

            byte[] receivingBuff = new byte[508];
            DatagramPacket receivingPacket = new DatagramPacket(receivingBuff, receivingBuff.length);

            try {
                datagramSocket.receive(receivingPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String response = new String(receivingPacket.getData(), 0, receivingPacket.getLength()).trim();
            String[] responseParts = response.split("#");
            Integer responseKey = Integer.parseInt(responseParts[0]);
            String responseMessage = responseParts[1];

            System.out.println("Client Process received a response for message \"" + pendingMessages.get(responseKey) + "\" : " + responseMessage);
            pendingMessages.remove(responseKey);


        }
    }
}

