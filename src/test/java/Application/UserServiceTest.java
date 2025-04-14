package Application;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import Application.Response;
import Application.UserDTO;
import Application.UserService;
import Domain.User.IUserRepository;
import Domain.User.Member;
import Domain.User.User;

public class UserServiceTest {

    private IUserRepository mockRepository;
    private UserService userService;
    
    @Before
    public void setUp() {
        // Create a mock repository (IUserRepository)
        mockRepository = mock(IUserRepository.class);
        userService = new UserService(mockRepository);
    }
    
    // =======================
    // 1. Guest Enter (כניסה לשוק כמבקר אורח)
    // =======================
    
    @Test
    public void testGuestEnter_Positive() {
        // Assume that a successful guest enter returns a Response<UserDTO> with:
        // - username "Guest" 
        // - non-null session token
        Response<UserDTO> response = userService.guestEnter();
        assertNotNull("Response should not be null", response);
        // A successful operation returns a null error message.
        assertNull("No error should be present in a successful response", response.getErrorMessage());
        
        UserDTO guest = response.getValue();
        assertNotNull("UserDTO should not be null", guest);
        assertEquals("Guest", guest.getUsername());
        assertNotNull("Session token should be set for guest", guest.getSessionToken());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testGuestEnter_AlreadyLoggedUser_Negative() {
        // If the guest is already "logged in", a second call should fail.
        userService.guestEnter(); // first call: success
        userService.guestEnter(); // second call: should throw IllegalStateException
    }
    
    // =======================
    // 2. Exit (יציאה מהשוק)
    // =======================
    
    @Test
    public void testExit_Positive() {
        // Assume a guest user (with username "Guest" and a valid session token)
        UserDTO guest = new UserDTO("Guest", "tempSessionToken");
        
        // exit() returns a Response<Void> indicating success (no error)
        Response<Void> response = userService.exit(guest);
        assertNotNull("Response should not be null", response);
        assertNull("No error expected on successful exit", response.getErrorMessage());
        // In a full implementation you might verify repository interactions (e.g. deletion of temporary data)
    }
    
    @Test
    public void testExit_DifferentUser_Negative() {
        // Simulate a user exit for a user that does not match the current session.
        UserDTO guest = new UserDTO("Guest", "tempSessionToken");
        
        // For testing purposes, simulate that the repository throws an exception
        // when an exit is attempted for an unexpected user.
        // (Since IUserRepository does not include a deletion method,
        // we simulate this by stubbing getUser to throw an exception.)
        doThrow(new IllegalArgumentException("User mismatch"))
            .when(mockRepository).getUser("Guest");
        
        try {
            userService.exit(guest);
            fail("Expected an IllegalArgumentException due to user mismatch");
        } catch (IllegalArgumentException ex) {
            assertEquals("User mismatch", ex.getMessage());
        }
    }
    
    // =======================
    // 3. Register (רישום למערכת המסחר)
    // =======================
    
    @Test
    public void testRegister_Positive() {
        String username = "newUser";
        String password = "password123";
        
        // Simulate that no user exists with the new username
        when(mockRepository.getUser(username)).thenReturn(null);
        
        // Simulate repository adding the user.
        // Create an anonymous subclass of Member to represent the new user.
        Member newMember = new Member() {
            @Override
            public String getName() {
                return username;
            }
        };
        when(mockRepository.addUser(any(User.class))).thenReturn(newMember);
        
        Response<UserDTO> response = userService.register(username, password);
        assertNotNull("Response should not be null", response);
        // Successful registration: no error message.
        assertNull("No error expected on successful registration", response.getErrorMessage());
        
        UserDTO registeredUser = response.getValue();
        assertNotNull("UserDTO should not be null", registeredUser);
        assertEquals(username, registeredUser.getUsername());
        // Assume that a newly registered user is not immediately logged in,
        // so the session token would remain null.
        assertNull("Session token should be null for new registration", registeredUser.getSessionToken());
    }
    
    @Test
    public void testRegister_Negative() {
        String username = "existingUser";
        String password = "password123";
        
        // Simulate an existing user by returning a non-null user.
        Member existingMember = new Member() {
            @Override
            public String getName() {
                return username;
            }
        };
        when(mockRepository.getUser(username)).thenReturn(existingMember);
        
        Response<UserDTO> response = userService.register(username, password);
        assertNotNull("Response should not be null", response);
        // Expect an error message indicating that the user already exists.
        assertEquals("User already exists", response.getErrorMessage());
        assertNull("Value should be null on error", response.getValue());
    }
    
    // =======================
    // 4. Login (כניסה מזוהה)
    // =======================
    
    @Test
    public void testLogin_Positive() {
        String username = "registeredUser";
        String password = "correctPass";
        
        // Simulate retrieving an existing user.
        // Create an anonymous subclass of Member that returns true when login is called
        // with the correct password.
        Member member = new Member() {
            @Override
            public String getName() {
                return username;
            }
            @Override
            public boolean login(String providedPassword) {
                return password.equals(providedPassword);
            }
        };
        when(mockRepository.getUser(username)).thenReturn(member);
        
        Response<UserDTO> response = userService.login(username, password);
        assertNotNull("Response should not be null", response);
        // Successful login: no error message.
        assertNull("No error expected on successful login", response.getErrorMessage());
        
        UserDTO loggedUser = response.getValue();
        assertNotNull("UserDTO should not be null", loggedUser);
        assertEquals(username, loggedUser.getUsername());
        // Expect that a session token is assigned upon successful login.
        assertNotNull("Session token should be set on successful login", loggedUser.getSessionToken());
    }
    
    @Test
    public void testLogin_Negative() {
        String username = "registeredUser";
        String password = "wrongPass";
        
        // Simulate retrieving an existing user whose login method returns false for a wrong password.
        Member member = new Member() {
            @Override
            public String getName() {
                return username;
            }
            @Override
            public boolean login(String providedPassword) {
                return "correctPass".equals(providedPassword);
            }
        };
        when(mockRepository.getUser(username)).thenReturn(member);
        
        Response<UserDTO> response = userService.login(username, password);
        assertNotNull("Response should not be null", response);
        // A failed login returns an error message.
        assertEquals("Invalid credentials", response.getErrorMessage());
        assertNull("Value should be null on failed login", response.getValue());
    }
}
