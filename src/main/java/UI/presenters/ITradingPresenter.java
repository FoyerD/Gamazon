package UI.presenters;

import Application.DTOs.StoreDTO;
import Application.utils.Response;
import java.util.Date;
import java.util.Map;

public interface ITradingPresenter {
    boolean closeStore(String sessionToken, String storeId);
    Response<StoreDTO> getStoreByName(String sessionToken, String storeName);
    Response<Boolean> banUser(String sessionToken, String userId, Date endDate);
    Response<Boolean> unbanUser(String sessionToken, String userId);
    Response<Map<String, Date>> getBannedUsers(String sessionToken);
} 