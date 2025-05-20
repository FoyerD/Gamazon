package UI.webSocketConfigurations;

import Application.utils.Error;
import Application.utils.Response;
import Domain.ExternalServices.INotificationService;
import org.springframework.stereotype.Component;

@Component
public class WebSocketNotificationService implements INotificationService {

    private final WebSocketNotifier webSocketNotifier;
    private final ConnectedUserRegistry connectedUserRegistry;
    private final PendingMessageStore pendingMessageStore;

    public WebSocketNotificationService(WebSocketNotifier notifier,
                                        ConnectedUserRegistry registry,
                                        PendingMessageStore store) {
        this.webSocketNotifier = notifier;
        this.connectedUserRegistry = registry;
        this.pendingMessageStore = store;
    }

    @Override
    public Response<Boolean> sendNotification(String userId, String content) {
        if (!connectedUserRegistry.isConnected(userId)) {
            pendingMessageStore.store(userId, content);
            return new Response<>(false); // Not delivered now
        }

        try {
            webSocketNotifier.notifyUser(userId, content);
            return new Response<>(true);
        } catch (Exception e) {
            return new Response<>(new Error("WebSocket notification failed: " + e.getMessage()));
        }
    }

}
