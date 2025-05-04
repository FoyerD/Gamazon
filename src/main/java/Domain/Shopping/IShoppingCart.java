package Domain.Shopping;

import java.util.Set;

/**
 * Interface defining the functionality of a shopping cart.
 * A shopping cart maintains a collection of store IDs that a client is shopping from.
 */
public interface IShoppingCart {
    /**
     * Retrieves the set of store IDs in the cart.
     * 
     * @return A Set containing all store IDs where the client has items
     */
    Set<String> getCart();
    
    /**
     * Gets the ID of the client who owns this shopping cart.
     * 
     * @return The client's unique identifier
     */
    String getClientId();
    
    /**
     * Adds a store to the shopping cart.
     * 
     * @param storeId The unique identifier of the store to add
     */
    void addStore(String storeId);
    
    /**
     * Removes a store from the shopping cart.
     * 
     * @param storeId The unique identifier of the store to remove
     */
    void removeStore(String storeId);
    
    /**
     * Checks if a specific store exists in the shopping cart.
     * 
     * @param storeId The store ID to check
     * @return true if the store is in the cart, false otherwise
     */
    boolean hasStore(String storeId);
    
    /**
     * Removes all stores from the shopping cart.
     */
    void clear();
    
    /**
     * Checks if the shopping cart contains any stores.
     * 
     * @return true if the cart has no stores, false otherwise
     */
    boolean isEmpty();
}