package Domain.User;
import java.util.UUID;

public abstract class User {

    private UUID id;
    private String username;

    boolean isLoggedIn;

    public User(String username) {
        this.id = UUID.randomUUID();
        this.username = username;
        isLoggedIn = true;
    }

    public String getName() {
        return username;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getId() {
        return id.toString();
    }

    public void visitExit(LoginManager loginManager) {
        this.isLoggedIn = false;
    }
}
