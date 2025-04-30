package Domain.Shopping;

import java.util.Date;
import java.util.Map;

/**
 * Interface defining the facade for shopping cart operations.
 * Provides high-level functionality for managing shopping carts and baskets,
 * including adding/removing products and checkout process.
 */
public interface IShoppingCartFacade {
    /**
     * Adds a product to the client's shopping cart.
     * 
     * @param storeId The ID of the store where the product is sold
     * @param clientId The ID of the client adding the product
     * @param productId The ID of the product being added
     * @param quantity The amount of the product to add
     * @return true if the product was successfully added, false otherwise
     */
    boolean addProductToCart(String storeId, String clientId, String productId, int quantity);
    
    /**
     * Performs checkout operation for all items in a client's cart.
     * Processes payment and handles inventory updates.
     * 
     * @param clientId The ID of the client checking out
     * @param card_number The payment card number
     * @param expiry_date The expiration date of the payment card
     * @param cvv The CVV security code of the payment card
     * @param andIncrement A transaction sequence number
     * @param clientName The name of the client
     * @param deliveryAddress The address for delivery
     * @return true if checkout completed successfully, false otherwise
     */
    boolean checkout(String clientId, String card_number, Date expiry_date, String cvv, long andIncrement, String clientName, String deliveryAddress);
    
    /**
     * Removes a specific quantity of a product from the client's cart.
     * 
     * @param storeId The ID of the store where the product is sold
     * @param clientId The ID of the client removing the product
     * @param productId The ID of the product being removed
     * @param quantity The amount of the product to remove
     * @return true if the product was successfully removed, false otherwise
     */
    boolean removeProductFromCart(String storeId, String clientId, String productId, int quantity);
    
    /**
     * Removes all instances of a product from the client's cart.
     * 
     * @param storeId The ID of the store where the product is sold
     * @param clientId The ID of the client removing the product
     * @param productId The ID of the product being removed
     * @return true if the product was successfully removed, false otherwise
     */
    boolean removeProductFromCart(String storeId, String clientId, String productId);
    
    /**
     * Gets the total number of items in the client's cart across all stores.
     * 
     * @param clientId The ID of the client
     * @return The total number of items in the cart
     */
    int getTotalItems(String clientId);
    
    /**
     * Checks if the client's cart is empty.
     * 
     * @param clientId The ID of the client
     * @return true if the cart is empty, false otherwise
     */
    boolean isEmpty(String clientId);
    
    /**
     * Removes all items from the client's cart across all stores.
     * 
     * @param clientId The ID of the client
     * @return true if the cart was successfully cleared, false otherwise
     */
    boolean clearCart(String clientId);
    
    /**
     * Removes all items from a specific store in the client's cart.
     * 
     * @param clientId The ID of the client
     * @param storeId The ID of the store to clear from the cart
     * @return true if the store's basket was successfully cleared, false otherwise
     */
    boolean clearBasket(String clientId, String storeId);
    
    /**
     * Provides a view of all items in the client's cart organized by store.
     * 
     * @param clientId The ID of the client
     * @return A map where the keys are store IDs and the values are maps of product IDs to quantities
     */
    Map<String, Map<String, Integer>> viewCart(String clientId);
}