import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import Application.DTOs.UserDTO;
import Application.UserService;
import Application.utils.Response;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTests {
    @Autowired
    private UserService userService;

    private String guestToken;

    @Before
    public void setUp() {
        // Initialize service and obtain a guest session token
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

    @Test
    public void GivenActiveUsers_WhenLogoutAllUsers_ThenTokensAreInvalidated() {
        // Register a user
        Response<UserDTO> regResp = userService.register(guestToken, "erin", "Password1!", "erin@mail.com");
        String erinToken = regResp.getValue().getSessionToken();

        // Create another guest
        Response<UserDTO> guestResp2 = userService.guestEntry();
        String guestToken2 = guestResp2.getValue().getSessionToken();

        // Now call logoutAllUsers
        Response<Void> logoutAllResp = userService.logOutAllUsers();
        assertFalse("Logout all should succeed", logoutAllResp.errorOccurred());

        // Try to use the tokens (should be invalid now)
        Response<Void> exit1 = userService.exit(erinToken);
        Response<Void> exit2 = userService.exit(guestToken2);

        assertTrue("Error should occur!", exit1.errorOccurred());
        assertTrue(exit1.getErrorMessage().contains("User is not logged in"));

        assertTrue("Error should occur!", exit2.errorOccurred());
        assertTrue(exit2.getErrorMessage(), exit2.getErrorMessage().contains("User not found"));
    }

    @Test
    public void GivenGuestSession_WhenRegisterWithBirthDate_ThenUserRegistered() {
        LocalDate birthDate = LocalDate.of(1995, 5, 15);
        
        Response<UserDTO> response = userService.register(
            guestToken,
            "frank",
            "SecurePass123!",
            "frank@example.com",
            birthDate
        );

        assertFalse("Registration with birth date should succeed", response.errorOccurred());
        assertEquals("frank", response.getValue().getUsername());
        assertEquals("frank@example.com", response.getValue().getEmail());
        assertNotNull("Session token should be returned", response.getValue().getSessionToken());
    }
    
}
