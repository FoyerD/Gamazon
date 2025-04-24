package Domain.User;

import java.util.UUID;

public class Member extends User{
    String password; // encoded
    String email;
    public Member(UUID id, String username, String password, String email) {
        super(id, username);
        this.password = password;
        this.email = email;
    }

    String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
