package Domain.Shopping;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the IShoppingCart interface.
 * Represents a client's shopping cart containing references to stores where they have items.
 */
class ShoppingCart implements IShoppingCart {

    private String clientId;
    private Set<String> baskets;
    
    /**
     * Constructs a new shopping cart for a client with an empty set of stores.
     * 
     * @param clientId The ID of the client who owns this cart
     */
    public ShoppingCart(String clientId) {
        this.clientId = clientId;
        this.baskets = new HashSet<>();
    }

    /**
     * Constructs a new shopping cart for a client with a pre-populated set of stores.
     * 
     * @param clientId The ID of the client who owns this cart
     * @param baskets A set of store IDs where the client has items
     */
    public ShoppingCart(String clientId, Set<String> baskets) {
        this.clientId = clientId;
        this.baskets = baskets;
    }

    /**
     * Gets the ID of the client who owns this shopping cart.
     * 
     * @return The client's unique identifier
     */
    @Override
    public String getClientId() {
        return clientId;
    }

    /**
     * Retrieves a copy of the set of store IDs in the cart.
     * 
     * @return A new HashSet containing all store IDs where the client has items
     */
    @Override
    public Set<String> getCart() {
        return new HashSet<>(baskets); // return a copy for safety
    }

    /**
     * Adds a store to the shopping cart.
     * 
     * @param storeId The unique identifier of the store to add
     */
    @Override
    public void addStore(String storeId) {
        baskets.add(storeId);
    }

    /**
     * Removes a store from the shopping cart.
     * 
     * @param storeId The unique identifier of the store to remove
     */
    @Override
    public void removeStore(String storeId) {
        baskets.remove(storeId);
    }

    /**
     * Checks if a specific store exists in the shopping cart.
     * 
     * @param storeId The store ID to check
     * @return true if the store is in the cart, false otherwise
     */
    @Override
    public boolean hasStore(String storeId) {
        return baskets.contains(storeId);
    }

    /**
     * Removes all stores from the shopping cart.
     */
    @Override
    public void clear() {
        baskets.clear();
    }

    /**
     * Checks if the shopping cart contains any stores.
     * 
     * @return true if the cart has no stores, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return baskets.isEmpty();
    }
}