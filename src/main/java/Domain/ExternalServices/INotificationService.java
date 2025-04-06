package Domain.ExternalServices;

public interface INotificationService {

    void sendNotification(String name, String string, String deliveryAddress);

}
