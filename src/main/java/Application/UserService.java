package Application;
import java.util.NoSuchElementException;

import Domain.TokenService;
import Domain.User.*;

public class UserService {
    private LoginManager loginManager;
    private TokenService tokenService;

    public UserService(LoginManager loginManager, TokenService tokenService) {
        this.loginManager = loginManager;
        this.tokenService = tokenService;
    }

    public Response<UserDTO> guestEntry() {
        try {
            User guest = loginManager.createGuest();
            String token = tokenService.generateToken(guest.getId());
            UserDTO guestDto = new UserDTO(guest.getName(), token);
    
            return Response.success(guestDto);
        } catch (IllegalStateException e) {
            return Response.error("Failed to create guest user: " + e.getMessage());
        }
        
    }

    public Response<Boolean> isLoggedIn(String username) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Response<Void> exit(String sessionToken) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }

        String id = tokenService.extractId(sessionToken);
        try {
            loginManager.exit(id);
        } catch (NoSuchElementException e) {
            return Response.error("User not found");
        }
        return Response.success(null);
    }

    public Response<UserDTO> register(String sessionToken, String username, String password) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }

        String id = tokenService.extractId(sessionToken);

        
        try {
            loginManager.register(id, username, password); // Validate the session token
            return Response.success(userDto);
        } catch (IllegalStateException e) {
            return Response.error("Failed to register user: " + e.getMessage());
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        catch (NoSuchElementException e) {
            return Response.error(e.getMessage());
        }
        catch (Exception e) {
            return Response.error("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Response<UserDTO> login(String username, String password) {
        // Logic to authenticate a user
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
