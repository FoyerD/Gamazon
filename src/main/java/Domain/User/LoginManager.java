package Domain.User;

import java.util.NoSuchElementException;

public class LoginManager {
    private IUserRepository userRepository;

    private LoginManager(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(String id) {
        User user = userRepository.getUser(id);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }
        return user;
    }

    public Guest createGuest() {
        return userRepository.createGuest();
    }

    public void exit(String id) {

        User user = userRepository.getUser(id);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }
        
        user.visitExit(this);
    }

    public void exit(Guest guest) throws IllegalStateException {
        if (!userRepository.remove(guest.getId())) {
            throw new IllegalStateException("Failed to remove guest from repository");
        }
    }
}

