package Domain.User;

import java.util.UUID;

import jakarta.persistence.Entity;

@Entity
public class Member extends User{
    String password; // encoded
    String email;

    /**
     * Constructor for creating a Member object with a specified ID.
     * @param id The unique identifier for the member.
     * @param username The username of the member.
     * @param password The password of the member (encoded).
     * @param email The email address of the member.
     */
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
