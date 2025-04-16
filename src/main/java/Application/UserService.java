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
        User guest = loginManager.createGuest();
        String token = tokenService.generateToken(guest.getId());
        UserDTO user = new UserDTO(guest.getName(), token);

        return new Response<>(user);
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

    public Response<UserDTO> register(String username, String password) {
        // Logic to register a new user
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Response<UserDTO> login(String username, String password) {
        // Logic to authenticate a user
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
