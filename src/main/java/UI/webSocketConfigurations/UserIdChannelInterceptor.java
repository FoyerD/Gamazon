package UI.webSocketConfigurations;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * Intercepts STOMP CONNECT frames to extract the userId from headers
 * and bind it as a StompPrincipal to the WebSocket session.
 */
@Component
public class UserIdChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = accessor.getFirstNativeHeader("userId");

            if (userId != null && !userId.isBlank()) {
                accessor.setUser(new StompPrincipal(userId));
            }
        }

        return message;
    }
}
