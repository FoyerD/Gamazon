package Domain.User;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
public class Member extends User {

    private String password; // encoded
    private String email;
    private Integer age; // optional

    protected Member() {
        super(); // JPA requires a no-arg constructor
    // only for JPA
    }

    public Member(UUID id, String username, String password, String email, Integer age) {
        super(id, username);
        this.password = password;
        this.email = email;
        this.age = age;
    }

    public Member(UUID id, String username, String password, String email) {
        this(id, username, password, email, 20);
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
