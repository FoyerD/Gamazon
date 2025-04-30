import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import Application.UserService;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import Domain.User.LoginManager;
import Domain.TokenService;
import Infrastructure.MemoryUserRepository;

public class UserServiceTests {
    private UserService userService;
    private String guestToken;

    @Before
    public void setUp() {
        // Initialize service and obtain a guest session token
        userService = new UserService(new LoginManager(new MemoryUserRepository()), new TokenService());
        Response<UserDTO> guestResp = userService.guestEntry();
        assertFalse("Guest entry should succeed", guestResp.errorOccurred());
        guestToken = guestResp.getValue().getSessionToken();

    }

    // 1.1 Guest Entry
    @Test
    public void GivenNoUser_WhenGuestEntry_ThenGuestCreated() {
        Response<UserDTO> response = userService.guestEntry();
        assertFalse("Second guest entry should succeed until limit", response.errorOccurred());
        assertEquals("Guest", response.getValue().getUsername());
        assertNotNull("Session token must be provided", response.getValue().getSessionToken());
    }

    // 1.2 Exit
    @Test
    public void GivenValidToken_WhenExit_ThenSessionEnded() {
        Response<Void> response = userService.exit(guestToken);
        assertFalse("Exit with valid token should succeed", response.errorOccurred());
    }

    @Test
    public void GivenInvalidToken_WhenExit_ThenErrorInvalidToken() {
        Response<Void> response = userService.exit("invalid-token");
        assertTrue("Exit with bad token should fail", response.errorOccurred());
        assertEquals("Invalid token", response.getErrorMessage());
    }

    // 1.3 Register
    @Test
    public void GivenGuestSession_WhenRegister_ThenUserRegistered() {
        Response<UserDTO> response = userService.register(
            guestToken, "alice", "Password1!", "alice@example.com");
        

        assertFalse("Registration should succeed", response.errorOccurred());
        assertEquals("alice", response.getValue().getUsername());
        assertEquals("alice@example.com", response.getValue().getEmail());
    }

    @Test
    public void GivenDuplicateUsername_WhenRegister_ThenErrorUserExists() {
        userService.register(guestToken, "bob", "Password1!", "bob@mail.com");
        Response<UserDTO> secondGuest = userService.guestEntry();

        assertFalse(secondGuest.errorOccurred());

        Response<UserDTO> second = userService.register(
            secondGuest.getValue().getSessionToken(), "bob", "Password1!", "bob@mail.com");

        assertTrue("Duplicate registration should fail", second.errorOccurred());
        assertTrue(second.getErrorMessage(), second.getErrorMessage().contains("Username already exists"));
    }

    // 1.4 Login
    @Test
    public void GivenRegisteredUser_WhenLogin_ThenAuthenticated() {
        Response<UserDTO> regResp = userService.register(guestToken, "carol", "Password1!", "carol@mail.com");
        userService.exit(regResp.getValue().getSessionToken());
        Response<UserDTO> loginResp = userService.login("carol", "Password1!");

        assertFalse("Login should succeed", loginResp.errorOccurred());
        assertEquals("carol", loginResp.getValue().getUsername());
        assertNotNull("Login session token should be set", loginResp.getValue().getSessionToken());
    }

    @Test
    public void GivenWrongCredentials_WhenLogin_ThenErrorInvalidCredentials() {
        Response<UserDTO> regResp = userService.register(guestToken, "dave", "Password1!", "dave@mail.com");
        userService.exit(regResp.getValue().getSessionToken());
        Response<UserDTO> loginBad = userService.login("dave", "Password1"); //no ! here
        assertTrue("Login with wrong credentials should fail", loginBad.errorOccurred());
        assertTrue(loginBad.getErrorMessage().contains("Invalid username or password"));
    }
}
