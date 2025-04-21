import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import Application.UserService;
import Domain.User.IUserRepository;
import Domain.User.LoginManager;
import Application.Response;
import Application.UserDTO;
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

    @Test
    public void GivenGuestAlreadyLogged_WhenGuestEntryAgain_ThenErrorShown() {
        // First call already in setUp; second call should fail
        Response<UserDTO> second = userService.guestEntry();
        assertTrue("Expected error on duplicate guest entry", second.errorOccurred());
        assertNull("No DTO on error", second.getValue());
        assertTrue(second.getErrorMessage().contains("Failed to create guest user"));
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
            guestToken, "alice", "password", "alice@example.com");
        assertFalse("Registration should succeed", response.errorOccurred());
        assertEquals("alice", response.getValue().getUsername());
        assertEquals("alice@example.com", response.getValue().getEmail());
    }

    @Test
    public void GivenDuplicateUsername_WhenRegister_ThenErrorUserExists() {
        userService.register(guestToken, "bob", "pass", "bob@mail.com");
        Response<UserDTO> second = userService.register(
            guestToken, "bob", "pass", "bob@mail.com");
        assertTrue("Duplicate registration should fail", second.errorOccurred());
        assertTrue(second.getErrorMessage().contains("User already exists"));
    }

    // 1.4 Login
    @Test
    public void GivenRegisteredUser_WhenLogin_ThenAuthenticated() {
        userService.register(guestToken, "carol", "1234", "carol@mail.com");
        Response<UserDTO> loginResp = userService.login("carol", "1234");
        assertFalse("Login should succeed", loginResp.errorOccurred());
        assertEquals("carol", loginResp.getValue().getUsername());
        assertNotNull("Login session token should be set", loginResp.getValue().getSessionToken());
    }

    @Test
    public void GivenWrongCredentials_WhenLogin_ThenErrorInvalidCredentials() {
        Response<UserDTO> loginBad = userService.login("dave", "wrong");
        assertTrue("Login with wrong credentials should fail", loginBad.errorOccurred());
        assertTrue(loginBad.getErrorMessage().contains("Invalid username or password"));
    }
}
