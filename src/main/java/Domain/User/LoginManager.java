
package Domain.User;

import java.util.NoSuchElementException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.apache.commons.validator.routines.EmailValidator;

public class LoginManager {
    private IUserRepository userRepository;

    public LoginManager(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(String id) {
        User user = userRepository.get(id);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }
        return user;
    }

    public Guest createGuest() throws IllegalStateException {
        Guest guest = Guest.createGuest();
        if (!userRepository.add(guest.getId(), guest)) {
            throw new IllegalStateException("Failed to add guest to repository");
        }
        return guest;
    }

    public Member register(String id, String username, String password, String email) throws IllegalStateException {

        User guest = userRepository.get(id);
        if (guest == null) {
            throw new NoSuchElementException("Guest not found");
        }

        if (userRepository.getUserByUsername(username) != null) {
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


        if (userRepository.remove(guest.getId()) == null) {
            throw new IllegalStateException("Failed to remove guest from repository");
        }

        if (!userRepository.add(member.getId(), member)) {
            throw new IllegalStateException("Failed to add member to repository");
        }
        return member;
    }

    public Member login(String id, String password) throws NoSuchElementException, IllegalStateException {
        User user = userRepository.get(id);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }

        if (user.isLoggedIn()) {
            throw new IllegalStateException("User is already logged in");
        }

        if (user instanceof Member) {
            Member member = (Member) user;
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(password, member.getPassword())) {
                member.login();
                return member;
            } else {
                throw new IllegalArgumentException("Invalid password");
            }
        } else {
            throw new IllegalStateException("User is not a member");
        }
    }

    

    public void exit(String id) {

        User user = userRepository.get(id);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }
        
        user.visitExit(this);
    }

    public void exit(Guest guest) throws IllegalStateException {
        if (userRepository.remove(guest.getId()) == null) {
            throw new IllegalStateException("Failed to remove guest from repository");
        }
    }
}

