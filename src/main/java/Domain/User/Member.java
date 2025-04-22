package Domain.User;

import java.util.UUID;

public class Member extends User{
    String password; // encoded
    String email;
    public Member(UUID id, String username, String password, String email) {
        super(id, username);
        this.password = password;
    }

    String getPassword() {
        return password;
    }

    public Member register(String username, String password, String email) {
        throw new UnsupportedOperationException("Member cannot register itself");
    }

    @Override
    public String getEmail() {
        return email;
    }
}
