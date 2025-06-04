package Domain.User;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "app_user") 
public abstract class User {
    
    @Id
    protected UUID id;
    private String username;

    boolean isLoggedIn;

    protected User() {
        // JPA requires a no-arg constructor
    }

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
