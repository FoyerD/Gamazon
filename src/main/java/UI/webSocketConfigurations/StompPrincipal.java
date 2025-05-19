package UI.webSocketConfigurations;

import java.security.Principal;

/**
 * A simple implementation of Principal that holds a user ID as its name.
 * Used to associate WebSocket sessions with specific users.
 */
public class StompPrincipal implements Principal {
    private final String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
