package Domain.Notification;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a notification in the system.
 * This entity stores notifications for users.
 */
@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    private String id;
    private String userId;
    private String content;

    protected Notification() {
        // Required by JPA
    }

    /**
     * Creates a new notification.
     *
     * @param id      The unique identifier for the notification
     * @param userId  The ID of the user to whom the notification belongs
     * @param content The content of the notification
     */
    public Notification(String id, String userId, String content) {
        this.id = id;
        this.userId = userId;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
} 