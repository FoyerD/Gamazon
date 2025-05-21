package UI.presenters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.TokenService;
import Application.UserService;
import Application.DTOs.UserDTO;
import Application.utils.Response;

@Component
public class LoginPresenter implements ILoginPresenter {
    private final UserService userService;

    @Autowired
    public LoginPresenter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Response<UserDTO> login(String username, String password) {
        return userService.login(username, password);
    }

    @Override
    public Response<UserDTO> registerUser(String sessionToken, String username, String password, String email) {
        return userService.register(sessionToken, username, password, email);
    }

    @Override
    public Response<UserDTO> guestEnter() {
        return userService.guestEntry();
    }

    @Override
    public Response<Void> logout(String sessionToken) {
        return userService.exit(sessionToken);
    }
}