package Domain.User;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class MemberTest {

    private Member member;
    private UUID id;
    private String username;
    private String password;
    private String email;

    @Before
    public void setUp() {
        id = UUID.randomUUID();
        username = "john_doe";
        password = "encodedPassword123";
        email = "john.doe@example.com";
        
        // Create a new Member instance with provided parameters
        member = new Member(id, username, password, email);
    }

    @Test
    public void testConstructor() {
        assertNotNull("Member should be created", member);
        assertEquals("ID should be correct", id.toString(), member.getId());
        assertEquals("Username should be correct", username, member.getName());
        assertTrue("Member should be logged in", member.isLoggedIn());
    }

    @Test
    public void testGetPassword() {
        // Ensure the password is correctly stored (this would be encoded in a real scenario)
        assertEquals("Password should match", password, member.getPassword());
    }

    @Test
    public void testGetEmail() {
        // Ensure the email is correctly stored
        assertEquals("Email should match", email, member.getEmail());
    }
}
