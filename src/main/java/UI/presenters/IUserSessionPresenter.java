package UI.presenters;

public interface IUserSessionPresenter {

    String extractUserIdFromToken(String sessionToken);
    
    void setSessionToken(String token);
    String getSessionToken();
}
