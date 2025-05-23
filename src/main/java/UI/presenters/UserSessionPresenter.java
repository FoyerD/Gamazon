package UI.presenters;

import org.springframework.stereotype.Component;

import Application.TokenService;

@Component
public class UserSessionPresenter implements IUserSessionPresenter {
    private final TokenService tokenService;
    private String sessionToken;

    public UserSessionPresenter(TokenService tokenService) {
        this.tokenService = tokenService;
        this.sessionToken = null;
    }

    public void setSessionToken(String token) {
        this.sessionToken = token;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    @Override
    public String extractUserIdFromToken(String sessionToken) {
        return tokenService.extractId(sessionToken); // your real method
    }
}
