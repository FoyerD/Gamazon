package Application;

import java.util.List;

import org.springframework.stereotype.Service;

import Application.utils.Error;
import Application.utils.Response;
import Domain.Notification.INotificationRepository;
import jakarta.transaction.Transactional;

@Service
public class MessageService {

    private INotificationRepository notificationRepository;

    public MessageService(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Response<List<String>> getNotifications(String userId) {
        try{
        List<String> messages = notificationRepository.getNotifications(userId);
        return new Response<>(messages);
        }
        catch (Exception e){
            return new Response<>(new Error(e.getMessage()));
        }
    }
}
