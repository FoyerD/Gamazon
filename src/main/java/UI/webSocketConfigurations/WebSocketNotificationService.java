package UI.webSocketConfigurations;

import Application.utils.Error;
import Application.utils.Response;
import Domain.ExternalServices.INotificationService;
import org.springframework.stereotype.Component;

@Component
public class WebSocketNotificationService implements INotificationService {

    private final WebSocketNotifier webSocketNotifier;
    private final ConnectedUserRegistry connectedUserRegistry;

    public WebSocketNotificationService(WebSocketNotifier webSocketNotifier,
                                        ConnectedUserRegistry connectedUserRegistry) {
        this.webSocketNotifier = webSocketNotifier;
        this.connectedUserRegistry = connectedUserRegistry;
    }

    @Override
    public Response<Boolean> sendNotification(String userId, String content) {
        if (!connectedUserRegistry.isConnected(userId)) {
            return new Response<>(false); // User not connected
        }

        try {
            webSocketNotifier.notifyUser(userId, content);
            return new Response<>(true);
        } catch (Exception e) {
            return new Response<>(new Error("WebSocket notification failed: " + e.getMessage()));
        }
    }

}
