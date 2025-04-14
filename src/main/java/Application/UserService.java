package Application;
import Domain.User.*;

public class UserService {
    private IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Response<UserDTO> guestEnter() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Response<Boolean> isLoggedIn(String username) {
        // Logic to check if user is logged in
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public Response<Void> exit(UserDTO user) {
        // Logic to handle user exit
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public Response<UserDTO> register(String username, String password) {
        // Logic to register a new user
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Response<UserDTO> login(String username, String password) {
        // Logic to authenticate a user
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
