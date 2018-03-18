package tcp_ip.channels;

import java.io.IOException;

public interface AbstractSocket {
    void sendMessage(String message) throws IOException;
    void close() throws IOException;
}
