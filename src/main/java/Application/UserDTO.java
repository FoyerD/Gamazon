package Application;

public class UserDTO {
    
    private String username;
    private String sessionToken;

    public UserDTO(String username, String sessionToken) {
        this.username = username;
        this.sessionToken = sessionToken;
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
}
