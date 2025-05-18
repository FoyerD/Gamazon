package Infrastructure;

import Application.utils.Response;
import org.springframework.stereotype.Service;
import Domain.ExternalServices.INotificationService;


@Service
public class NotificationService implements INotificationService {
    
    @Override
    public Response<Boolean> sendNotification(String name, String content) {
        // Implementation for sending notification with additional parameters
        System.out.println("Notification sent to " + name + ": " + content);
        return new Response<Boolean>(true);
    }

    public void initialize() {
        // Initialization logic for the notification service
        System.out.println("Notification service initialized.");
    }
}
