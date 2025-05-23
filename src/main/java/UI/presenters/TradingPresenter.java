package UI.presenters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.StoreService;
import Application.utils.Response;
import Application.DTOs.StoreDTO;
import UI.webSocketConfigurations.WebSocketNotifier;

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
        try {
            Response<Boolean> response = storeService.closeStore(sessionToken, storeId);
            return response.getValue() != null && response.getValue();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Response<StoreDTO> getStoreByName(String sessionToken, String storeName) {
        return storePresenter.getStoreByName(sessionToken, storeName);
    }
} 