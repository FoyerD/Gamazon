package Domain.Shopping;

import Domain.Pair;
import Domain.Store.Product;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a purchase receipt in the system.
 * Contains information about the purchase, including products, client, store,
 * timestamp, and payment details.
 */
public class Receipt {
    private final String receiptId;
    private final String clientId;
    private final String storeId;
    private final Map<Product, Pair<Integer, Double>> products; // Map of Product copies to quantities
    private final LocalDateTime timestamp;
    private final double totalPrice;
    private final String paymentDetails; // Could contain masked card details or payment method
    
    /**
     * Creates a new receipt with the given information.
     * 
     * @param clientId The ID of the client making the purchase
     * @param storeId The ID of the store where the purchase was made
     * @param products Map of products to their quantities
     * @param totalPrice The total price of the purchase
     * @param paymentDetails Payment method or masked card details
     */
    public Receipt(String clientId, String storeId, Map<Product, Pair<Integer, Double>> products, 
                  double totalPrice, String paymentDetails) {
        this.receiptId = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.storeId = storeId;
        // Create deep copies of all products to make receipt immutable
        this.products = new HashMap<>();
        for (Map.Entry<Product, Pair<Integer, Double>> entry : products.entrySet()) {
            this.products.put(new Product(entry.getKey()), entry.getValue());
        }
        this.timestamp = LocalDateTime.now();
        this.totalPrice = totalPrice;
        this.paymentDetails = paymentDetails;
    }
    
    /**
     * Gets the unique ID of this receipt.
     * 
     * @return The receipt ID
     */
    public String getReceiptId() {
        return receiptId;
    }
    
    /**
     * Gets the ID of the client who made the purchase.
     * 
     * @return The client ID
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Gets the ID of the store where the purchase was made.
     * 
     * @return The store ID
     */
    public String getStoreId() {
        return storeId;
    }
    
    /**
     * Gets the products purchased, as a map of product copies to quantities.
     * 
     * @return Map of products to quantities
     */
    public Map<Product, Pair<Integer, Double>> getProducts() {
        // Return a copy to maintain immutability
        return new HashMap<>(products);
    }
    
    /**
     * Gets the timestamp when the purchase was made.
     * 
     * @return The timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the total price of the purchase.
     * 
     * @return The total price
     */
    public double getTotalPrice() {
        return totalPrice;
    }
    
    /**
     * Gets the payment details for this purchase.
     * 
     * @return The payment details (masked card info or payment method)
     */
    public String getPaymentDetails() {
        return paymentDetails;
    }
    
    /**
     * Converts this receipt to a map representation for serialization or storage.
     * 
     * @return Map representation of the receipt
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("receiptId", receiptId);
        map.put("clientId", clientId);
        map.put("storeId", storeId);
        map.put("timestamp", timestamp);
        map.put("totalPrice", totalPrice);
        map.put("paymentDetails", paymentDetails);
        
        // Convert products to map of productId to quantity
        Map<String, Pair<Integer, Double>> productQtyPrice = new HashMap<>();
        for (Map.Entry<Product,  Pair<Integer, Double>> entry : products.entrySet()) {
            productQtyPrice.put(entry.getKey().getProductId(), entry.getValue());
        }
        map.put("products", productQtyPrice);
        
        return map;
    }
}