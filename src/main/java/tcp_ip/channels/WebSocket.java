package tcp_ip.channels;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WebSocket implements AbstractSocket{
    private WebSocketSession webSocketSession;

    public WebSocket(WebSocketSession socketSession){
        this.webSocketSession=socketSession;
    }

    @Override
    public void sendMessage(String message) throws IOException {
      //  System.out.println(message+" ->>>>>>");
        webSocketSession.sendMessage(new TextMessage(message));
    }

    @Override
    public void close() throws IOException {
        webSocketSession.close();
    }

    @Override
    public int hashCode() {
        return webSocketSession.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WebSocket && webSocketSession.equals(((WebSocket) obj).webSocketSession);
    }
}
