package UI.presenters;

import Application.DTOs.StoreDTO;
import Application.utils.Response;

public interface ITradingPresenter {
    boolean closeStore(String sessionToken, String storeId);
    Response<StoreDTO> getStoreByName(String sessionToken, String storeName);
} 