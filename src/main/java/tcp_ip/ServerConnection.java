package tcp_ip;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import tcp_ip.channels.AbstractSocket;
import tcp_ip.channels.SChannel;
import utils.Constants;
import utils.MessagesUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Alex on 14.02.2018.
 */
public class ServerConnection {
    private int port = 19000;
    private Selector selector;
    private ServerSocketChannel serverAbstractSocket;

    @Autowired
    private AllClientsBase allClientsBase;
    private MessagesUtils mUtils = new MessagesUtils();
    private Logger logger = Logger.getRootLogger();
    @Autowired
    private ServerCommunication serverCommunication;

    public ServerConnection() {
        new Thread(()->{
            try {
                createConnection();
            } catch (IOException e) {
                Logger.getRootLogger().log(Level.TRACE,e);
                return;
            }
            System.out.println("Server started\n");
            try {
                listenConnection();
            } catch (IOException e) {
                Logger.getRootLogger().log(Level.TRACE,e);
                System.out.println("Error in server work");
            }
        }).start();
    }

    public void createConnection() throws IOException {
        selector = Selector.open();
        serverAbstractSocket = ServerSocketChannel.open();
        serverAbstractSocket.configureBlocking(false);
        serverAbstractSocket.socket().bind(new InetSocketAddress(port));
        serverAbstractSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void listenConnection() throws IOException {
        while (true) {
            if (selector.select() > 0) {
                handleSet(selector.selectedKeys());
            }
          /*  System.out.println("s");
            serverCommunication.tryToCreateNewPair();*/

        }
    }

    private void handleSet(Set<SelectionKey> set) {
        Iterator<SelectionKey> keySetIterator = set.iterator();
        while (keySetIterator.hasNext()) {
            SelectionKey key = keySetIterator.next();
            switch (key.readyOps()) {
                case SelectionKey.OP_ACCEPT:
                        acceptNewConnection();
                    break;
                case SelectionKey.OP_READ:
                    String message = "";
                    try {
                        message = readMessage(key);
                    } catch (IOException e) {
                        logger.log(Level.INFO, "Client " + allClientsBase.getClientNameByChanel((SocketChannel) key.channel()) + " disconnect");
                        serverCommunication.handlingClientDisconnecting(new SChannel((SocketChannel) key.channel()));
                        break;
                    }
                    messageHandle(key, message.trim());
                    break;
            }
            keySetIterator.remove();
        }
    }


    private void acceptNewConnection() {
        SocketChannel socketChannel = null;
        try {
             socketChannel= serverAbstractSocket.accept();
            logger.log(Level.INFO, "Connected new client " + socketChannel.getLocalAddress());
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e){
            try {
                logger.log(Level.INFO, "Error in new client connection");
                if (socketChannel != null) {
                    socketChannel.close();
                }
            } catch (IOException ignored) {

            }
        }
    }

    private void messageHandle(SelectionKey key, String message) {
        SChannel clientChannel = new SChannel((SocketChannel) key.channel());
        //final String type=;
        switch (mUtils.getMessageType(message.trim())) {
            case Constants.MESSAGE_TYPE_REGISTER:
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    serverCommunication.handleRegistration(clientChannel, message);
                } catch (IOException e) {
                    serverCommunication.handlingClientDisconnecting(clientChannel);
                }
                break;
            case Constants.MESSAGE_TYPE_SMS:
                if (!allClientsBase.isAutorized(clientChannel)) {
                    try {
                        sendMessageToClient(clientChannel, Constants.ERROR_NEED_REGISTERING);
                    } catch (IOException e) {
                        serverCommunication.handlingClientDisconnecting(clientChannel);
                    }
                } else
                    serverCommunication.handleMessagesFromAutorizedUser(clientChannel, message);
                break;
            case Constants.MESSAGE_TYPE_EXIT:
                serverCommunication.handleClientExit(clientChannel);
                break;
            case Constants.MESSAGE_TYPE_LEAVE:
                if (allClientsBase.doesClientHaveInterlocutor(clientChannel) && allClientsBase.doesItsUserChannel(clientChannel)) {
                    try {
                        sendMessageToClient(allClientsBase.getUserInterlocutorChannel(clientChannel), "{ \"disconnected\":" + allClientsBase.getClientByChannel(clientChannel).getId() + "}");
                    } catch (IOException e) {
                        serverCommunication.handlingClientDisconnecting(allClientsBase.getUserInterlocutorChannel(clientChannel));
                    }
                    String agentName = allClientsBase.getClientNameByChanel(allClientsBase.getUserInterlocutorChannel(clientChannel));
                    allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
                    logger.log(Level.INFO, "user " + allClientsBase.getClientNameByChanel(clientChannel) + " leave from dialog with " + agentName);
                }
                break;
        }
    }

    public void sendMessageToClient(AbstractSocket channel, String message) throws IOException {
       channel.sendMessage(message);
    }

    private String readMessage(SelectionKey key) throws IOException {
        SocketChannel sChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1);
        StringBuilder stringBuilder=new StringBuilder();
        while (true) {
            int bytesCount = sChannel.read(buffer);
            if (bytesCount > 0) {
                stringBuilder.append(new String(buffer.array()));
                buffer.flip();
            } else break;
        }
        //System.err.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

}
