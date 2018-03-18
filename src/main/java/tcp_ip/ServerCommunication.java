package tcp_ip;

import javafx.util.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import tcp_ip.channels.AbstractSocket;

import java.io.IOException;

public class ServerCommunication {

    @Autowired
    private AllClientsBase allClientsBase/*=AllClientsBase.getInstance()*/;

    private MessagesUtils mUtils = new MessagesUtils();
    private UsersSMSCache usersSMSCache = new UsersSMSCache();
    private Logger logger = Logger.getRootLogger();



    /*private static ServerCommunication serverCommunication=new ServerCommunication();
    private ServerCommunication() {
    }

    public static ServerCommunication getInstance() {
        return serverCommunication;
    }*/

    public void handleRegistration(AbstractSocket userChannel, String message) throws IOException {
        String name = mUtils.getNameFromMessage(message);
        if (allClientsBase.isAutorized(userChannel, name)) {
           userChannel.sendMessage( Constants.ERROR_ALREADY_REGISTRED);
            return;
        }
        if (mUtils.isSignInUserMessage(message)) {
           userChannel.sendMessage( Constants.SUCCESS_REGISTRED);
            allClientsBase.addNewUser(userChannel, name);
            logger.log(Level.INFO, "Registered user " + name);
            //System.out.println("user");
        } else if (mUtils.isSignInAgentMessage(message)) {
           userChannel.sendMessage( Constants.SUCCESS_REGISTRED + "\n");
            allClientsBase.addNewAgent(userChannel, name);
            tryToCreateNewPair();
            logger.log(Level.INFO, "Registered agent " + name);
            // System.out.println("agent");
        }
    }

    public void handleMessagesFromAutorizedUser(AbstractSocket userChannel, String message) {
        if (allClientsBase.doesClientHaveInterlocutor(userChannel)){
            try {
                sendMessageToInterlocutorOf(userChannel, message);
            } catch (IOException e) {
                logger.log(Level.INFO, "Client " + allClientsBase.getClientNameByChanel(allClientsBase.getClientInterlocutorChannel(userChannel)) + " disconnect");
                handlingClientDisconnecting(allClientsBase.getClientInterlocutorChannel(userChannel));
            }
        } else {
            try {
                sendMessageBackToClient(userChannel, message);
            } catch (IOException e) {
                logger.log(Level.INFO, "Client " + allClientsBase.getClientNameByChanel(userChannel) + " disconnect");
                handlingClientDisconnecting(userChannel);
            }
        }
    }

    private void sendMessageToInterlocutorOf(AbstractSocket clientChannel, String message) throws IOException {
        if (allClientsBase.doesItsUserChannel(clientChannel)) {
           allClientsBase.getClientInterlocutorChannel(clientChannel).sendMessage( "user: " + message);
        } else
           allClientsBase.getClientInterlocutorChannel(clientChannel).sendMessage( "agent: " + message);
    }

    private void sendMessageBackToClient(AbstractSocket clientChannel, String message) throws IOException {
        if (allClientsBase.doesItsUserChannel(clientChannel)) {
            usersSMSCache.addSMSinCache(clientChannel, message);
            allClientsBase.addUserChannelInWaiting(clientChannel);
            clientChannel.sendMessage( Constants.WAIT_AGENT);
            tryToCreateNewPair();
        } else clientChannel.sendMessage( Constants.WAIT_USER);
    }

    public void handlingClientDisconnecting(AbstractSocket clientChannel) {
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
        AbstractSocket channel = allClientsBase.getClientInterlocutorChannel(clientChannel);
        allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
        allClientsBase.removeUserChanelFromBase(clientChannel);
        try {
           channel.sendMessage( "user disconnected");
        } catch (IOException e) {
            handlingClientDisconnecting(channel);
        }
    }

    private void breakAgentChannelConn(AbstractSocket clientChannel) {
        AbstractSocket channel = allClientsBase.getClientInterlocutorChannel(clientChannel);
        allClientsBase.breakChatBetweenAgentAndUser(clientChannel);
        allClientsBase.removeAgentChanelFromBase(clientChannel);
        try {
           channel.sendMessage( "agent disconnected");
        } catch (IOException e) {
            handlingClientDisconnecting(channel);
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
        Pair<AbstractSocket, AbstractSocket> pair = allClientsBase.createNewPairOfUserAndAgent();
        if (pair != null) {
            String userName = allClientsBase.getClientNameByChanel(pair.getKey());
            String agentName = allClientsBase.getClientNameByChanel(pair.getValue());
            logger.log(Level.INFO, "Created chat between " + userName + " and " + agentName);
            try {
               pair.getKey().sendMessage( "your agent is " + allClientsBase.getClientNameByChanel(pair.getValue()));
            } catch (IOException e) {
                handlingClientDisconnecting(pair.getKey());
            }
            try {
               pair.getValue().sendMessage( "your user is " + allClientsBase.getClientNameByChanel(pair.getKey()) + "\n");
               pair.getValue().sendMessage( "user: " + usersSMSCache.removeCachedSMS(pair.getKey()));
            } catch (IOException e){
                handlingClientDisconnecting(pair.getValue());
            }
        }
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
                   allClientsBase.getClientInterlocutorChannel(clientChannel).sendMessage( "user exit");
                    allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
                    allClientsBase.removeUserChanelFromBase(clientChannel);
                } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
                    logger.log(Level.INFO, "agent " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
                   allClientsBase.getClientInterlocutorChannel(clientChannel).sendMessage( "agent exit");
                    allClientsBase.breakChatBetweenAgentAndUser(clientChannel);
                    allClientsBase.removeAgentChanelFromBase(clientChannel);
                }
            } catch (IOException e) {
                handlingClientDisconnecting(allClientsBase.getClientInterlocutorChannel(clientChannel));
            }
        } else logger.log(Level.INFO, "client " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
    }
}
