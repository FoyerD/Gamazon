
package Domain.User;

import java.util.NoSuchElementException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class LoginManager {
    private IUserRepository userRepository;

    private LoginManager(IUserRepository userRepository) {
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

    public Member register(String id, String username, String password) throws IllegalStateException {

        User guest = userRepository.get(id);
        if (guest == null) {
            throw new NoSuchElementException("Guest not found");
        }
        

        if (userRepository.getUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        PasswordChecker.check(password);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encoded = passwordEncoder.encode(password);

        Member member = new Member(guest);
        if (!userRepository.add(member.getId(), member)) {
            throw new IllegalStateException("Failed to add member to repository");
        }
        return member;
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

