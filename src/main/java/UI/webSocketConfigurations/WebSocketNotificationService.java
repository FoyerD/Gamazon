package UI.webSocketConfigurations;

import Application.utils.Response;
import Application.utils.Error;
import Domain.ExternalServices.INotificationService;
import org.springframework.stereotype.Component;

/**
 * WebSocket-based implementation of INotificationService.
 * Sends real-time notifications to users via WebSocket.
 */
@Component
public class WebSocketNotificationService implements INotificationService {

    private final WebSocketNotifier webSocketNotifier;

    public WebSocketNotificationService(WebSocketNotifier webSocketNotifier) {
        this.webSocketNotifier = webSocketNotifier;
    }

    @Override
    public Response<Boolean> sendNotification(String userId, String content) {
        try {
            webSocketNotifier.notifyUser(userId, content);
            return new Response<>(true);
        } catch (Exception e) {
            return new Response<>(new Error("WebSocket notification failed: " + e.getMessage()));
        }
    }

}
