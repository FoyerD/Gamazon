package Infrastructure;

import Domain.ExternalServices.INotificationService;

public class NotificationService implements INotificationService {
    
    @Override
    public void sendNotification(String name, String string, String deliveryAddress) {
        // Implementation for sending notification with additional parameters
        System.out.println("Notification sent to " + name + ": " + string + " at " + deliveryAddress);
    }

    public void initialize() {
        // Initialization logic for the notification service
        System.out.println("Notification service initialized.");
    }
}
