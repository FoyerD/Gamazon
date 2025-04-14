package Application;
import Domain.TokenService;
import Domain.User.*;

public class UserService {
    private IUserRepository userRepository;
    private TokenService tokenService;

    public UserService(IUserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public Response<UserDTO> guestEnter() {
        User guest = userRepository.createGuest();
        String token = tokenService.generateToken(guest.getName());
        UserDTO user = new UserDTO(guest.getName(), token);

        return new Response<>(user);
    }

    public Response<Boolean> isLoggedIn(String username) {
        User user = userRepository.getUser(username);
        if (user == null) {
            return Response.error("User not found");
        }
        return Response.success(user.isLoggedIn());
    }

    public Response<Void> exit(String sessionToken) {
        // TODO: fix guest name logic
        String username = tokenService.extractUsername(sessionToken);
        User user = userRepository.getUser(username);
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
