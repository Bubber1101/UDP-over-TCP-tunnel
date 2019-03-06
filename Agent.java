import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Agent extends Thread {
    private InetAddress agentAddress;
    private InetAddress relayAddress;
    private int relayPort;
    private String remoteProcessAddress;
    private int[] ports;
    private ArrayList<DatagramSocket> clients;
    private HashMap<Integer, SocketAddress> portsToClients;
    private Socket relaySocket;
    private PrintWriter relayPrintWriter;
    private BufferedReader relayBufferedReader;

    public Agent(InetAddress relayAddress, int relayPort, InetAddress r, int[] ports) {
        this.relayAddress = relayAddress;
        this.relayPort = relayPort;
        this.remoteProcessAddress = r.getHostAddress();
        this.ports = ports;
        clients = new ArrayList<>();
        portsToClients = new HashMap<Integer, SocketAddress>();
        for (int p : ports) {
            portsToClients.put(p, null);
        }

        try {
            agentAddress = InetAddress.getLocalHost();

            //Relay setup
            relaySocket = new Socket(relayAddress, relayPort);
            relayPrintWriter = new PrintWriter(relaySocket.getOutputStream(), true);
            relayBufferedReader = new BufferedReader(new InputStreamReader(relaySocket.getInputStream()));
            relayPrintWriter.println(remoteProcessAddress);
            System.out.println("AGENT: SENT CONFIGURATION " + remoteProcessAddress);

            //datagram sockets setup
            for (int port : ports) {
                DatagramSocket datagramSocket = new DatagramSocket(port);
                clients.add(datagramSocket);

                //each socket has a udp thread for receiving
                Thread udpThread = new Thread(() -> {
                    while (true) {
                        byte[] receivingBuff = new byte[508];
                        DatagramPacket receivingPacket = new DatagramPacket(receivingBuff, receivingBuff.length);

                        try {
                            datagramSocket.receive(receivingPacket);
                            System.out.println("AGENT: RECEIVED A PACKET " + receivingPacket.toString());
                            if (portsToClients.get(datagramSocket.getLocalPort()) == null) {
                                portsToClients.put(datagramSocket.getLocalPort(), receivingPacket.getSocketAddress());
                            }
                            if (receivingPacket.getSocketAddress().equals(portsToClients.get(datagramSocket.getLocalPort()))) {
                                String udpMessage = new String(receivingPacket.getData(), 0, receivingPacket.getLength()).trim();
                                System.out.println("AGENT: RECEIVED A MESSAGE " + udpMessage);

                                udpMessage = (datagramSocket.getLocalPort() + 10) + "%%" + udpMessage;
                                relayPrintWriter.println(udpMessage);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
                udpThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //a thread for tcp receiving and sending to utp thread
    @Override
    public void run() {
        while (true) {
            try {
                String[] splitMessage = relayBufferedReader.readLine().split("%%");
                String messageToClient = splitMessage[1];
                int port = Integer.parseInt(splitMessage[0]);
                DatagramSocket d = clients.stream().filter(p -> p.getLocalPort() == port).findFirst().get();
                byte[] sendingBuff = messageToClient.getBytes();
                DatagramPacket sendingPacket = new DatagramPacket(sendingBuff, sendingBuff.length, portsToClients.get(port));
                try {
                    d.send(sendingPacket);
                } catch (IOException e) {
                    System.out.println("Client couldn't send: " + sendingPacket.toString());
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public InetAddress getAgentAddress() {
        return agentAddress;
    }
}
