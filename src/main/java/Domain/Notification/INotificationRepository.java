package Domain.Notification;

import java.util.List;

import Domain.Repos.ILockbasedRepository;

/**
 * Abstract repository class for managing notifications.
 * Extends the lock-based repository pattern for thread-safe operations on notifications.
 * Uses a String as the unique identifier for notifications.
 */
public abstract class INotificationRepository extends ILockbasedRepository<List<String>, String> {

    /**
     * Adds a notification to the repository.
     * This method should handle the logic for adding a notification to the repository.
     *
     * @param userId  The ID of the user to whom the notification belongs.
     * @param content The content of the notification.
     */
    public abstract void addNotification(String userId, String content);

    /**
     * Retrieves notifications for a specific user.
     * This method should handle the logic for retrieving notifications from the repository.
     *
     * @param userId The ID of the user whose notifications are to be retrieved.
     * @return A list of notifications for the specified user.
     */
    public abstract List<String> getNotifications(String userId);
}
