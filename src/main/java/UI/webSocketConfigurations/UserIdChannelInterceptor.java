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

    private final ConnectedUserRegistry connectedUsers;

    public UserIdChannelInterceptor(ConnectedUserRegistry connectedUsers) {
        this.connectedUsers = connectedUsers;
    }
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = accessor.getFirstNativeHeader("userId");

            System.out.println("▶ Intercepted CONNECT frame");
            System.out.println(" Headers: " + accessor.toNativeHeaderMap());

            if (userId != null && !userId.isBlank()) {
                StompPrincipal principal = new StompPrincipal(userId);
                accessor.setUser(principal);
                connectedUsers.markConnected(userId);

                System.out.println(" CONNECT userId = " + userId + " | principal = " + principal.getName());
            } else {
                System.out.println(" Missing or blank userId in headers");
            }
        }

        if (StompCommand.DISCONNECT.equals(accessor.getCommand()) && accessor.getUser() != null) {
            String userId = accessor.getUser().getName();
            connectedUsers.markDisconnected(userId);
            System.out.println(" DISCONNECT: userId = " + userId);
        }

        return message;
    }

}
