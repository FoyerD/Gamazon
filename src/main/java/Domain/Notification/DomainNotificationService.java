package Domain.Notification;

import org.springframework.stereotype.Component;

import Application.utils.TradingLogger;
import UI.webSocketConfigurations.AbstractNotificationService;
import UI.webSocketConfigurations.ConnectedUserRegistry;
import UI.webSocketConfigurations.WebSocketNotifier;

@Component
public class DomainNotificationService extends AbstractNotificationService {

    private final INotificationRepository repo;

    public DomainNotificationService(WebSocketNotifier notifier,
                                     ConnectedUserRegistry registry,
                                     INotificationRepository repo) {
        super(notifier, registry);
        this.repo = repo;
    }

    @Override
    protected void storeUndelivered(String userId, String content) {
        TradingLogger.logEvent("DomainNotificationService", "storeUndelivered",
            "DEBUG: Storing undelivered notification for userId=" + userId + " with content: " + content);

        repo.addNotification(userId, content);
    }
}
