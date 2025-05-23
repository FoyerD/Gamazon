package Application.DTOs;

public class UserDTO {
    
    private String username;
    private String sessionToken;
    private String email;

    public UserDTO(String sessionToken, String username) {
        this.username = username;
        this.sessionToken = sessionToken;
        this.email = ""; 
    }

    public UserDTO(String sessionToken, String username, String email) {
        this.username = username;
        this.sessionToken = sessionToken;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
