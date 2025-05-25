package UI.webSocketConfigurations;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketTestController {
    // to test run stompClient.publish({destination: "/app/ping", body: "test"}); in dev tools console
    @MessageMapping("/ping")
    @SendToUser("/topic/notifications")
    public String handlePing(Principal principal) {
        System.out.println("[WS] Got ping from: " + (principal != null ? principal.getName() : "anonymous"));
        return "Pong from " + (principal != null ? principal.getName() : "anonymous");
    }
}
