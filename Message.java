import java.net.InetSocketAddress;

public class Message{

    private InetSocketAddress remoteAddress;
    private String message;

    public Message(InetSocketAddress remoteAddress, String message) {
        this.remoteAddress = remoteAddress;
        this.message = message;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
