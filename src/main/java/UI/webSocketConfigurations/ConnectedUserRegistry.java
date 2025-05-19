package UI.webSocketConfigurations;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ConnectedUserRegistry {

    private final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();

    public void markConnected(String userId) {
        connectedUsers.add(userId);
    }

    public void markDisconnected(String userId) {
        connectedUsers.remove(userId);
    }

    public boolean isConnected(String userId) {
        return connectedUsers.contains(userId);
    }
}
