package Domain.User;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import Domain.Repos.IUserRepository;


@RunWith(MockitoJUnitRunner.class)
public class LoginManagerTest {


    @Mock
    private IUserRepository userRepository;
    
   

    private LoginManager loginManager;

    @Before
    public void setUp() {
        loginManager = new LoginManager(userRepository);
    }

    @Test
    public void testGetUser_Success() {
        String userId = UUID.randomUUID().toString();
        User mockUser = new Member(UUID.fromString(userId), "alice", "pw", "a@b.com");
        when(userRepository.get(userId)).thenReturn(mockUser);

        User result = loginManager.getUser(userId);

        assertSame(mockUser, result);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetUser_NotFound() {
        String userId = UUID.randomUUID().toString();
        when(userRepository.get(userId)).thenReturn(null);

        loginManager.getUser(userId);
    }

    @Test
    public void testCreateGuest_Success() {
        when(userRepository.add(anyString(), any(Guest.class))).thenReturn(true);

        Guest guest = loginManager.createGuest();

        assertNotNull(guest);
        verify(userRepository).add(eq(guest.getId()), same(guest));
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateGuest_FailToAdd() {
        when(userRepository.add(anyString(), any(Guest.class))).thenReturn(false);

        loginManager.createGuest();
    }

    @Test
    public void testRegister_Success() {
        String guestId = UUID.randomUUID().toString();
        String username = "bob123";
        String rawPass = "StrongP@ssw0rd!";
        String email = "bob@example.com";

        Guest mockGuest = mock(Guest.class);
        Member fakeMember = new Member(UUID.randomUUID(), username, "ENCODED", email);

        when(userRepository.getGuest(guestId)).thenReturn(mockGuest);
        when(userRepository.getMemberByUsername(username)).thenReturn(null);
        when(userRepository.remove(guestId)).thenReturn(mockGuest);
        when(userRepository.add(anyString(), any(Member.class))).thenReturn(true);
        when(mockGuest.register(anyString(), anyString(), anyString()))
            .thenReturn(fakeMember);

        Member result = loginManager.register(guestId, username, rawPass, email);

        assertSame(fakeMember, result);
        InOrder order = inOrder(userRepository);
        order.verify(userRepository).getGuest(guestId);
        order.verify(userRepository).remove(guestId);
        order.verify(userRepository).add(eq(fakeMember.getId()), same(fakeMember));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRegister_GuestNotFound() {
        String guestId = UUID.randomUUID().toString();
        when(userRepository.getGuest(guestId)).thenReturn(null);

        loginManager.register(guestId, "u", "p", "e@x.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegister_UsernameReserved() {
        String guestId = UUID.randomUUID().toString();
        when(userRepository.getGuest(guestId)).thenReturn(mock(Guest.class));

        loginManager.register(guestId, Guest.NAME, "p", "e@x.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegister_UsernameAlreadyExists() {
        String guestId = UUID.randomUUID().toString();
        when(userRepository.getGuest(guestId)).thenReturn(mock(Guest.class));
        when(userRepository.getMemberByUsername("taken")).thenReturn(mock(Member.class));

        loginManager.register(guestId, "taken", "p", "e@x.com");
    }

    @Test (expected = IllegalArgumentException.class)
    public void testRegister_InvalidEmail() {
        String username = "newuser";
        String password = "StrongP@ss1";
        String email = "dfhgfh.";
        UUID guestId = UUID.randomUUID();
        Guest mockGuest = mock(Guest.class);
        when(userRepository.getGuest(guestId.toString())).thenReturn(mockGuest);
        loginManager.register(guestId.toString(), username, password, email);

    }

    @Test
    public void testLogin_Success() {
        String username = "charlie";
        String rawPass = "openSesame";
        BCryptPasswordEncoder realEncoder = new BCryptPasswordEncoder();
        String encoded = realEncoder.encode(rawPass);

        Member member = new Member(UUID.randomUUID(), username, encoded, "c@d.com");
        member.logout(loginManager);
        when(userRepository.getMemberByUsername(username)).thenReturn(member);

        Member result = loginManager.login(username, rawPass);

        assertSame(member, result);
        assertTrue(member.isLoggedIn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLogin_InvalidPassword() {
        String username = "charlie";
        String encoded = new BCryptPasswordEncoder().encode("correct");
        Member member = new Member(UUID.randomUUID(), username, encoded, "c@d.com");
        member.logout(loginManager);
        when(userRepository.getMemberByUsername(username)).thenReturn(member);

        loginManager.login(username, "wrongPassword");
    }

    @Test(expected = NoSuchElementException.class)
    public void testLogin_UserNotFound() {
        when(userRepository.getMemberByUsername("nope")).thenReturn(null);

        loginManager.login("nope", "pw");
    }

    @Test(expected = IllegalStateException.class)
    public void testLogin_AlreadyLoggedIn() {
        Member member = new Member(UUID.randomUUID(), "dana", "pw", "d@e.com");
        when(userRepository.getMemberByUsername("dana")).thenReturn(member);

        loginManager.login("dana", "pw");
    }

    @Test
    public void testExit_ById_Success() {
        String userId = UUID.randomUUID().toString();
        User user = mock(User.class);
        when(user.isLoggedIn()).thenReturn(true);
        when(userRepository.get(userId)).thenReturn(user);

        loginManager.exit(userId);

        verify(user).logout(loginManager);
    }

    @Test(expected = NoSuchElementException.class)
    public void testExit_ById_NotFound() {
        when(userRepository.get("bad")).thenReturn(null);

        loginManager.exit("bad");
    }

    @Test
    public void testExit_Guest_Success() {
        Guest guest = Guest.createGuest();
        when(userRepository.remove(guest.getId())).thenReturn(guest);

        loginManager.exit(guest);

        verify(userRepository).remove(guest.getId());
    }

    @Test(expected = IllegalStateException.class)
    public void testExit_Guest_FailToRemove() {
        Guest guest = Guest.createGuest();
        when(userRepository.remove(guest.getId())).thenReturn(null);

        loginManager.exit(guest);
    }

    @Test
    public void testLogOutAllUsers_AllLoggedOutAndUpdated() {
        // Arrange
        Guest guest1 = spy(Guest.createGuest());
        Guest guest2 = spy(Guest.createGuest());
        Member member = spy(new Member(UUID.randomUUID(), "zoe", "pass", "z@x.com"));

        // Set them as logged in
        doReturn(true).when(guest1).isLoggedIn();
        doReturn(true).when(guest2).isLoggedIn();
        doReturn(true).when(member).isLoggedIn();


        List<User> allUsers = List.of(guest1, guest2, member);
        when(userRepository.getAllUsers()).thenReturn(allUsers);
        when(userRepository.update(guest1.getId(), guest1)).thenReturn(guest1);
        when(userRepository.update(guest2.getId(), guest2)).thenReturn(guest2);
        when(userRepository.update(member.getId(), member)).thenReturn(member);
        when(userRepository.remove(guest1.getId())).thenReturn(guest1);
        when(userRepository.remove(guest2.getId())).thenReturn(guest2);

        // Act
        loginManager.logOutAllUsers();

        // Assert
        for (User user : allUsers) {
            verify(user).logout(loginManager);
            verify(userRepository).update(user.getId(), user);
        }
    }

}
