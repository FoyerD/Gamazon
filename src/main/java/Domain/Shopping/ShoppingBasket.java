package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of products from a specific store in a client's shopping cart.
 * Maintains the mapping between product IDs and their quantities.
 */
public class ShoppingBasket {
    
    private Map<String, Integer> orders;
    private String storeId;
    private String clientId;

    /**
     * Constructs a new shopping basket for a specific store and client.
     * 
     * @param storeId The ID of the store this basket belongs to
     * @param clientId The ID of the client who owns this basket
     */
    public ShoppingBasket(String storeId, String clientId) {
        this.storeId = storeId;
        this.clientId = clientId;
        this.orders = new HashMap<>();
    }

    /**
     * Gets a copy of the orders in this basket.
     * 
     * @return A new HashMap containing product IDs mapped to their quantities
     */
    public Map<String, Integer> getOrders() {
        // clone the set to prevent external modification
        return new HashMap<>(orders);
    }

    /**
     * Adds a product to the basket or increases its quantity if already present.
     * 
     * @param productId The ID of the product to add
     * @param quantity The quantity of the product to add
     */
    public void addOrder(String productId, Integer quantity) {
        if (orders.containsKey(productId)) {
            orders.put(productId,orders.get(productId) + quantity); 
        } 
        else {
            orders.put(productId, quantity);
        }
    }

    /**
     * Decreases the quantity of a product in the basket and removes it if quantity reaches zero or below.
     * 
     * @param productId The ID of the product to remove
     * @param quantity The quantity to remove
     */
    public void removeItem(String productId, int quantity) {
        orders.put(productId, orders.get(productId) - quantity);
        if (orders.get(productId) <= 0) {
            orders.remove(productId);
        }
    }

    /**
     * Completely removes a product from the basket regardless of quantity.
     * 
     * @param productId The ID of the product to remove
     */
    public void removeItem(String productId){
        orders.remove(productId);
    }

    /**
     * Checks if the basket contains any products.
     * 
     * @return true if the basket has no products, false otherwise
     */
    public boolean isEmpty() {
        return orders.isEmpty();
    }

    /**
     * Removes all products from the basket.
     */
    public void clear() {
        orders.clear();
    }

    /**
     * Returns the quantity of a specific product in the basket
     * If the product is not in the basket, it returns 0
     * 
     * @param productId The ID of the product to check
     * @return The quantity of the product in the basket, or 0 if not present
     */
    public int getProduct(String productId) {
        return orders.getOrDefault(productId, 0);
    }

    /**
     * Calculates the total quantity of all products in the basket.
     * 
     * @return The sum of quantities of all products
     */
    public int getQuantity(){
        int total = 0;
        for(int quantity : orders.values()){
            total += quantity;
        }  
        return total;
    }

    /**
     * Gets the ID of the store this basket belongs to.
     * 
     * @return The store ID
     */
    public String getStoreId() {
        return storeId;
    }

    /**
     * Gets the ID of the client who owns this basket.
     * 
     * @return The client ID
     */
    public String getClientId() {
        return clientId;
    }
}