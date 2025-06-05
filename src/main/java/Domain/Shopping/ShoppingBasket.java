package Domain.Shopping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import Domain.Store.Item;
import Domain.Store.Discounts.Discount;
import Domain.Store.Discounts.ItemPriceBreakdown;

/**
 * Represents a collection of products from a specific store in a client's shopping cart.
 * Maintains the mapping between product IDs and their quantities.
 */
public class ShoppingBasket {
    
    private Map<String, Integer> orders; // maps product IDs to their quantities
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

    public int getQuantity(String productId) {
        if (orders.containsKey(productId)) {
            return orders.get(productId);
        } else {
            return 0; // Return 0 if the product is not in the basket
        }
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

    public Map<String, ItemPriceBreakdown> getBestPrice(BiFunction<String, String, Item> itemGetter){
        return getBestPrice(itemGetter, new ArrayList<Discount>());
    }

    public Map<String, ItemPriceBreakdown> getBestPrice(BiFunction<String, String, Item> itemGetter, List<Discount> discounts) {
        Map<String, ItemPriceBreakdown> bestPrices = new HashMap<>();
        Map<String, ItemPriceBreakdown> currPriceBreakdowns = null;
        
        double bestPrice = Double.MAX_VALUE;
        double currPrice = 0;

        if (itemGetter == null) {
            throw new IllegalArgumentException("Item getter function cannot be null");
        }
        if( discounts == null || discounts.isEmpty()) {
            for (String productId : orders.keySet()) {
                ItemPriceBreakdown priceBreakdown = new ItemPriceBreakdown(itemGetter.apply(storeId, productId)); // Placeholder, should be replaced with actual price retrieval logic
                bestPrices.put(productId, priceBreakdown);
            }
            return bestPrices; // Return the best prices without any discounts
        }

        // Iterate through all discounts and calculate the best price breakdowns
        for (Discount discount : discounts) {
                currPriceBreakdowns = discount.calculatePrice(this, itemGetter);
                if (currPriceBreakdowns == null || currPriceBreakdowns.isEmpty()) {
                    continue; // Skip this discount if it doesn't provide any price breakdowns
                }
                currPrice = ItemPriceBreakdown.calculateFinalPrice(currPriceBreakdowns);
                if( currPrice < bestPrice) {
                    bestPrice = currPrice;
                    bestPrices = currPriceBreakdowns; // Update best prices with the current breakdowns
                }
            }
        return bestPrices;
    }
}