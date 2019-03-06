import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Relay  extends Thread{
    private int tcpPort;
    private InetAddress relayAddress;
    private ServerSocket tcpSocket;
    private BlockingQueue<Message> udpMessagesToSend;
    private DatagramSocket udpSocket;
    private List<RelayThread> agents;

    public Relay(int tcpPort) {
        this.tcpPort = tcpPort;
        udpMessagesToSend = new LinkedBlockingQueue<>();
        agents = new ArrayList<>();


        try {
            relayAddress = InetAddress.getLocalHost();

            tcpSocket = new ServerSocket(tcpPort);
            System.out.println("RELAY: TCP SOCKET OPENED");
            udpSocket = new DatagramSocket();
            System.out.println("RELAY: UDP SOCKET OPENED ON PORT: " + udpSocket.getLocalPort());

        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread sendToRemote = new Thread(() -> {
            while (true) {
                if (udpMessagesToSend.size() > 0) {

                    try {
                        Message temp = udpMessagesToSend.take();
                        String formattedMessage = temp.getMessage();
                        byte[] sendingBuff = formattedMessage.getBytes();
                        DatagramPacket sendingPacket = new DatagramPacket(sendingBuff, sendingBuff.length, temp.getRemoteAddress());
                        try {
                            udpSocket.send(sendingPacket);
                            System.out.println("RELAY: SENT A MESSAGE TO REMOTE: " + formattedMessage);
                        } catch (IOException e) {
                            System.out.println("Client couldn't send: " + sendingPacket.toString());
                            e.printStackTrace();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendToRemote.start();

        Thread receiveFromRemote = new Thread(() -> {
            while (true) {
                byte[] receivingBuff = new byte[508];
                DatagramPacket receivingPacket = new DatagramPacket(receivingBuff, receivingBuff.length);

                try {
                    udpSocket.receive(receivingPacket);
                    String udpMessage = new String(receivingPacket.getData(), 0, receivingPacket.getLength()).trim();
                    System.out.println("RELAY: RECEIVED A MESSAGE FROM REMOTE: " + udpMessage);
                    int port = receivingPacket.getPort()-10;
                    agents.stream().filter(p-> p.getRemoteAddress().equals(receivingPacket.getAddress())).findFirst().get().forward(udpMessage,port);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        receiveFromRemote.start();


    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket agentSocket = tcpSocket.accept();
                RelayThread rt = new RelayThread(agentSocket,this);
                agents.add(rt);
                rt.start();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void addToQueue(Message message) {
        udpMessagesToSend.add(message);
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public InetAddress getRelayAddress() {
        return relayAddress;
    }
}


