package Domain.User;

import java.util.UUID;

public class Member extends User{
    String password; // encoded
    String email;
    Integer age; // optional, can be null

    /**
     * Constructor for creating a Member object with a specified ID.
     * @param id The unique identifier for the member.
     * @param username The username of the member.
     * @param password The password of the member (encoded).
     * @param email The email address of the member.
     */
    public Member(UUID id, String username, String password, String email, Integer age) {
        super(id, username);
        this.password = password;
        this.email = email;
        this.age = age;

    }

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
        this.age = 20; // default age

    }

    String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }
}
