package tcp_ip.client;

import tcp_ip.channels.AbstractSocket;

public class User implements Client{
    private long id;
    private AbstractSocket abstractSocket;
    private String name;

    public User(long id, AbstractSocket abstractSocket, String name) {
        this.id = id;
        this.abstractSocket = abstractSocket;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public AbstractSocket getAbstractSocket() {
        return abstractSocket;
    }

    public String getName() {
        return name;
    }
}
