package UI.presenters;

import Application.DTOs.UserDTO;
import Application.utils.Response;

/**
 * Presenter interface for handling user login-related operations
 * within the UI layer in an MVP architecture.
 */
public interface ILoginPresenter {

    /**
     * Initiates a guest session for a user who is not logged in.
     *
     * @return Response containing a {@link UserDTO} with guest session details, or an error.
     */
    Response<UserDTO> guestEnter();

    /**
     * Terminates the session associated with the provided token.
     *
     * @param sessionToken Unique identifier for the session to terminate.
     * @return Response indicating success or failure of the operation.
     */
    Response<Void> logout(String sessionToken);

    /**
     * Registers a new user account during an active session.
     *
     * @param sessionToken Token of the session performing the registration.
     * @param username Desired username for the new user.
     * @param password Chosen password for the new user.
     * @param email User's email address.
     * @return Response containing a {@link UserDTO} for the registered user, or an error.
     */
    Response<UserDTO> registerUser(String sessionToken, String username, String password, String email);

    /**
     * Authenticates a user using provided credentials.
     *
     * @param username The username of the user attempting to log in.
     * @param password The password associated with the username.
     * @return Response containing a {@link UserDTO} for the authenticated user, or an error.
     */
    Response<UserDTO> login(String username, String password);
}
