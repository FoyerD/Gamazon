package Infrastructure;

import Application.utils.Notifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotifier implements Notifier {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyUser(String userId, String message) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, message);
    }
}
