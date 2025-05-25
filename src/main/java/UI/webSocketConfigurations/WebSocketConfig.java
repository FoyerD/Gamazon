package UI.webSocketConfigurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import Application.utils.TradingLogger;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final UserIdChannelInterceptor userIdInterceptor;

    public WebSocketConfig(UserIdChannelInterceptor userIdInterceptor) {
        this.userIdInterceptor = userIdInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
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
