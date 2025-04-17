package Domain.User;

public class Member extends User{
    String password; // encoded
    public Member(String username, String password) {
        super(username);
        this.password = password;
    }

    boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}
