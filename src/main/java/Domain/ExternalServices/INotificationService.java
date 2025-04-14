package Domain.ExternalServices;

public interface INotificationService {

    void sendNotification(String name, String content);

    void initialize();

}
