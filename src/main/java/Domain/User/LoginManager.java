package Domain.User;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import Domain.Repos.IUserRepository;

@Component
public class LoginManager {
    private IUserRepository userRepository;

    public LoginManager(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /***
     * Retrieves a user by their ID.
     * @param id The ID of the user to retrieve.
     * @return The user associated with the given ID.
     * @throws NoSuchElementException if no user is found with the given ID.
     ***/
    public User getUser(String id) {
        User user = userRepository.get(id);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }
        return user;
    }

    /***
     * Retrieves a user by their username.
     * @param username The username of the user to retrieve.
     * @return The user associated with the given username.
     * @throws NoSuchElementException if no user is found with the given username.
     ***/
    public User getUserByUsername(String username) {
        Member member = userRepository.getMemberByUsername(username);
        if (member == null) {
            throw new NoSuchElementException("User not found");
        }
        return member;
    }

    /***
     * Creates a new guest user and adds it to the repository.
     * @return The newly created guest user.
     * @throws IllegalStateException if the guest cannot be added to the repository.
     ***/
    public Guest createGuest() throws IllegalStateException {
        Guest guest = Guest.createGuest();
        if (!userRepository.add(guest.getId(), guest)) {
            throw new IllegalStateException("Failed to add guest to repository");
        }
        return guest;
    }


    /***
     * Registers a new member user based on the provided guest user.
     * @param id The ID of the guest user to register.
     * @param username The desired username for the new member.
     * @param password The desired password for the new member.
     * @param email The email address of the new member.
     * @return The newly registered member user.
     * @throws IllegalStateException if the registration fails or if the guest cannot be removed from the repository.
     ***/
    public synchronized Member register(String id, String username, String password, String email) throws IllegalStateException {

        Guest guest = userRepository.getGuest(id);
        if (guest == null) {
            throw new NoSuchElementException("Guest not found");
        }

        if (username.equals(Guest.NAME)) {
            throw new IllegalArgumentException("Cannot register with username " + Guest.NAME);
        }

        if (userRepository.getMemberByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        PasswordChecker.check(password);
        
        if (!EmailValidator.getInstance().isValid(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encoded = passwordEncoder.encode(password);

        Member member = guest.register(username, encoded, email);
        member.login();

        if (userRepository.remove(id) == null) {
            throw new IllegalStateException("Failed to remove guest from repository");
        }

        if (!userRepository.add(member.getId(), member)) {
            throw new IllegalStateException("Failed to add member to repository");
        }
        return member;
    }


    /***
     * Logs in a member user with the given username and password.
     * @param username The username of the member to log in.
     * @param password The password of the member to log in.
     * @return The logged-in member user.
     * @throws NoSuchElementException if no member is found with the given username.
     * @throws IllegalStateException if the member is already logged in.
     ***/
    public Member login(String username, String password) throws NoSuchElementException, IllegalStateException {
        Member member = userRepository.getMemberByUsername(username);
        if (member == null) {
            throw new NoSuchElementException("Member not found");
        }

        if (member.isLoggedIn()) {
            throw new IllegalStateException("User is already logged in");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (passwordEncoder.matches(password, member.getPassword())) {
            member.login();
            return member;
        } else {
            throw new IllegalArgumentException("Invalid password");
        }

    }

    
    /***
     * Logs out a member user with the given ID.
     * @param id The ID of the member to log out.
     * @return The logged-out member user.
     * @throws NoSuchElementException if no member is found with the given ID.
     ***/
    public User exit(String id) {

        User user = userRepository.get(id);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }
        
        user.logout(this);
        return user;
    }


    /***
     * Logs out a guest user with the given ID. Essentially removes the guest from the repository.
     * @param guest The guest user to log out.
     * @throws IllegalStateException if the guest cannot be removed from the repository.
     ***/
    public void exit(Guest guest) throws IllegalStateException {
        if (userRepository.remove(guest.getId()) == null) {
            throw new IllegalStateException("Failed to remove guest from repository");
        }
    }


    /***
     * Checks if a member user is logged in based on their username.
     * @param username The username of the member to check.
     * @return true if the member is logged in, false otherwise.
     ***/
    public boolean isLoggedin(String username) {
        Member user = userRepository.getMemberByUsername(username);
        return user.isLoggedIn();
    }


    public List<Member> getAllMembers() {
        return userRepository.getAllMembers();
    }

    public void logOutAllUsers() {
        List<Member> members = userRepository.getAllMembers();
        for (Member member : members) {
            if (member.isLoggedIn()) {
                member.logout(this);
            }
        }
    }
}

