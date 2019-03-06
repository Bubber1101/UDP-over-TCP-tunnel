import java.net.UnknownHostException;

public class Main {

    public static void main(String... args) throws UnknownHostException, InterruptedException {
        Relay relay = new Relay(12345);
        int[] agentPorts = {3111,3112};
        int[] remotePorts = {3121,3122};
        RemoteProcess remoteProcess = new RemoteProcess(remotePorts);
        Agent agent = new Agent(relay.getRelayAddress(),relay.getTcpPort(),remoteProcess.getRemoteAddress(),agentPorts);
        ClientProcess clientProcess = new ClientProcess(3111,agent.getAgentAddress().getHostAddress(),"3","2");
        ClientProcess clientProcess2 = new ClientProcess(3112,agent.getAgentAddress().getHostAddress(),"5");

        relay.start();
        remoteProcess.start();
        agent.start();
        clientProcess.start();
        clientProcess2.start();
    }
}
