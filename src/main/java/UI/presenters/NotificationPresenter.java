package UI.presenters;

import java.util.List;

import org.springframework.stereotype.Component;

import Application.MessageService;

@Component
public class NotificationPresenter implements INotificationPresenter {
    private final MessageService messageService;

    public NotificationPresenter(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public List<String> getNotifications(String sessionToken) {
        return messageService.getNotifications(sessionToken).getValue();
    }
    
}
