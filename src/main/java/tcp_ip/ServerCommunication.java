package tcp_ip;

import com.google.gson.Gson;
import javafx.util.Pair;
import messages_entities.AgentMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import tcp_ip.channels.AbstractSocket;
import tcp_ip.client.Agent;
import tcp_ip.client.Client;
import tcp_ip.client.User;
import utils.Constants;
import utils.MessagesUtils;
import utils.UsersSMSCache;

import java.io.IOException;
import java.util.stream.Collectors;

public class ServerCommunication {

    @Autowired
    private AllClientsBase allClientsBase/*=AllClientsBase.getInstance()*/;

    private MessagesUtils mUtils;
    private UsersSMSCache usersSMSCache;
    private Logger logger;
    private Gson gson;

    public ServerCommunication() {
        this.mUtils = new MessagesUtils();
        this.usersSMSCache = new UsersSMSCache();
        this.logger = Logger.getRootLogger();
        this.gson = new Gson();
    }
    /*private static ServerCommunication serverCommunication=new ServerCommunication();
    private ServerCommunication() {
    }

    public static ServerCommunication getInstance() {
        return serverCommunication;
    }*/

    public void handleRegistration(AbstractSocket userChannel, String message) throws IOException {
        String name = mUtils.getNameFromMessage(message);
        if (allClientsBase.isAutorized(userChannel)) {
            userChannel.sendMessage(Constants.ERROR_ALREADY_REGISTRED);
            return;
        }
        if (mUtils.isSignInUserMessage(message)) {
            handleUserRegistration(userChannel, name);
        } else if (mUtils.isSignInAgentMessage(message)) {
            handleAgentRegistration(userChannel, name);
        }
    }

    public void handleUserRegistration(AbstractSocket userChannel, String name) throws IOException {
        userChannel.sendMessage(Constants.SUCCESS_REGISTRED);
        allClientsBase.addNewUser(userChannel, name);
        logger.log(Level.INFO, "Registered user " + name);
    }

    public void handleAgentRegistration(AbstractSocket userChannel, String name) throws IOException {
        userChannel.sendMessage(Constants.SUCCESS_REGISTRED);
        allClientsBase.addNewAgent(userChannel, name);
        tryToCreateNewPair();
        logger.log(Level.INFO, "Registered agent " + name);
        // System.out.println("agent");
    }

    public void handleMessagesFromAutorizedUser(AbstractSocket userChannel, String message) {
        if (allClientsBase.doesClientHaveInterlocutor(userChannel)) {
            sendMessageToInterlocutorOf(userChannel, message);
        } else {
            try {
                sendMessageBackToClient(userChannel, message);
            } catch (IOException e) {
                logger.log(Level.INFO, "Client " + allClientsBase.getClientNameByChanel(userChannel) + " disconnect");
                handlingClientDisconnecting(userChannel);
            }
        }
    }

    private void sendMessageToInterlocutorOf(AbstractSocket clientChannel, String message) {
        if (allClientsBase.doesItsUserChannel(clientChannel)) {
            long id = allClientsBase.getClientByChannel(clientChannel).getId();
            AbstractSocket agentChannel = allClientsBase.getUserInterlocutorChannel(clientChannel);
            try {
                agentChannel.sendMessage(gson.toJson(new AgentMessage(message, id)));
            } catch (IOException e) {
                logger.log(Level.INFO, "Client " + allClientsBase.getClientNameByChanel(agentChannel) + " disconnect");
                handlingClientDisconnecting(agentChannel);
            }
        } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
            try {
                AgentMessage agentMessage = gson.fromJson(message, AgentMessage.class);
                for (User user : allClientsBase.getAgentInterlocutors(clientChannel))
                    if (user.getId() == agentMessage.getId()) {
                        try {
                            user.getAbstractSocket().sendMessage(agentMessage.getMessage());
                        } catch (IOException e) {
                            logger.log(Level.INFO, "Client " + allClientsBase.getClientNameByChanel(user.getAbstractSocket()) + " disconnect");
                            handlingClientDisconnecting(user.getAbstractSocket());
                        }
                    }
            } catch (Exception e) {
                logger.log(Level.ERROR, "Wrong info " + message);
            }
        }
    }

    private void sendMessageBackToClient(AbstractSocket clientChannel, String message) throws IOException {
        if (allClientsBase.doesItsUserChannel(clientChannel)) {
            usersSMSCache.addSMSinCache(clientChannel, message);
            allClientsBase.addUserChannelInWaiting(clientChannel);
            clientChannel.sendMessage(Constants.WAIT_AGENT);
            tryToCreateNewPair();
        } else clientChannel.sendMessage(Constants.WAIT_USER);
    }

    public void handlingClientDisconnecting(AbstractSocket clientChannel) {
        if (allClientsBase.doesClientHaveInterlocutor(clientChannel)) {
            if (allClientsBase.doesItsUserChannel(clientChannel)) {
                breakUserChannelConn(clientChannel);
                tryToCreateNewPair();
            } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
                breakAgentChannelConn(clientChannel);
            }
        } else handleSingleClientDisconnecting(clientChannel);
        try {
            clientChannel.close();
        } catch (Exception ignored) {
        }
    }
