package Domain.User;
import java.util.UUID;
import Domain.Shopping.IShoppingCart;

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

    public void setToken(UUID token) {
        
    }

    public void visitExit(LoginManager loginManager) {
        this.isLoggedIn = false;
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
