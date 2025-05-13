package Domain.ExternalServices;

import Application.utils.Response;

public interface INotificationService {

    Response<Boolean> sendNotification(String name, String content);

    void initialize();

}
