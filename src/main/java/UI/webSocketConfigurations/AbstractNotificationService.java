package UI.webSocketConfigurations;

import Application.utils.Response;
import Application.utils.Error;
import Domain.ExternalServices.INotificationService;

public abstract class AbstractNotificationService implements INotificationService {

    protected final WebSocketNotifier webSocketNotifier;
    protected final ConnectedUserRegistry connectedUserRegistry;

    protected AbstractNotificationService(WebSocketNotifier notifier, ConnectedUserRegistry registry) {
        this.webSocketNotifier = notifier;
        this.connectedUserRegistry = registry;
    }

    @Override
    public Response<Boolean> sendNotification(String userId, String content) {
        if (!connectedUserRegistry.isConnected(userId)) {
            storeUndelivered(userId, content);
            return new Response<>(false);
        }

        try {
            webSocketNotifier.notifyUser(userId, content);
            return new Response<>(true);
        } catch (Exception e) {
            storeUndelivered(userId, content);
            return new Response<>(new Error("WebSocket notification failed: " + e.getMessage()));
        }
    }

    protected abstract void storeUndelivered(String userId, String content);
}
