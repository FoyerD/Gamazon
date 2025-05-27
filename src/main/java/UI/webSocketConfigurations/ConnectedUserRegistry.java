package UI.webSocketConfigurations;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Application.utils.TradingLogger;

@Component
public class ConnectedUserRegistry {

    private final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();

    public void markConnected(String userId) {
        TradingLogger.logEvent("ConnectedUserRegistry", "markConnected",
            "DEBUG: Marked userId=" + userId + " as connected");
        connectedUsers.add(userId);
    }

    public void markDisconnected(String userId) {
        TradingLogger.logEvent("ConnectedUserRegistry", "markDisconnected",
            "DEBUG: Marked userId=" + userId + " as disconnected");
        connectedUsers.remove(userId);
    }

    public boolean isConnected(String userId) {
        boolean status = connectedUsers.contains(userId);
        TradingLogger.logEvent("ConnectedUserRegistry", "isConnected",
            "DEBUG: Checked connection for userId=" + userId + ": " + status);
        return status;
    }
}
