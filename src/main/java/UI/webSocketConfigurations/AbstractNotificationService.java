package UI.webSocketConfigurations;

import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;
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
        TradingLogger.logEvent("AbstractNotificationService", "sendNotification",
            "DEBUG: Attempting to send notification to userId=" + userId + " with content: " + content);

        if (!connectedUserRegistry.isConnected(userId)) {
            TradingLogger.logEvent("AbstractNotificationService", "sendNotification",
                "DEBUG: User " + userId + " is not connected. Storing as undelivered.");

            storeUndelivered(userId, content);
            return new Response<>(false);
        }

        try {
            webSocketNotifier.notifyUser(userId, content);
            TradingLogger.logEvent("AbstractNotificationService", "sendNotification",
                "DEBUG: Notification sent successfully to userId=" + userId);
            return new Response<>(true);
        } catch (Exception e) {
            TradingLogger.logError("AbstractNotificationService", "sendNotification",
                "DEBUG: WebSocket notification to userId=%s failed: %s", userId, e.getMessage());

            storeUndelivered(userId, content);
            return new Response<>(new Error("WebSocket notification failed: " + e.getMessage()));
        }
    }

    protected abstract void storeUndelivered(String userId, String content);
}
