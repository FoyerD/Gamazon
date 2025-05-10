package UI.presenters;

import Application.DTOs.UserDTO;
import Application.utils.Response;
import org.springframework.stereotype.Component;

@Component
public class LoginPresenterMock implements ILoginPresenter {

    @Override
    public Response<UserDTO> guestEnter() {
        UserDTO guestUser = new UserDTO("guest-token", "Guest", "guest@gamazon.com");
        return Response.success(guestUser);
    }

    @Override
    public Response<Void> logout(String sessionToken) {
        return Response.success(null);
    }

    @Override
    public Response<UserDTO> registerUser(String sessionToken, String username, String password, String email) {
        UserDTO newUser = new UserDTO("fake-session-token", username, email);
        return Response.success(newUser);
    }

    @Override
    public Response<UserDTO> login(String username, String password) {
        if (username.isBlank() || password.isBlank()) {
            return Response.error("Username and password cannot be empty");
        }
        UserDTO user = new UserDTO("mock-session-token", username, username + "@gamazon.com");
        return Response.success(user);
    }
}
