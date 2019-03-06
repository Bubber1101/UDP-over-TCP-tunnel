import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class RelayThread extends Thread{
    private Socket agentSocket;
    private List<InetSocketAddress> remotePorts;
    private PrintWriter out;
    private BufferedReader in;
    private InetAddress remoteAddress;
    private Relay parent;

    public RelayThread(Socket agentSocket, Relay parent){
        this.agentSocket = agentSocket;
        this.parent = parent;
        try {
            out = new PrintWriter(agentSocket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String cofiguration = in.readLine();
            System.out.println("RELAYTHREAD: GOT CONFIGURATION MESSAGE " + cofiguration);
            remoteAddress = InetAddress.getByName(cofiguration);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Didn't configure");
        }

        while(true){
            try {
                String message = in.readLine();
                String[] messageParts = message.split("%%");
                parent.addToQueue(new Message(new InetSocketAddress(remoteAddress, Integer.parseInt(messageParts[0])),messageParts[1]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




    }

    public void forward(String message, int port){
    out.println(port + "%%" + message);
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }
}
