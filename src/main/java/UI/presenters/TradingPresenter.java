package UI.presenters;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.DTOs.StoreDTO;
import Application.MarketService;
import Application.StoreService;
import Application.UserService;
import Application.utils.Error;
import Application.utils.Response;
import Domain.User.Member;
import Domain.User.User;
import UI.webSocketConfigurations.WebSocketNotifier;

@Component
public class TradingPresenter implements ITradingPresenter {
    
    private StoreService storeService;
    private MarketService marketService;
    private UserService userService;
    private WebSocketNotifier webSocketNotifier;
    private UserSessionPresenter userSessionPresenter;
    private IStorePresenter storePresenter;

    @Autowired
    public TradingPresenter(StoreService storeService, MarketService marketService, UserService userService,
                           WebSocketNotifier webSocketNotifier, UserSessionPresenter userSessionPresenter, 
                           IStorePresenter storePresenter) {
        this.storeService = storeService;
        this.marketService = marketService;
        this.userService = userService;
        this.webSocketNotifier = webSocketNotifier;
        this.userSessionPresenter = userSessionPresenter;
        this.storePresenter = storePresenter;
    }

    @Override
    public boolean closeStore(String sessionToken, String storeId) {
        try {
            //Response<StoreDTO> storeResponse = storeService.getStoreByName(sessionToken, storeId);

            storeService.closeStore(sessionToken, storeId);
            /*
            if (response.getValue() != null && response.getValue()) {
                // Get store name for the notification message
                String storeName = storeResponse.errorOccurred() ? storeId : storeResponse.getValue().getName();
                
                // Get all users with baskets in this store and notify them
                Set<String> usersWithBaskets = storeService.getUsersWithBaskets(storeId);
                for (String userId : usersWithBaskets) {
                    webSocketNotifier.notifyUser(userId, 
                        String.format("Store '%s' has been closed. Your shopping basket in this store has been cleared.", storeName));
                }
                return true;
            }*/
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Response<StoreDTO> getStoreByName(String sessionToken, String storeName) {
        return storePresenter.getStoreByName(sessionToken, storeName);
    }

    @Override
    public Response<Boolean> banUser(String sessionToken, String username, Date endDate) {
        try {
            // First check if the user exists
            Response<Boolean> userExistsResponse = marketService.userExists(username);
            if (userExistsResponse.errorOccurred() || !userExistsResponse.getValue()) {
                return new Response<>(new Error("User not found: " + username));
            }

            // Get the member from the repository using username
            User user = userService.getLoginManager().getUserByUsername(username);
            if (!(user instanceof Member)) {
                return new Response<>(new Error("User is not a member: " + username));
            }
            Member member = (Member) user;
            String userId = member.getId();

            Response<Boolean> response = marketService.banUser(sessionToken, userId, endDate);
            return response;
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    @Override
    public Response<Boolean> unbanUser(String sessionToken, String username) {
        try {
            // First check if the user exists
            Response<Boolean> userExistsResponse = marketService.userExists(username);
            if (userExistsResponse.errorOccurred() || !userExistsResponse.getValue()) {
                return new Response<>(new Error("User not found: " + username));
            }

            // Get the member from the repository using username
            User user = userService.getLoginManager().getUserByUsername(username);
            if (!(user instanceof Member)) {
                return new Response<>(new Error("User is not a member: " + username));
            }
            Member member = (Member) user;
            String userId = member.getId();

            Response<Boolean> response = marketService.unbanUser(sessionToken, userId);
            return response;
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    @Override
    public Response<Map<String, Date>> getBannedUsers(String sessionToken) {
        try {
            return marketService.getBannedUsers(sessionToken);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }
} 