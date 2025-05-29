package UI.presenters;

import org.springframework.stereotype.Component;

import Application.TokenService;

@Component
public class UserSessionPresenter implements IUserSessionPresenter {
    private final TokenService tokenService;

    public UserSessionPresenter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public String extractUserIdFromToken(String sessionToken) {
        return tokenService.extractId(sessionToken); // your real method
    }
}
