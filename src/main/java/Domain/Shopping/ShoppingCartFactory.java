package Domain.Shopping;

import java.util.Set;

/**
 * Factory class for creating ShoppingCart instances.
 * This provides a controlled way for classes outside the Domain.Shopping package
 * to create shopping cart instances without directly accessing the ShoppingCart class.
 */
public class ShoppingCartFactory {
    
    /**
     * Creates a new shopping cart for the specified client.
     * 
     * @param clientId The ID of the client who owns this cart
     * @return A new IShoppingCart instance
     */
    public static IShoppingCart createShoppingCart(String clientId) {
        return new ShoppingCart(clientId);
    }
    
    /**
     * Creates a new shopping cart for the specified client with pre-populated stores.
     * 
     * @param clientId The ID of the client who owns this cart
     * @param stores A set of store IDs where the client has items
     * @return A new IShoppingCart instance
     */
    public static IShoppingCart createShoppingCart(String clientId, Set<String> stores) {
        return new ShoppingCart(clientId, stores);
    }
}