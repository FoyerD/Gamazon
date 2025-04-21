package Domain.User;
import java.util.UUID;
import Domain.Shopping.IShoppingCart;

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

    public void logout() {
        this.isLoggedIn = false;
    }

    public String getId() {
        return id.toString();
    }

    public abstract Member register(String username, String password, String email);

    public void visitExit(LoginManager loginManager) {
        this.logout();
    }


    public IShoppingCart getUserShoppingCart() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserShoppingCart'");
    }

    public void removeUserCart() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeUserCart'");
    }

    public String getEmail() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEmail'");
    }

}
