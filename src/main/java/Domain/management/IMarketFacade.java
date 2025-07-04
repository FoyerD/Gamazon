package Domain.management;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.ExternalServices.INotificationService;
import Domain.Repos.IStoreRepository;
import Domain.Repos.IUserRepository;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.Receipt;
import Domain.User.Member;

/**
 * Interface for the Market Facade that manages all market-related operations.
 */
public interface IMarketFacade {

    // Section 1

    /**
     * 1.1 Open the whole market system by initializing user session.
     * 
     * @param userId ID of the user opening the market
     */
    void openMarket(String userId);

    /**
     * 1.2 Update the payment service used by the market.
     * 
     * @param paymentService New payment service implementation
     */
    void updatePaymentService(IExternalPaymentService paymentService);

    /**
     * 1.2 Update the URL of the payment service endpoint.
     * 
     * @param url New payment service URL
     * @throws IOException if updating the URL fails
     */
    void updatePaymentServiceURL(String url) throws IOException;

    /**
     * 1.3 Update the supply service used by the market.
     * 
     * @param supplyService New supply service implementation
     */
    void updateSupplyService(IExternalSupplyService supplyService);

    /**
     * Update the notification service used for sending messages to users.
     * 
     * @param notificationService New notification service implementation
     */
    void updateNotificationService(INotificationService notificationService);

    /**
     * Get the current notification service in use.
     * 
     * @return The notification service instance
     */
    INotificationService getNotificationService();

    /**
     * Checks if a user exists in the system by their username.
     * 
     * @param username The username to check
     * @return true if the user exists, false otherwise
     */
    boolean userExists(String username);

    /**
     * Get the username associated with a given user ID.
     * If the user does not exist, an exception is thrown.
     * 
     * @param userId The ID of the user
     * @return The username of the user
     */
    public String getUsername(String userId) throws NoSuchElementException;

    /**
     * Initialize facades with required repositories and services.
     * 
     * @param userRepository Repository for user data
     * @param itemRepository Repository for item data
     * @param storeFacade Facade for store management
     * @param shoppingCartFacade Facade for shopping cart management
     */
    void initFacades(IUserRepository userRepository,IStoreRepository storeRepository, IShoppingCartFacade shoppingCartFacade, PermissionManager permissionManager);
    // Section 4

    /**
     * 4.3 Appoint a new store manager.
     * 
     * @param appointerId Username of the appointing user (owner)
     * @param appointeeId Username of the user being appointed as manager
     * @param storeId ID of the store
     */
    void appointStoreManager(String appointerId, String appointeeId, String storeId);

    /**
     * 4.4 Remove a store owner from their position.
     * 
     * @param removerId ID of the user removing the manager
     * @param ownerId Username of the manager being removed
     * @param storeId ID of the store
     */
    void removeStoreOwner(String removerId, String ownerId, String storeId);


    /**
     * 4.7 Change the permissions of a store manager.
     * 
     * @param ownerUsername Username of the owner performing the change
     * @param managerUsername Username of the manager whose permissions are being changed
     * @param storeId ID of the store
     * @param newPermissions List of new permissions to assign
     */
    void changeManagerPermissions(String ownerUsername, String managerUsername, String storeId,
                                   List<PermissionType> newPermissions);
                                   
    /**
     * 4.6 Appoint a new store owner.
     * 
     * @param appointerUsername Username of the appointing user
     * @param appointeeUsername Username of the user being appointed as owner
     * @param storeId ID of the store
     */
    void appointStoreOwner(String appointerUsername, String appointeeUsername, String storeId);

    /**
     * 4.11 Retrieve all managers and their permissions in a store.
     * 
     * @param storeId ID of the store
     * @param userId ID of the user requesting the information
     * @return Map of managers to their list of permissions
     */
    Map<Member, List<PermissionType>> getManagersPermissions(String storeId, String userId);



    /**
     * part of 4.11 Retrieves all owners and their permissions in a store.
     *  
     * @param storeId ID of the store
     * @param userId ID of the user requesting the information
     * @return List of owners
     */
    List<Member> getOwners(String storeId, String userId);
    /**
     * 4.13 Retrieve the purchase history of a store within a given date range.
     * 
     * @param storeId ID of the store
     * @param userId ID of the user requesting the history
     * @return List of Receipts representing purchases
     */
    List<Receipt> getStorePurchaseHistory(String storeId, String userId);

    boolean banUser(String bannerId, String userId, Date endDate);
    boolean unbanUser(String bannerId, String userId);
    void checkPermission(String userId, String storeId, PermissionType permissionType);

    /**
     * Get all currently banned users and their ban expiration dates.
     * 
     * @return A map of usernames to their ban expiration dates
     */
    Map<String, Date> getBannedUsers();
}
