package UI.webSocketConfigurations;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import Application.utils.TradingLogger;

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
            System.out.println(" CONNECT userId header: " + userId);

            if (userId != null && !userId.isBlank()) {
                Principal principal = new StompPrincipal(userId);
                accessor.setUser(principal);
                accessor.getSessionAttributes().put("user", principal);
                connectedUsers.markConnected(userId);
                System.out.println(" Bound principal: " + principal.getName());
            } else {
                System.out.println(" Missing userId header in CONNECT");
            }
        }
        if (StompCommand.SEND.equals(accessor.getCommand()) && accessor.getUser() == null) {
            Principal stored = (Principal) accessor.getSessionAttributes().get("user");
            if (stored != null) {
                accessor.setUser(stored);
                System.out.println(" Restored principal from session attributes: " + stored.getName());
            }
        }

        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            Principal user = accessor.getUser();
            if (user != null) {
                String userId = user.getName();
                connectedUsers.markDisconnected(userId); 

                TradingLogger.logEvent("UserIdChannelInterceptor", "preSend",
                    "🔌 DISCONNECT userId = " + userId);
            }
        }


        return message;
    }

}
