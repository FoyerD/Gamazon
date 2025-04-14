package Domain.User;

public abstract class User {
    private String username;

    boolean isLoggedIn;

    public User(String username) {
        this.username = username;
        isLoggedIn = true;
    }

    public String getName() {
        return username;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}
