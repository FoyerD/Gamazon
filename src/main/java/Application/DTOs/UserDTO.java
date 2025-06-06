package Application.DTOs;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.cglib.core.Local;

import Domain.User.Member;

public class UserDTO {
    
    private final String id;
    private final String username;
    private final String sessionToken;
    private final String email;
    private final LocalDate birthDate;

    public UserDTO(String sessionToken, String id, String username) {
        this.id = id;
        this.username = username;
        this.sessionToken = sessionToken;
        this.email = ""; 
        this.birthDate = LocalDate.of(1970, 1, 1);
    }

    public UserDTO(Member member) {
        this.sessionToken = "";
        this.id = member.getId();
        this.username = member.getName();
        this.email = member.getEmail();
        this.birthDate = member.getBirthDate();
    }

    public UserDTO(String sessionToken, Member member) {
        this.id = member.getId();
        this.username = member.getName();
        this.email = member.getEmail();
        this.birthDate = member.getBirthDate();
        this.sessionToken = sessionToken;
    }

    

    public UserDTO(String sessionToken, String id, String username, String email, LocalDate birthDate) {
        this.id = id;
        this.username = username;
        this.sessionToken = sessionToken;
        this.email = email;
        this.birthDate = birthDate;
    }



    public String getUsername() { return username; }
    public String getSessionToken() { return sessionToken; }
    public String getId() { return id; }
    public String getEmail() { return email; }
    public LocalDate getBirthDate() { return birthDate; }

    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

}
