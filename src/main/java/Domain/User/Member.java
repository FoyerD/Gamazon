package Domain.User;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
public class Member extends User {

    private String password; // encoded
    private String email;
    private LocalDate birthDate; // optional

    protected Member() {
        super(); // JPA requires a no-arg constructor
    // only for JPA
    }

    public Member(UUID id, String username, String password, String email, LocalDate birthDate) {
        super(id, username);
        this.password = password;
        this.email = email;
        this.birthDate = birthDate;
    }

    public Member(UUID id, String username, String password, String email) {
        this(id, username, password, email, getDefaultBirthDateFor20YearsOld());
    }


    String getPassword() { return password; }
    public String getEmail() { return email; }
    public LocalDate getBirthDate() { return birthDate; }

    public Integer getAge() {
        if (birthDate == null) return null;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private static LocalDate getDefaultBirthDateFor20YearsOld() {

    return LocalDate.now().minusYears(20);
}
}
