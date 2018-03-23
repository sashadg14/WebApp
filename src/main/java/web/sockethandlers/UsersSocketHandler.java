package web.sockethandlers;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tcp_ip.AllClientsBase;
import utils.Constants;
import tcp_ip.ServerCommunication;
import tcp_ip.channels.WebSocket;

import java.util.Map;

public class UsersSocketHandler extends TextWebSocketHandler {
    @Autowired
    private AllClientsBase allClientsBase/*=AllClientsBase.getInstance()*/;
    @Autowired
    ServerCommunication serverCommunication;

    private Logger logger = Logger.getRootLogger();
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {
        WebSocket webSocket=new WebSocket(session);
        // for(WebSocketSession webSocketSession : sessions) {
        Map value = new Gson().fromJson(message.getPayload(), Map.class);
        System.out.println(message.getPayload());
        if(value.containsKey("name")) {
            String name= (String) value.get("name");
           serverCommunication.handleUserRegistration(webSocket,name);
        }
        else if(allClientsBase.isAutorized(webSocket))
            serverCommunication.handleMessagesFromAutorizedUser(webSocket,value.get("message").toString());
        else webSocket.sendMessage(Constants.ERROR_NEED_REGISTERING);
        //}
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //sessions.remove(session);
        serverCommunication.handlingClientDisconnecting(new WebSocket(session));
        System.out.println("closed user");
    }
}
