package tcp_ip.client;

import tcp_ip.channels.AbstractSocket;

public interface Client {
    public long getId();

    public AbstractSocket getAbstractSocket();

    public String getName();
}
