package web;

import org.springframework.context.annotation.Bean;
import tcp_ip.AllClientsBase;
import tcp_ip.ServerCommunication;
import tcp_ip.ServerConnection;
import web.sockethandlers.AgentSocketHandler;
import web.sockethandlers.UsersSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public ServerCommunication serverCommunication(){
        return new ServerCommunication();
    }

    @Bean
    public AllClientsBase allClientsBase(){
        return new AllClientsBase();
    }

    @Bean
    public ServerConnection serverConnection(){
        return new ServerConnection();
    }

    @Bean
    public UsersSocketHandler usersSocketHandler(){
        return new UsersSocketHandler();
    }

    @Bean
    public AgentSocketHandler agentSocketHandler(){
        return new AgentSocketHandler();
    }


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(usersSocketHandler(), "/usersocket");
        registry.addHandler(agentSocketHandler(), "/agentsocket");
    }
}