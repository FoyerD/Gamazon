package Domain.Shopping;

import java.util.Map;

public interface IReceiptRepository {
    /**
     * Save a purchase record
     * 
     * @param clientId The client who made the purchase
     * @param storeId The store where the purchase was made
     * @param products Map of productId to quantity purchased
     * @param totalPrice The total price of the purchase
     * @return The generated receipt ID
     */
    String savePurchase(String clientId, String storeId, Map<String, Integer> products, double totalPrice);
    
    /**
     * Get all purchases made by a client
     * 
     * @param clientId The client ID
     * @return Map of receipt IDs to purchase details
     */
    Map<String, Map<String, Object>> getClientPurchaseHistory(String clientId);
    
    /**
     * Get all purchases made at a store
     * 
     * @param storeId The store ID
     * @return Map of receipt IDs to purchase details
     */
    Map<String, Map<String, Object>> getStorePurchaseHistory(String storeId);
}