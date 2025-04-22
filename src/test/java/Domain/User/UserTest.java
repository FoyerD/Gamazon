package Domain.User;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User("john") {};
    }

    @Test
    public void testInitialState() {
        assertTrue(user.isLoggedIn());
        assertEquals("john", user.getName());
        assertNotNull(user.getId());
    }

    @Test
    public void testLogout() {
        user.login();
        user.logout(null); // no actual login manager needed
        assertFalse(user.isLoggedIn());
    }

    @Test
    public void testLogin() {
        user.login();
        assertTrue(user.isLoggedIn());
    }

    @Test
    public void testConstructorId() {
        UUID id = UUID.randomUUID();
        User userWithId = new User(id, "named") {};
        assertEquals(id.toString(), userWithId.getId());
        assertEquals("named", userWithId.getName());
        assertTrue(userWithId.isLoggedIn());
    }

    @Test
    public void testDifferentId() {
        User user2 = new User("eve") {};
        assertNotEquals(user.id, user2.id);
    }
}
