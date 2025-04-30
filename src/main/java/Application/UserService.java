package Application;
import java.util.NoSuchElementException;

import Application.DTOs.UserDTO;
import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.TokenService;
import Domain.User.*;

public class UserService {
    private LoginManager loginManager;
    private TokenService tokenService;
    private static final  String CLASS_NAME = UserService.class.getName();

    public UserService(LoginManager loginManager, TokenService tokenService) {
        this.loginManager = loginManager;
        this.tokenService = tokenService;
    }

    public Response<UserDTO> guestEntry() {
        try {
            User guest = loginManager.createGuest();
            String token = tokenService.generateToken(guest.getId());
            UserDTO guestDto = new UserDTO(token, guest.getName());
            TradingLogger.logEvent(CLASS_NAME, "guestEntry", "New Guest has entered.");
    
            return Response.success(guestDto);
        } catch (IllegalStateException e) {
            String msg = "Failed to create guest user: " + e.getMessage();
            TradingLogger.logError(CLASS_NAME, "guestEntry", msg);
            return Response.error("Couldn't enter as guest. Please try again");
        }
        
    }

    public Response<Void> exit(String sessionToken) {
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "exit", "received invalid session token.", sessionToken);
            return Response.error("Invalid token");
        }

        String id = tokenService.extractId(sessionToken);
        try {
            User user = loginManager.exit(id);
            TradingLogger.logEvent(CLASS_NAME, "exit", user.getName() + " has exited.");
        } catch (NoSuchElementException e) {
            TradingLogger.logError(CLASS_NAME, "exit", "Couldn't find user", id);
            return Response.error("User not found");
        }
        return Response.success(null);
    }

    public Response<UserDTO> register(String sessionToken, String username, String password, String email) {
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "register", "Received invalid session token", sessionToken);
            return Response.error("Invalid token");
        }

        String id = tokenService.extractId(sessionToken);

        try {
            Member member = loginManager.register(id, username, password, email);
            TradingLogger.logEvent(CLASS_NAME, "register", "Guest has registed as " + username +".");
            return Response.success(new UserDTO(sessionToken, member.getName(), email));
        } catch (IllegalStateException e) {
            TradingLogger.logError(CLASS_NAME, "register", "Failed to register " + username + ": " + e.getMessage());
            return Response.error("Failed to register " + username + ": " + e.getMessage());
        }
        catch (IllegalArgumentException | NoSuchElementException e) {
            TradingLogger.logError(CLASS_NAME, "register", "Failed to register " + username + ": " + e.getMessage());
            return Response.error(e.getMessage());
        }
        catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "register", "Failed to register " + username + ": " + e.getMessage());
            return Response.error("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Response<UserDTO> login(String username, String password) {
        try {
            Member member = loginManager.login(username, password);
            String token = tokenService.generateToken(member.getId());
            TradingLogger.logEvent(CLASS_NAME, "login", username + " has logged in.");
            return Response.success(new UserDTO(token, member.getName(), member.getEmail()));
        } catch (IllegalArgumentException | NoSuchElementException e) {
            TradingLogger.logError(CLASS_NAME, "login", "Attempted login has failed. Username: " + username + " Password: " + password, e.getMessage());
            return Response.error("Invalid username or password: " + e.getMessage());
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "login", "Attempted login has failed. Username: " + username + " Password: " + password, e.getMessage());
            return Response.error("An unexpected error occurred: " + e.getMessage());
        }
    }
}
