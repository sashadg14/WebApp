package tcp_ip.client;

import tcp_ip.channels.AbstractSocket;

import java.util.concurrent.atomic.AtomicInteger;

public class Agent implements Client{
    private long id;
    private AbstractSocket abstractSocket;
    private String name;
    private AtomicInteger usersCount;
    public Agent(long id, AbstractSocket abstractSocket, String name) {
        this.id = id;
        this.abstractSocket = abstractSocket;
        this.name = name;
        usersCount=new AtomicInteger();
    }

    public AtomicInteger getUsersCount() {
        return usersCount;
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
