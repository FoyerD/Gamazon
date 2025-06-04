package Infrastructure.MemoryRepositories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Notification.INotificationRepository;

@Repository
@Profile("dev")
public class MemoryNotificationRepository extends INotificationRepository {

    private final Map<String, List<String>> notifications = new ConcurrentHashMap<>();

    @Override
    public void addNotification(String userId, String content) {
        notifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(content);
    }

    @Override
    public List<String> getNotifications(String userId) {
        // Return and clear the user's notifications
        List<String> userNotifications = notifications.getOrDefault(userId, Collections.emptyList());
        notifications.remove(userId);
        return userNotifications;
    }

    // Implementing base interface methods

    @Override
    public boolean add(String userId, List<String> value) {
        notifications.merge(userId, new ArrayList<>(value), (existing, newOnes) -> {
            existing.addAll(newOnes);
            return existing;
        });
        return true;
    }

    @Override
    public List<String> remove(String userId) {
        return notifications.remove(userId);
    }

    @Override
    public List<String> get(String userId) {
        return notifications.get(userId);
    }

    @Override
    public List<String> update(String userId, List<String> value) {
        notifications.put(userId, new ArrayList<>(value));
        return value;
    }

    @Override
    public void deleteAll() {
        notifications.clear();
        deleteAllLocks();
    }
}
