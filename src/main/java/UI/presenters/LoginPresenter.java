package UI.presenters;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.DTOs.UserDTO;
import Application.UserService;
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
    public Response<UserDTO> registerUser(String sessionToken, String username, String password, String email, LocalDate birthDate) {
        return userService.register(sessionToken, username, password, email, birthDate);
    }

    @Override
    public Response<UserDTO> guestEnter() {
        return userService.guestEntry();
    }

    @Override
    public Response<Void> logout(String sessionToken) {
        return userService.exit(sessionToken);
    }

    @Override
    public Response<List<UserDTO>> getAllMembers(String sessionToken) {
        return userService.getAllMembers(sessionToken);
    }

    @Override
    public Response<Void> logOutAllUsers() {
        return userService.logOutAllUsers();
    }
}