package Domain.Shopping;

import java.util.Date;
import java.util.List;
import java.util.Set;

import Domain.Pair;
import Domain.Store.Item;

/**
 * Interface for shopping cart operations
 */
public interface IShoppingCartFacade {
    /**
     * Makes a bid on an auction
     * 
     * @param auctionId The auction ID
     * @param clientId The client making the bid
     * @param price The bid price
     * @return true if the bid was successful
     */
    boolean makeBid(String auctionId, String clientId, float price);
    
    /**
     * Adds a product to the shopping cart
     * 
     * @param storeId The store ID
     * @param clientId The client ID
     * @param productId The product ID
     * @param quantity The quantity to add
     * @return true if the product was added successfully
     */
    boolean addProductToCart(String storeId, String clientId, String productId, int quantity);
    
    /**
     * Processes checkout for all items in the cart
     * 
     * @param clientId The client ID
     * @param card_number The payment card number
     * @param expiry_date The card expiry date
     * @param cvv The card security code
     * @param andIncrement Transaction identifier
     * @param clientName The client's name
     * @param deliveryAddress The delivery address
     * @return true if checkout was successful
     */
    boolean checkout(String clientId, String card_number, Date expiry_date, String cvv, 
                    long andIncrement, String clientName, String deliveryAddress);
    
    /**
     * Removes a specific quantity of a product from the cart
     * 
     * @param storeId The store ID
     * @param clientId The client ID
     * @param productId The product ID
     * @param quantity The quantity to remove
     * @return true if the product quantity was successfully removed
     */
    boolean removeProductFromCart(String storeId, String clientId, String productId, int quantity);
    
    /**
     * Removes a product entirely from the cart
     * 
     * @param storeId The store ID
     * @param clientId The client ID
     * @param productId The product ID
     * @return true if the product was successfully removed
     */
    boolean removeProductFromCart(String storeId, String clientId, String productId);
    
    /**
     * Gets the total number of items in the cart
     * 
     * @param clientId The client ID
     * @return the total number of items
     */
    int getTotalItems(String clientId);
    
    /**
     * Checks if the cart is empty
     * 
     * @param clientId The client ID
     * @return true if the cart is empty
     */
    boolean isEmpty(String clientId);
    
    /**
     * Clears all contents from the client's cart
     * 
     * @param clientId The client ID
     * @return true if the cart was successfully cleared
     */
    boolean clearCart(String clientId);
    
    /**
     * Clears all items from a specific store in the client's cart
     * 
     * @param clientId The client ID
     * @param storeId The store ID
     * @return true if the basket was successfully cleared
     */
    boolean clearBasket(String clientId, String storeId);
    
    /**
     * Views the contents of the client's cart
     * 
     * @param clientId The client ID
     * @return Map of store IDs to maps of product IDs and quantities
     */
    Set<Pair<Item,Integer>> viewCart(String clientId);
    
    /**
     * Gets a client's purchase history
     * 
     * @param clientId The client ID
     * @return List of receipt objects for the client
     */
    List<Receipt> getClientPurchaseHistory(String clientId);
    
    /**
     * Gets a store's purchase history
     * 
     * @param storeId The store ID
     * @return List of receipt objects for the store
     */
    List<Receipt> getStorePurchaseHistory(String storeId);
}