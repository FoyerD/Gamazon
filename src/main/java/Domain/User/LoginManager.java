
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

    

    public User exit(String id) {

        User user = userRepository.get(id);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }
        
        user.logout(this);
        return user;
    }

    public void exit(Guest guest) throws IllegalStateException {
        if (userRepository.remove(guest.getId()) == null) {
            throw new IllegalStateException("Failed to remove guest from repository");
        }
    }

    public boolean isLoggedin(String username) {
        Member user = userRepository.getMemberByUsername(username);
        return user.isLoggedIn();
    }
}

