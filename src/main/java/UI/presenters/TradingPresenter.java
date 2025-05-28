package UI.presenters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.StoreService;
import Application.utils.Response;
import Application.DTOs.StoreDTO;
import UI.webSocketConfigurations.WebSocketNotifier;
import java.util.Set;

@Component
public class TradingPresenter implements ITradingPresenter {
    
    private StoreService storeService;
    
    private WebSocketNotifier webSocketNotifier;
    
    private UserSessionPresenter userSessionPresenter;

    private IStorePresenter storePresenter;

    @Autowired
    public TradingPresenter(StoreService storeService, WebSocketNotifier webSocketNotifier, 
                           UserSessionPresenter userSessionPresenter, IStorePresenter storePresenter) {
        this.storeService = storeService;
        this.webSocketNotifier = webSocketNotifier;
        this.userSessionPresenter = userSessionPresenter;
        this.storePresenter = storePresenter;
    }

    @Override
    public boolean closeStore(String sessionToken, String storeId) {
        // try {
        //     Response<Boolean> response = storeService.closeStore(sessionToken, storeId);
        //     if (response.getValue() != null && response.getValue()) {
        //         // Get store name for the notification message
        //         Response<StoreDTO> storeResponse = storePresenter.getStoreByName(sessionToken, storeId);
        //         String storeName = storeResponse.errorOccurred() ? storeId : storeResponse.getValue().getName();
                
        //         // Get all users with baskets in this store and notify them
        //         Set<String> usersWithBaskets = storeService.getUsersWithBaskets(storeId);
        //         for (String userId : usersWithBaskets) {
        //             webSocketNotifier.notifyUser(userId, 
        //                 String.format("Store '%s' has been closed. Your shopping basket in this store has been cleared.", storeName));
        //         }
        //         return true;
        //     }
        //     return false;
        // } catch (Exception e) {
        //     return false;
        // }
        return true; // Placeholder for actual implementation
    }

    @Override
    public Response<StoreDTO> getStoreByName(String sessionToken, String storeName) {
        return storePresenter.getStoreByName(sessionToken, storeName);
    }
} 