package Domain.User;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class GuestTest {

    private Guest guest;
    private LoginManager loginManager;

    @Before
    public void setUp() {
        guest = Guest.createGuest();  // Create a new guest
        loginManager = mock(LoginManager.class);  // Mock the LoginManager for the logout test
    }

    @Test
    public void testCreateGuest() {
        assertNotNull("Guest should be created", guest);
        assertEquals(Guest.NAME, guest.getName());  // Verify the name of the guest
        assertTrue(guest.isLoggedIn());  // Ensure the guest is logged in by default
    }

    @Test
    public void testRegister() {
        String username = "alice";
        String password = "Password123!";
        String email = "alice@example.com";

        // Perform the registration
        Member member = guest.register(username, password, email);

        // Verify the member was created with the correct attributes
        assertNotNull("Member should be created", member);
        assertEquals(username, member.getName());
        assertEquals(email, member.getEmail());
        assertTrue(member.isLoggedIn());  // Ensure the member is logged in
    }

    @Test
    public void testLogout() {
        guest.logout(loginManager);  // Perform logout
        
        // Verify that the logout method in LoginManager was called
        verify(loginManager).exit(guest);  // Ensure loginManager.exit() was invoked for the guest
        
        // Ensure the guest is logged out
        assertFalse(guest.isLoggedIn());
    }
}
