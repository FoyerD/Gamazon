package Infrastructure;

import Domain.ExternalServices.INotificationService;

public class NotificationService implements INotificationService {
    
    @Override
    public void sendNotification(String name, String content) {
        // Implementation for sending notification with additional parameters
        System.out.println("Notification sent to " + name + ": " + content);
    }

    public void initialize() {
        // Initialization logic for the notification service
        System.out.println("Notification service initialized.");
    }
}
