package tcp_ip.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SChannel implements AbstractSocket{
    private SocketChannel socketChannel;

    public SChannel(SocketChannel socketChannel){
        this.socketChannel=socketChannel;
    }

    @Override
    public void sendMessage(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        socketChannel.write(buffer);
        buffer.flip();
    }

    @Override
    public void close() throws IOException {
        socketChannel.close();
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    @Override
    public int hashCode() {
        return socketChannel.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SChannel && socketChannel.equals(((SChannel) obj).socketChannel);
    }
}
