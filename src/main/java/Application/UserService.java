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

    public Response<UserDTO> register(String sessionToken, String username, String password, String email) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }

        String id = tokenService.extractId(sessionToken);

        try {
            Member member = loginManager.register(id, username, password, email);
            
            return Response.success(new UserDTO(member.getName(), sessionToken, email));
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
        try {
            Member member = loginManager.login(username, password);
            String token = tokenService.generateToken(member.getId());
            return Response.success(new UserDTO(member.getName(), token));
        } catch (IllegalArgumentException e) {
            return Response.error("Invalid username or password: " + e.getMessage());
        } catch (NoSuchElementException e) {
            return Response.error("User not found: " + e.getMessage());
        } catch (Exception e) {
            return Response.error("An unexpected error occurred: " + e.getMessage());
        }
    }
}
