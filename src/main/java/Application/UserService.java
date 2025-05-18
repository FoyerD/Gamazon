package Application;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import Application.DTOs.UserDTO;
import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.User.LoginManager;
import Domain.User.Member;
import Domain.User.User;

@Service
public class UserService {
    private LoginManager loginManager;
    private TokenService tokenService;
    private static final  String CLASS_NAME = UserService.class.getName();

    public UserService(LoginManager loginManager, TokenService tokenService) {
        this.loginManager = loginManager;
        this.tokenService = tokenService;
    }


    /***
     * Creates a guest user and returns a token for it.
     * @return {@link Response} of {@link UserDTO} containing the token and guest user name.
     *         If an error occurs, returns an error message.
     */
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


    /***
     * Exits the session of the user associated with the given token.
     * @param sessionToken The token of the user to exit.
     * @return {@link Response} of {@link Void} indicating success or failure.
     */
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

    /***
     * Registers a new user with the given username, password, and email.
     * @param sessionToken The token of the guest user to register.
     * @param username The desired username for the new user.
     * @param password The desired password for the new user.
     * @param email The email address of the new user.
     * @return {@link Response} of {@link UserDTO} containing the token and user name.
     *         If an error occurs, returns an error message.
     */
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


    /***
     * Logs in a user with the given username and password.
     * @param username The username of the user to log in.
     * @param password The password of the user to log in.
     * @return {@link Response} of {@link UserDTO} containing the token and user name.
     *         If an error occurs, returns an error message.
     */
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
