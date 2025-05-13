package Infrastructure;

import org.springframework.stereotype.Service;

import Domain.ExternalServices.INotificationService;


@Service
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
