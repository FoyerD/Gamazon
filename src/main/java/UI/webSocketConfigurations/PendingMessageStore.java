package UI.webSocketConfigurations;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PendingMessageStore {

    private final Map<String, List<String>> userMessages = new ConcurrentHashMap<>();

    public void store(String userId, String message) {
        userMessages.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
    }

    public List<String> consume(String userId) {
        List<String> messages = userMessages.getOrDefault(userId, List.of());
        userMessages.remove(userId);
        return messages;
    }

    public boolean hasMessages(String userId) {
        return userMessages.containsKey(userId);
    }
}
