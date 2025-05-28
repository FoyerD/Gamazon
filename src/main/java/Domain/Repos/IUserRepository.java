package Domain.Repos;

import java.util.List;

import Domain.User.Guest;
import Domain.User.Member;
import Domain.User.User;

public abstract class IUserRepository extends ILockbasedRepository<User, String> {

    /**
     * Adds a user to the repository.
     * @param id The ID of the user to add.
     * @param user The user to add.
     * @return true if the user was added successfully, false otherwise.
     */
    abstract public boolean add(String id, User user);

    /**
     * Removes a user from the repository.
     * @param id The ID of the user to remove.
     * @return The removed user, or null if no user was found with the given ID.
     */
    abstract public User remove(String id);

    /**
     * Retrieves a user from the repository by their ID.
     * @param id The ID of the user to retrieve.
     * @return The user with the given ID, or null if no user was found.
     */
    abstract public User get(String id);

    /**
     * Retrieves a guest user from the repository by their ID.
     * @param id The ID of the guest user to retrieve.
     * @return The guest user with the given ID, or null if no user was found.
     */
    abstract public Guest getGuest(String id);

    /**
     * Retrieves a member user from the repository by their ID.
     * @param id The ID of the member user to retrieve.
     * @return The member user with the given ID, or null if no user was found.
     */
    abstract public Member getMember(String id);

    /**
     * Updates a user in the repository.
     * @param id The ID of the user to update.
     * @param user The updated user object.
     * @return The updated user, or null if no user was found with the given ID.
     */
    abstract public User update(String id, User user);

    /**
     * Retrieves a member user from the repository by their username.
     * @param username The username of the member user to retrieve.
     * @return The member user with the given username, or null if no user was found.
     */
    abstract public Member getMemberByUsername(String username);

    /**
     * Retrieves the username of a member user by their ID.
     * @param id The ID of the member user.
     * @return The username of the member user with the given ID, or null if no user was found.
     */
    abstract public String getMemberUsername(String id);

    /**
     * Checks if a user is a member based on their ID.
     * @param id The ID of the user to check.
     * @return true if the user is a member, false otherwise.
     */
    abstract public boolean userIsMember(String id);

    /**
     * Retrieves all members in the repository.
     * @return A list of all {@link Member} users.
     */
    abstract public List<Member> getAllMembers();
}
