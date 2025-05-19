package UI.presenters;

public interface IUserSessionPresenter {

    String extractUserIdFromToken(String sessionToken);
    
}
