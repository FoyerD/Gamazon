package Application.DTOs;

public class UserDTO {
    
    private final String id;
    private final String username;
    private final String sessionToken;
    private final String email;

    public UserDTO(String sessionToken, String id, String username) {
        this.id = id;
        this.username = username;
        this.sessionToken = sessionToken;
        this.email = ""; 
    }

    public UserDTO(String sessionToken, String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.sessionToken = sessionToken;
        this.email = email;
    }

    

    public String getUsername() {
        return username;
    }


    public String getSessionToken() {
        return sessionToken;
    }

    public String getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }

}
