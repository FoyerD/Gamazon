package Domain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.User.IUserRepository;
import Domain.Store.Feedback;
import Domain.Store.IItemRepository;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.StoreFacade;

public interface IMarketFacade {
    
    // Section 1
    // 1.1 Open the whole market system
    void openMarket(String userId);

    // 1.2 Payment service
    void updatePaymentService(IPaymentService paymentService);

    void updatePaymentServiceURL(String url) throws IOException;

    // 1.3 Supply service
    void updateSupplyService(ISupplyService supplyService);

    void updateNotificationService(INotificationService notificationService);

    INotificationService getNotificationService();

    void initFacades(IUserRepository userRepository, IItemRepository itemRepository, StoreFacade storeFacade);
    
    // Section 4
    // 4.1 Manage product inventory
    void addProductsToInventory(String storeId, Map<Integer, Integer> productQuantitiesm, String userId);
    void updateProductQuantities(String storeId, Map<Integer, Integer> productQuantities, String userId);
    void removeProductsFromInventory(String storeId, Map<Integer, Integer> productQuantities, String userId);
    
    // 4.3 Appoint a store manager
    void appointStoreManager(String appointerUsername, String appointeeUsername, String storeId);
    
    // 4.4 Remove a store manager
    void removeStoreManager(String removerUsername, String managerUsername, String storeId);
    
    // 4.6 Appoint a store owner
    void appointStoreOwner(String appointerUsername, String appointeeUsername, String storeId);
    
    // 4.7 Change a manager's permissions
    void changeManagerPermissions(String ownerUsername, String managerUsername, String storeId,
                                      List<PermissionType> newPermissions);
    
    // 4.9 Close a store 
    void closeStore(String storeId, String userId);

    // 4.11 Get info about manager permissions in a store
    Map<String, List<PermissionType>> getManagersPermissions(String storeId, String userId);
    
    // 4.12 Respond to user messages
    boolean respondToUserMessage(String storeId, String productId, String userId, String comment);
    Feedback getUserMessage(String storeId, String productId, String userId);
    
    // 4.13 View store purchase history
    List<ShoppingBasket> getStorePurchaseHistory(String storeId, LocalDateTime from, LocalDateTime to, String userId);
    
    // Section 6
    // 6.1 Close a store in the market without cancelling subscriptions
    void marketCloseStore(String storeId, String userId);
}
