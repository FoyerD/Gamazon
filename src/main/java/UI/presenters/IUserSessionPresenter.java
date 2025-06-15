package UI.presenters;

import Application.utils.Response;

public interface IUserSessionPresenter {

    String extractUserIdFromToken(String sessionToken);
    Response<Boolean> isUserBanned(String sessionToken);
    
}
