package Domain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.User.IUserRepository;
import Domain.User.User;
import Domain.Store.IStoreRepository;
import Domain.Shopping.IShoppingBasket;
import Domain.Shopping.IShoppingCart;
import Domain.Store.Item;

public interface IMarketFacade {
    
    // External services related methods
    void updatePaymentService(IPaymentService paymentService);

    void updateNotificationService(INotificationService notificationService);

    void updateSupplyService(ISupplyService supplyService);

    void updatePaymentServiceURL(String url) throws IOException;

    void initFacades(IUserRepository userFacade, IStoreRepository storeFacade);
    
    INotificationService getNotificationService();

    // Section 4
    // 1. Manage product inventory
    void manageStoreInventory(int storeId, Map<Integer, Integer> productQuantities);

    // 3. Appoint a store manager
    void appointStoreManager(String appointerUsername, String appointeeUsername, int storeId);
    
    // 4. Remove a store manager
    void removeStoreManager(String removerUsername, String managerUsername, int storeId);
    
    // 6. Appoint a store owner
    void appointStoreOwner(String appointerUsername, String appointeeUsername, int storeId);
    
    // 7. Change a manager's permissions
    void changeManagerPermissions(String ownerUsername, String managerUsername, int storeId,
                                      List<PermissionType> newPermissions);
    
    // 9. Close a store
    void closeStore(int storeId, User user);

    // 11. Get info about manager permissions in a store
    Map<String, List<PermissionType>> getManagersPermissions(int storeId);
    
    // 12. Respond to user messages
    void respondToUserMessage(int storeId, int messageId, String response);
    
    // 13. View store purchase history
    List<IShoppingBasket> getStorePurchaseHistory(int storeId, LocalDateTime from, LocalDateTime to);
    
    // Section 6
    // 6.1 Open the whole market system
    void openMarket();
}
