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
    
    // Section 1
    // 1.1 Open the whole market system
    void openMarket();

    // 1.2 Payment service
    void updatePaymentService(IPaymentService paymentService);

    void updatePaymentServiceURL(String url) throws IOException;

    // 1.3 Supply service
    void updateSupplyService(ISupplyService supplyService);

    void updateNotificationService(INotificationService notificationService);

    INotificationService getNotificationService();

    void initFacades(IUserRepository userFacade, IStoreRepository storeFacade);
    
    // Section 4
    // 4.1 Manage product inventory
    void manageStoreInventory(int storeId, Map<Integer, Integer> productQuantities);

    // 4.3 Appoint a store manager
    void appointStoreManager(String appointerUsername, String appointeeUsername, int storeId);
    
    // 4.4 Remove a store manager
    void removeStoreManager(String removerUsername, String managerUsername, int storeId);
    
    // 4.6 Appoint a store owner
    void appointStoreOwner(String appointerUsername, String appointeeUsername, int storeId);
    
    // 4.7 Change a manager's permissions
    void changeManagerPermissions(String ownerUsername, String managerUsername, int storeId,
                                      List<PermissionType> newPermissions);
    
    // 4.9 Close a store 
    void closeStore(int storeId, User user);

    // 4.11 Get info about manager permissions in a store
    Map<String, List<PermissionType>> getManagersPermissions(int storeId);
    
    // 4.12 Respond to user messages
    void respondToUserMessage(int storeId, int messageId, String response);
    
    // 4.13 View store purchase history
    List<IShoppingBasket> getStorePurchaseHistory(int storeId, LocalDateTime from, LocalDateTime to);
    
    // Section 6
    // 6.1 Close a store in the market without cancelling subscriptions
    void marketCloseStore(int storeId, User user);

}
