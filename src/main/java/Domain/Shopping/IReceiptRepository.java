package Domain.Shopping;

import Domain.Pair;
import Domain.Store.Product;
import java.util.List;
import java.util.Map;

/**
 * Interface for repository that manages purchase receipts.
 */
public interface IReceiptRepository {
    /**
     * Save a purchase receipt.
     * 
     * @param receipt The Receipt object to save
     * @return The ID of the saved receipt
     */
    String saveReceipt(Receipt receipt);
    
    /**
     * Save a purchase record (convenience method)
     * 
     * @param clientId The client who made the purchase
     * @param storeId The store where the purchase was made
     * @param products Map of products to quantity purchased
     * @param totalPrice The total price of the purchase
     * @param paymentDetails Payment method information (masked)
     * @return The generated receipt ID
     */
    String savePurchase(String clientId, String storeId, Map<Product, Pair<Integer, Double>> products, 
                       double totalPrice, String paymentDetails);
    
    /**
     * Get a specific receipt by ID
     * 
     * @param receiptId The receipt ID
     * @return The receipt object, or null if not found
     */
    Receipt getReceipt(String receiptId);
    
    /**
     * Get all receipts for a specific client
     * 
     * @param clientId The client ID
     * @return List of receipts for the client
     */
    List<Receipt> getClientReceipts(String clientId);
    
    /**
     * Get all receipts for a specific store
     * 
     * @param storeId The store ID
     * @return List of receipts for the store
     */
    List<Receipt> getStoreReceipts(String storeId);
    
    /**
     * Get all receipts for a client at a specific store
     * 
     * @param clientId The client ID
     * @param storeId The store ID
     * @return List of receipts for the client at the specified store
     */
    List<Receipt> getClientStoreReceipts(String clientId, String storeId);
    
    /**
     * Clear all stored receipts
     */
    void clear();
}