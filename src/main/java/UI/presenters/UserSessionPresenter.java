package UI.presenters;

import org.springframework.stereotype.Component;

import Application.TokenService;
import Application.MarketService;
import Application.utils.Response;

@Component
public class UserSessionPresenter implements IUserSessionPresenter {
    private final TokenService tokenService;
    private final MarketService marketService;

    public UserSessionPresenter(TokenService tokenService, MarketService marketService) {
        this.tokenService = tokenService;
        this.marketService = marketService;
    }

    @Override
    public String extractUserIdFromToken(String sessionToken) {
        return tokenService.extractId(sessionToken); // your real method
    }

    @Override
    public Response<Boolean> isUserBanned(String sessionToken) {
        try {
            String userId = extractUserIdFromToken(sessionToken);
            return marketService.isBanned(userId);
        } catch (Exception e) {
            return Response.error("Failed to check ban status: " + e.getMessage());
        }
    }
}
