package UI.webSocketConfigurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final UserIdChannelInterceptor userIdInterceptor;

    public WebSocketConfig(UserIdChannelInterceptor userIdInterceptor) {
        this.userIdInterceptor = userIdInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint for WebSocket handshake (with SockJS fallback)
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
        // Enable simple in-memory message broker for /topic
        registry.enableSimpleBroker("/topic");
        // Prefix for messages sent from client to server
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register our userId-binding interceptor
        registration.interceptors(userIdInterceptor);
    }
}
