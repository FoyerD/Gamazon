package UI.webSocketConfigurations;

import java.security.Principal;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import Application.utils.TradingLogger;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private ChannelInterceptor userIdInterceptor;
    
    public WebSocketConfig(ChannelInterceptor channelInterceptor){
        this.userIdInterceptor = channelInterceptor;
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(new UserHandshakeInterceptor()) 
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(
                        ServerHttpRequest request,
                        WebSocketHandler wsHandler,
                        Map<String, Object> attributes
                    ) {
                        Principal user = (Principal) attributes.get("user");
                        System.out.println(" determineUser sees: " + (user != null ? user.getName() : "null"));
                        return user;
                    }
                })
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");      // for sending from client
        registry.setUserDestinationPrefix("/user");              // for sending to users
        registry.enableSimpleBroker("/topic", "/queue");         // basic broker
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register our userId-binding interceptor
        TradingLogger.logEvent("WebScoketConfig.java", "configureClientInboundChannel", "DEBUG: WebScoketConfig configures the registration.");
        registration.interceptors(userIdInterceptor);
    }
}
