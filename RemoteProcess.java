import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

public class RemoteProcess extends Thread {
    private InetAddress remoteAddress;
    private LinkedList<DatagramSocket> sockets;
    private InetAddress relayAddress;
    private int relayPort;

    RemoteProcess(int[] ports) throws UnknownHostException {
        sockets = new LinkedList<>();
        relayAddress = null;
        relayPort = 0;
        remoteAddress = InetAddress.getLocalHost();

        for (int port : ports) {
            try {
                sockets.add(new DatagramSocket(port));
            } catch (SocketException e) {
                System.out.println("Remote Process couldn't create Socket " + port);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        for (DatagramSocket datagramSocket : sockets) {
            Thread socketListener = new Thread(
                    () -> {
                        while (true) {
                            byte[] receivingBuff = new byte[508];
                            DatagramPacket receivingPacket = new DatagramPacket(receivingBuff, receivingBuff.length);

                            try {
                                datagramSocket.receive(receivingPacket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if(relayPort == 0 || relayAddress == null){
                                relayAddress = receivingPacket.getAddress();
                                relayPort = receivingPacket.getPort();
                            }

                            String message = new String(receivingPacket.getData(), 0, receivingPacket.getLength()).trim();
                            String[] messageParts = message.split("#");
                            int responseCalculated = (Integer.parseInt(messageParts[1]));
                            responseCalculated *=responseCalculated;
                            System.out.println("REMOTE: CALCULATED RESPONSE " + responseCalculated);
                            String response = messageParts[0]+ "#" + responseCalculated;
                            byte[] sendingBuff = response.getBytes();
                            DatagramPacket sendingPacket = new DatagramPacket(sendingBuff, sendingBuff.length, relayAddress, relayPort);
                            try {
                                datagramSocket.send(sendingPacket);
                            } catch (IOException e) {
                                System.out.println("remoteProcess couldn't send: " + sendingPacket.toString());
                                e.printStackTrace();
                            }


                        }

                    }
                    );
            socketListener.start();
        }
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }
}
