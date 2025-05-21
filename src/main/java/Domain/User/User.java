package Domain.User;
import java.util.UUID;
import jakarta.persistence.MappedSuperclass;


@MappedSuperclass
public abstract class User {

    protected UUID id;
    private String username;

    boolean isLoggedIn;

    public User(String username) {
        this.id = UUID.randomUUID();
        this.username = username;
        isLoggedIn = true;
    }

    public User(UUID id, String username) {
        this.id = id;
        this.username = username;
        isLoggedIn = true;
    }

    public String getName() {
        return username;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void login() {
        this.isLoggedIn = true;
    }

    public void logout(LoginManager loginManager) {
        this.isLoggedIn = false;
        
    }

    public String getId() {
        return id.toString();
    }
}
