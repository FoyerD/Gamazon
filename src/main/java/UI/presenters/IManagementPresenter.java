package UI.presenters;

import java.util.List;

import Application.DTOs.ClientOrderDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.ReceiptDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import Domain.management.PermissionType; // Questionable but its enum so...

/**
 * Presenter interface for store management operations in the UI layer
 * (used by administrators or store owners/managers).
 */
public interface IManagementPresenter {

    /**
     * Creates a new store.
     *
     * @param sessionToken Session identifier for authentication.
     * @param name Store name.
     * @param description Store description.
     * @return Response with created {@link StoreDTO}, or error.
     */
    Response<StoreDTO> addStore(String sessionToken, String name, String description);

    /**
     * Opens a store (sets it to active/available).
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId ID of the store to open.
     * @return Response with true if successful.
     */
    Response<Boolean> openStore(String sessionToken, String storeId);

    /**
     * Closes a store (sets it to inactive/unavailable).
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId ID of the store to close.
     * @return Response with true if successful.
     */
    Response<Boolean> closeStore(String sessionToken, String storeId);

    /**
     * Adds a new item to a store's inventory.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId Store ID to which the item will be added.
     * @param productId Unique ID for the item.
     * @param description Item description.
     * @return Response with the ItemDTO if successful.
     */
    public Response<ItemDTO> addItem(String sessionToken, String storeId, String productId, String description);

    /**
     * Adds a new item to a store's inventory with price and amount.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId Store ID to which the item will be added.
     * @param productId Unique ID for the item.
     * @param price Price of the item.
     * @param amount Amount of the item.
     * @param description Item description.
     * @return Response with the ItemDTO if successful.
     */
    public Response<ItemDTO> addItem(String sessionToken, String storeId, String productId, double price, int amount, String description);
    
    /**
     * Removes an item from a store.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId Store ID from which the item will be removed.
     * @param productId ID of the item to remove.
     * @return Response with removed {@link ItemDTO}, or error.
     */
    Response<ItemDTO> removeItem(String sessionToken, String storeId, String productId);

    /**
     * Increases the amount of a specific item in store inventory.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId Store ID containing the item.
     * @param productId ID of the item.
     * @param amount Quantity to add.
     * @return Void response indicating success or failure.
     */
    Response<Void> increaseItemAmount(String sessionToken, String storeId, String productId, int amount);

    /**
     * Decreases the amount of a specific item in store inventory.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId Store ID containing the item.
     * @param productId ID of the item.
     * @param amount Quantity to remove.
     * @return Void response indicating success or failure.
     */
    Response<Void> decreaseItemAmount(String sessionToken, String storeId, String productId, int amount);

    /**
     * Appoints a new manager to a store.
     *
     * @param sessionToken Session identifier for authentication.
     * @param appointerUsername Username of the appointer (must have authority).
     * @param appointeeUsername Username of the new manager.
     * @param storeId Store ID for the appointment.
     * @return Void response indicating success or failure.
     */
    Response<Void> appointStoreManager(String sessionToken, String appointerUsername, String appointeeUsername, String storeId);

    /**
     * Removes a store manager.
     *
     * @param sessionToken Session identifier for authentication.
     * @param removerUsername Username of the remover (must have authority).
     * @param managerUsername Username of the manager to remove.
     * @param storeId Store ID.
     * @return Void response indicating success or failure.
     */
    Response<Void> removeStoreManager(String sessionToken, String removerUsername, String managerUsername, String storeId);

    /**
     * Appoints a new store owner.
     *
     * @param sessionToken Session identifier for authentication.
     * @param appointerUsername Username of the appointer (must be an owner).
     * @param appointeeUsername Username of the new owner.
     * @param storeId Store ID.
     * @return Void response indicating success or failure.
     */
    Response<Void> appointStoreOwner(String sessionToken, String appointerUsername, String appointeeUsername, String storeId);

    /**
     * Changes the permissions of a store manager.
     *
     * @param sessionToken Session identifier for authentication.
     * @param ownerUsername Username of the acting store owner.
     * @param managerUsername Username of the manager whose permissions will be updated.
     * @param storeId Store ID.
     * @param newPermissions List of new permissions to assign.
     * @return Void response indicating success or failure.
     */
    Response<Void> changeManagerPermissions(String sessionToken, String ownerUsername, String managerUsername, String storeId, List<PermissionType> newPermissions);

    /**
     * Closes a store (sets it to inactive/unavailable).
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId ID of the store to close.
     * @return Response with true if successful.
     */
    Response<Boolean> closeStoreNotPermanent(String sessionToken, String storeId);


    /**
     * Gets the purchase history of a store.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId ID of the store whose purchase history is requested.
     * @return Response with a list of {@link ClientOrderDTO} representing the purchase history, or error.
     */
    public Response<List<ClientOrderDTO>> getPurchaseHistory(String sessionToken, String storeId);
}
