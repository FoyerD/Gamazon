package UI.webSocketConfigurations;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import Application.utils.TradingLogger;

/**
 * A utility class that sends WebSocket messages to specific users
 * based on userId using Spring's convertAndSendToUser mechanism.
 */
@Component
public class WebSocketNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a message to a specific user's notification topic.
     *
     * @param userId  the user ID to send the message to (must match the Principal name)
     * @param message the message content
     */
    public void notifyUser(String userId, String message) {
        TradingLogger.logEvent("WebSocketNotifier", "notifyUser",
            "DEBUG: Sending to userId = [" + userId + "]");

        messagingTemplate.convertAndSendToUser(userId, "/topic/notifications", message);

        TradingLogger.logEvent("WebSocketNotifier", "notifyUser",
            "DEBUG: Finished sending notification to userId=" + userId);
    }

}
