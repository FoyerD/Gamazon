package Domain.User;

public class LoginManager {
    private IUserRepository userRepository;

    private LoginManager(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Guest createGuest() {
        return userRepository.createGuest();
    }

    public User exit() {
        return userRepository.getUser("Guest");
    }