/*

    public void handlingClientSocketChDisconnecting(SocketChannel clientChannel) {
        if (allClientsBase.doesClientHaveInterlocutor(clientChannel)) {
            if (allClientsBase.doesItsUserChannel(clientChannel)) {
                breakUserChannelConn(clientChannel);
            } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
                breakAgentChannelConn(clientChannel);
            }
        } else handleSingleClientDisconnecting(clientChannel);
        try {
            clientChannel.close();
        } catch (Exception ignored) {
        }
    }
*/

    private void breakUserChannelConn(AbstractSocket clientChannel) {
        AbstractSocket channel = allClientsBase.getUserInterlocutorChannel(clientChannel);
        Client user = allClientsBase.getClientByChannel(clientChannel);
        allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
        tryToCreateNewPair();
        allClientsBase.removeUserChanelFromBase(clientChannel);
        try {
            channel.sendMessage("{ \"disconnected\":" + user.getId() + "}");
        } catch (IOException e) {
            handlingClientDisconnecting(channel);
        }
    }

    private void breakAgentChannelConn(AbstractSocket clientChannel) {
        for (AbstractSocket userChannel : allClientsBase.getAgentInterlocutors(clientChannel).stream().map(User::getAbstractSocket).collect(Collectors.toList())) {
            allClientsBase.breakChatBetweenAgentAndUser(clientChannel);
            allClientsBase.removeAgentChanelFromBase(clientChannel);
            try {
                userChannel.sendMessage("agent disconnected");
            } catch (IOException e) {
                handlingClientDisconnecting(userChannel);
            }
        }
    }

    private void handleSingleClientDisconnecting(AbstractSocket clientChannel) {
        if (allClientsBase.doesItsUserChannel(clientChannel))
            allClientsBase.removeUserChanelFromBase(clientChannel);
        else if (allClientsBase.doesItsAgentChannel(clientChannel))
            allClientsBase.removeAgentChanelFromBase(clientChannel);
    }

    public void tryToCreateNewPair() {
        //  System.out.println("ds");
        Pair<User, Agent> pair = allClientsBase.createNewPairOfUserAndAgent();
        if (pair != null) {
            String userName = allClientsBase.getClientNameByChanel(pair.getKey().getAbstractSocket());
            String agentName = allClientsBase.getClientNameByChanel(pair.getValue().getAbstractSocket());
            logger.log(Level.INFO, "Created chat between " + userName + " and " + agentName);
            try {
                pair.getKey().getAbstractSocket().sendMessage("your agent is " + allClientsBase.getClientNameByChanel(pair.getValue().getAbstractSocket()));
            } catch (IOException e) {
                handlingClientDisconnecting(pair.getKey().getAbstractSocket());
                return;
            }
            try {
                messagesForAgentFromPair(pair);
                // System.out.println(mUtils.createMessageToAgent(usersSMSCache.removeCachedSMS(pair.getKey().getAbstractSocket()), id));
            } catch (IOException e) {
                handlingClientDisconnecting(pair.getValue().getAbstractSocket());
            }
        }
    }
    private void messagesForAgentFromPair(Pair<User,Agent> pair) throws IOException {
        AbstractSocket userAbstractSocket = pair.getKey().getAbstractSocket();
        long id = allClientsBase.getClientByChannel(userAbstractSocket).getId();
        pair.getValue().getAbstractSocket().sendMessage(mUtils.createInitialMessageToAgent(allClientsBase.getClientNameByChanel(userAbstractSocket), id));
        pair.getValue().getAbstractSocket().sendMessage(new Gson().toJson(new AgentMessage(usersSMSCache.removeCachedSMS(pair.getKey().getAbstractSocket()), id)));
    }

    public void handleClientExit(AbstractSocket clientChannel) {
        try {
            clientChannel.close();
        } catch (Exception ignored) {
        }
        if (allClientsBase.doesClientHaveInterlocutor(clientChannel)) {
            try {
                if (allClientsBase.doesItsUserChannel(clientChannel)) {
                    logger.log(Level.INFO, "user " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
                    allClientsBase.getUserInterlocutorChannel(clientChannel).sendMessage("{ \"disconnected\":" + allClientsBase.getClientByChannel(clientChannel).getId() + "}");
                    allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
                    tryToCreateNewPair();
                    allClientsBase.removeUserChanelFromBase(clientChannel);
                } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
                    logger.log(Level.INFO, "agent " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
                    for(Client client:allClientsBase.getAgentInterlocutors(clientChannel))
                    client.getAbstractSocket().sendMessage("agent exit");
                    allClientsBase.breakChatBetweenAgentAndUser(clientChannel);
                    allClientsBase.removeAgentChanelFromBase(clientChannel);
                }
            } catch (IOException e) {
                handlingClientDisconnecting(clientChannel);
            }
        } else logger.log(Level.INFO, "client " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
    }
}
