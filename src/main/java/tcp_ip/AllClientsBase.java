package tcp_ip;

import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tcp_ip.channels.AbstractSocket;
import tcp_ip.channels.SChannel;

import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Alex on 15.02.2018.
 */

public class AllClientsBase {
    private Map<AbstractSocket, String> usersMap = new ConcurrentHashMap<>();
    private List<AbstractSocket> waitingUsersList = Collections.synchronizedList(new LinkedList<>());

    private Map<AbstractSocket, String> agentsMap =  new ConcurrentHashMap<>();
    private List<AbstractSocket> freeArentsList = Collections.synchronizedList(new LinkedList<>());

    private List<Pair<AbstractSocket, AbstractSocket>> pairUserAgentList = new LinkedList<>();



    /*private AllClientsBase(){};

    private static AllClientsBase allClientsBase=new AllClientsBase();

    public static AllClientsBase getInstance() {
        return allClientsBase;
    }*/

    public void addNewUser(AbstractSocket channel, String name) {
        usersMap.put(channel, name);
        // waitingUsersList.add(channel);
    }

    public void addUserChannelInWaiting(AbstractSocket channel) {
       // System.out.println(getClientNameByChanel(channel)+"========================================");
        waitingUsersList.add(channel);
        //serverCommunication.tryToCreateNewPair();
    }

    public void addNewAgent(AbstractSocket channel, String name) {
        agentsMap.put(channel, name);
        freeArentsList.add(channel);
        //serverCommunication.tryToCreateNewPair();
    }

    public boolean isAutorized(AbstractSocket channel, String name) {
        return usersMap.containsKey(channel) || agentsMap.containsKey(channel) || agentsMap.containsValue(name) || usersMap.containsValue(name);
    }

    public String getClientNameByChanel(AbstractSocket channel) {
        if (usersMap.containsKey(channel))
            return usersMap.get(channel);
        else if (agentsMap.containsKey(channel))
            return agentsMap.get(channel);
        return "not authorized";
    }

    public String getClientNameByChanel(SocketChannel channel) {
        for (AbstractSocket abstractSocket : usersMap.keySet())
            if (abstractSocket instanceof SChannel &&((SChannel)abstractSocket).getSocketChannel().equals(channel))
                return usersMap.get(abstractSocket);

        for (AbstractSocket abstractSocket : agentsMap.keySet())
            if (abstractSocket instanceof SChannel &&((SChannel)abstractSocket).getSocketChannel().equals(channel))
                return agentsMap.get(abstractSocket);
        return "not authorized";
    }

    public boolean doesClientHaveInterlocutor(AbstractSocket channel) {
        for (Pair<AbstractSocket, AbstractSocket> pair : pairUserAgentList)
            if (pair.getKey().equals(channel) || pair.getValue().equals(channel))
                return true;
        return false;
    }

    public void breakChatBetweenUserAndAgent(AbstractSocket userChannel) {
        Iterator<Pair<AbstractSocket, AbstractSocket>> pairIterator = pairUserAgentList.iterator();
        while (pairIterator.hasNext()) {
            Pair<AbstractSocket, AbstractSocket> pair = pairIterator.next();
            if (pair.getKey().equals(userChannel)) {
                freeArentsList.add(pair.getValue());
                pairIterator.remove();
                break;
            }
        }
    }

    public void breakChatBetweenAgentAndUser(AbstractSocket agentChannel) {
        Iterator<Pair<AbstractSocket, AbstractSocket>> pairIterator = pairUserAgentList.iterator();
        while (pairIterator.hasNext()) {
            Pair<AbstractSocket, AbstractSocket> pair = pairIterator.next();
            if (pair.getValue().equals(agentChannel)) {
                freeArentsList.add(pair.getValue());
                pairIterator.remove();
            }
        }

    }

    public boolean doesItsUserChannel(AbstractSocket channel) {
        for (AbstractSocket abstractSocket:usersMap.keySet()) {
            if(abstractSocket.equals(channel))
            return true;
        }
        return false;
    }

    public boolean doesItsAgentChannel(AbstractSocket channel) {
        for (AbstractSocket abstractSocket:agentsMap.keySet()) {
            if(abstractSocket.equals(channel))
                return true;
        }
        return false;
    }

    public AbstractSocket getClientInterlocutorChannel(AbstractSocket channel) {
        for (Pair<AbstractSocket, AbstractSocket> pair : pairUserAgentList)
            if (pair.getKey().equals(channel))
                return pair.getValue();
            else if (pair.getValue().equals(channel))
                return pair.getKey();
        return null;
    }

    public void removeUserChanelFromBase(AbstractSocket userChannel) {
        if (waitingUsersList.contains(userChannel))
            waitingUsersList.remove(userChannel);
        if (usersMap.containsKey(userChannel))
            usersMap.remove(userChannel);
    }

    public void removeAgentChanelFromBase(AbstractSocket agentChannel) {
        if (freeArentsList.contains(agentChannel))
            freeArentsList.remove(agentChannel);
        if (agentsMap.containsKey(agentChannel))
            agentsMap.remove(agentChannel);
    }

    public Pair<AbstractSocket, AbstractSocket> createNewPairOfUserAndAgent() {
        if (isSomeUsersWait() && isSomeAgentsFree()) {
            //first channel - user, second - agent channel
            Pair<AbstractSocket, AbstractSocket> pair = new Pair<>(waitingUsersList.remove(0), freeArentsList.remove(0));
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
