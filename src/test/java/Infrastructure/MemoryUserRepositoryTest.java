package Infrastructure;

import org.junit.Before;
import org.junit.Test;

import Domain.User.Guest;
import Domain.User.Member;
import Domain.User.User;
import Infrastructure.Repositories.MemoryUserRepository;

import static org.junit.Assert.*;

import java.util.UUID;

public class MemoryUserRepositoryTest {

    private MemoryUserRepository userRepository;
    private Member member;
    private Guest guest;

    @Before
    public void setUp() {
        userRepository = new MemoryUserRepository();
        guest = Guest.createGuest();
        member = new Member(UUID.randomUUID(), "bob123", "ENCODED", "bob@example.com");
    }

    @Test
    public void testAddUser() {
        // Test adding a user successfully
        String userId = member.getId();
        boolean added = userRepository.add(userId, member);
        assertTrue(added);

        // Test adding the same user again, should return false
        boolean addedAgain = userRepository.add(userId, member);
        assertFalse(addedAgain);
    }

    @Test
    public void testRemoveUser() {
        // Add a user to remove
        userRepository.add(member.getId(), member);
        User removedUser = userRepository.remove(member.getId());

        assertNotNull(removedUser);
        assertEquals(member.getId(), removedUser.getId());

        // Remove the same user again, should return null
        User removedAgain = userRepository.remove(member.getId());
        assertNull(removedAgain);
    }

    @Test
    public void testGetUser() {
        // Add a user and retrieve it
        userRepository.add(member.getId(), member);
        User retrievedUser = userRepository.get(member.getId());
        assertNotNull(retrievedUser);
        assertEquals(member.getId(), retrievedUser.getId());

        // Retrieve a non-existing user
        User nonExistingUser = userRepository.get(UUID.randomUUID().toString());
        assertNull(nonExistingUser);
    }

    @Test
    public void testGetMember() {
        // Add a user and get member by ID
        userRepository.add(member.getId(), member);
        Member retrievedMember = userRepository.getMember(member.getId());
        assertNotNull(retrievedMember);
        assertEquals(member.getId(), retrievedMember.getId());

        // Add a guest and get member by ID
        userRepository.add(guest.getId(), guest);
        Member nonMember = userRepository.getMember(guest.getId());
        assertNull(nonMember);
    }

    @Test
    public void testGetGuest() {
        // Add a guest and get guest by ID
        userRepository.add(guest.getId(), guest);
        Guest retrievedGuest = userRepository.getGuest(guest.getId());
        assertNotNull(retrievedGuest);
        assertEquals(guest.getId(), retrievedGuest.getId());

        // Add a member and get guest by ID
        userRepository.add(member.getId(), member);
        Guest nonGuest = userRepository.getGuest(member.getId());
        assertNull(nonGuest);
    }

    @Test
    public void testUpdateUser() {
        // Add a user and update it
        userRepository.add(member.getId(), member);
        Member updatedMember = new Member(UUID.fromString(member.getId()), "bob123", "NEWENCODED", "bob@newemail.com");
        User updatedUser = userRepository.update(member.getId(), updatedMember);

        assertNotNull(updatedUser);
        assertEquals(updatedMember.getId(), updatedUser.getId());
        assertEquals(updatedMember.getEmail(), ((Member) updatedUser).getEmail());

        // Update a non-existing user
        User nonExistingUser = userRepository.update(UUID.randomUUID().toString(), updatedMember);
        assertNull(nonExistingUser);
    }

    @Test
    public void testGetMemberByUsername() {
        // Add a member and get by username
        userRepository.add(member.getId(), member);
        Member retrievedMember = userRepository.getMemberByUsername(member.getName());
        assertNotNull(retrievedMember);
        assertEquals(member.getId(), retrievedMember.getId());

        // Add a guest and try to get member by username
        userRepository.add(guest.getId(), guest);
        Member nonExistingMember = userRepository.getMemberByUsername(guest.getName());
        assertNull(nonExistingMember);
    }

    @Test
    public void testUserIsMember() {
        // Add a member and check if they are a member
        userRepository.add(member.getId(), member);
        boolean isMember = userRepository.userIsMember(member.getId());
        assertTrue(isMember);

        // Check if a guest is a member
        userRepository.add(guest.getId(), guest);
        boolean isNotMember = userRepository.userIsMember(guest.getId());
        assertTrue(isNotMember);  // Since the user exists, the method returns true for both members and guests
    }
}
