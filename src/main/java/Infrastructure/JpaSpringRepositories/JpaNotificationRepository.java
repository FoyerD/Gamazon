package Infrastructure.JpaSpringRepositories;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Notification.INotificationRepository;
import Domain.Notification.Notification;

@Repository
@Profile({"prod", "dbtest"})
public class JpaNotificationRepository extends INotificationRepository {

    private final IJpaNotificationRepository jpaNotificationRepository;

    public JpaNotificationRepository(IJpaNotificationRepository jpaNotificationRepository) {
        this.jpaNotificationRepository = jpaNotificationRepository;
    }

    @Override
    public void addNotification(String userId, String content) {
        String notificationId = UUID.randomUUID().toString();
        Notification notification = new Notification(notificationId, userId, content);
        // Create a lock for the notification before saving
        addLock(userId);
        jpaNotificationRepository.save(notification);
    }

    @Override
    public List<String> getNotifications(String userId) {
        return jpaNotificationRepository.getNotificationsByUserId(userId)
                .stream()
                .map(Notification::getContent)
                .toList();
    }

    @Override
    public boolean add(String id, List<String> value) {
        // Not used directly - notifications are added through addNotification
        return false;
    }

    @Override
    public List<String> remove(String id) {
        // Optional: Implement if notification deletion is needed
        return null;
    }

    @Override
    public List<String> get(String id) {
        return getNotifications(id);
    }

    @Override
    public List<String> update(String id, List<String> item) {
        // Not used directly - notifications are immutable
        return null;
    }

    @Override
    public void deleteAll() {
        jpaNotificationRepository.deleteAll();
        deleteAllLocks();
    }
} 