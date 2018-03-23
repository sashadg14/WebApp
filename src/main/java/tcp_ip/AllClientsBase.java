package tcp_ip;

import javafx.util.Pair;
import tcp_ip.channels.AbstractSocket;
import tcp_ip.channels.SChannel;
import tcp_ip.client.Agent;
import tcp_ip.client.Client;
import tcp_ip.client.User;

import javax.jws.soap.SOAPBinding;
import javax.swing.event.CaretListener;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by Alex on 15.02.2018.
 */

public class AllClientsBase {
    private List<User> userList = Collections.synchronizedList(new LinkedList<User>());

    private List<User> waitingUsersList = Collections.synchronizedList(new LinkedList<User>());

    private List<Agent> agentList = Collections.synchronizedList(new LinkedList<>());

    private List<Agent> freeArentsList = Collections.synchronizedList(new LinkedList<Agent>());

    private List<Pair<User, Agent>> pairUserAgentList = new LinkedList<>();

    private AtomicLong atomicLong;

    public AllClientsBase() {
        atomicLong = new AtomicLong();
    }

    ;
    /*
    private static AllClientsBase allClientsBase=new AllClientsBase();

    public static AllClientsBase getInstance() {
        return allClientsBase;
    }*/

    public void addNewUser(AbstractSocket channel, String name) {
        userList.add(new User(atomicLong.getAndIncrement(), channel, name));
        // waitingUsersList.add(channel);
    }

    public void addUserChannelInWaiting(AbstractSocket channel) {
        // System.out.println(getClientNameByChanel(channel)+"========================================");
        for (User user : userList)
            if (user.getAbstractSocket().equals(channel))
                waitingUsersList.add(user);
        //serverCommunication.tryToCreateNewPair();
    }

    public void addNewAgent(AbstractSocket channel, String name) {
        Agent agent = new Agent(atomicLong.getAndIncrement(), channel, name);
        agentList.add(agent);
        freeArentsList.add(agent);
        //serverCommunication.tryToCreateNewPair();
    }

    public boolean isAutorized(AbstractSocket channel) {
        for (User user : userList)
            if (user.getAbstractSocket().equals(channel))
                return true;

        for (Agent agent : agentList)
            if (agent.getAbstractSocket().equals(channel))
                return true;
        return false;
    }

    public String getClientNameByChanel(AbstractSocket channel) {
        for (User user : userList)
            if ((user.getAbstractSocket()).equals(channel))
                return user.getName();

        for (Agent agent : agentList)
            if ((agent.getAbstractSocket().equals(channel)))
                return agent.getName();
        return "not authorized";
    }

    public String getClientNameByChanel(SocketChannel channel) {
        for (User user : userList)
            if (user.getAbstractSocket() instanceof SChannel && ((SChannel) user.getAbstractSocket()).getSocketChannel().equals(channel))
                return user.getName();

        for (Agent agent : agentList)
            if (agent.getAbstractSocket() instanceof SChannel && ((SChannel) agent.getAbstractSocket()).getSocketChannel().equals(channel))
                return agent.getName();
        return "not authorized";
    }

    public boolean doesClientHaveInterlocutor(AbstractSocket channel) {
        for (Pair<User, Agent> pair : pairUserAgentList)
            if (pair.getKey().getAbstractSocket().equals(channel) || pair.getValue().getAbstractSocket().equals(channel))
                return true;
        return false;
    }

    public void breakChatBetweenUserAndAgent(AbstractSocket userChannel) {
        Iterator<Pair<User, Agent>> pairIterator = pairUserAgentList.iterator();
        while (pairIterator.hasNext()) {
            Pair<User, Agent> pair = pairIterator.next();
            if (pair.getKey().getAbstractSocket().equals(userChannel)) {
                freeArentsList.add(pair.getValue());
                pairIterator.remove();
                break;
            }
        }
    }

    public void breakChatBetweenAgentAndUser(AbstractSocket agentChannel) {
        Iterator<Pair<User, Agent>> pairIterator = pairUserAgentList.iterator();
        while (pairIterator.hasNext()) {
            Pair<User, Agent> pair = pairIterator.next();
            if (pair.getValue().getAbstractSocket().equals(agentChannel)) {
                freeArentsList.add(pair.getValue());
                pairIterator.remove();
                break;
            }
        }

    }

    public boolean doesItsUserChannel(AbstractSocket channel) {
        for (AbstractSocket abstractSocket : userList.stream().map(User::getAbstractSocket).collect(Collectors.toList())) {
            if (abstractSocket.equals(channel))
                return true;
        }
        return false;
    }

    public boolean doesItsAgentChannel(AbstractSocket channel) {
        for (AbstractSocket abstractSocket : agentList.stream().map(Agent::getAbstractSocket).collect(Collectors.toList())) {
            if (abstractSocket.equals(channel))
                return true;
        }
        return false;
    }


    public AbstractSocket getUserInterlocutorChannel(AbstractSocket channel) {
        for (Pair<User, Agent> pair : pairUserAgentList)
            if (pair.getKey().getAbstractSocket().equals(channel))
                return pair.getValue().getAbstractSocket();
        return null;
    }
    public List<User> getAgentInterlocutors(AbstractSocket channel) {
        List<User> users=new LinkedList<>();
        for (Pair<User, Agent> pair : pairUserAgentList)
            if (pair.getValue().getAbstractSocket().equals(channel))
                users.add(pair.getKey());
        return users;
    }



    public Client getClientByChannel(AbstractSocket channel) {
        for (User user : userList)
            if (user.getAbstractSocket().equals(channel))
                return user;
        for (Agent agent: agentList)
            if (agent.getAbstractSocket().equals(channel))
                return agent;
        return null;
    }

    public void removeUserChanelFromBase(AbstractSocket userChannel) {
        for (User user : waitingUsersList)
            if (user.getAbstractSocket().equals(userChannel))
                waitingUsersList.remove(user);
        //if (userList.containsKey(userChannel))
        for (User user : userList)
            if (user.getAbstractSocket().equals(userChannel))
                userList.remove(user);
    }

    public void removeAgentChanelFromBase(AbstractSocket agentChannel) {
        for (Agent agent : freeArentsList) {
            if (agent.getAbstractSocket().equals(agentChannel))
                freeArentsList.remove(agent);
        }
        for (Agent agent : agentList) {
            if (agent.getAbstractSocket().equals(agentChannel))
                agentList.remove(agent);
        }
    }

    public Pair<User, Agent> createNewPairOfUserAndAgent() {
        if (isSomeUsersWait() && isSomeAgentsFree()) {
            //first channel - user, second - agent channel
            Pair<User, Agent> pair = new Pair<>(waitingUsersList.remove(0), freeArentsList.get(0));
            pairUserAgentList.add(pair);
            return pair;
        }
        return null;
    }

    private boolean isSomeUsersWait() {
        return waitingUsersList.size() != 0;
    }

    private boolean isSomeAgentsFree() {
        return freeArentsList.size() != 0;
    }

}
