package Domain.Shopping;

import Domain.Pair;
import Domain.Store.Product;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;

/**
 * Represents a purchase receipt in the system.
 * Contains information about the purchase, including products, client, store,
 * timestamp, and payment details.
 */
@Entity
@Table(name = "receipts")
public class Receipt {
    @Id
    private String receiptId;
    private String clientId;
    private String storeId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "receipt_products", 
        joinColumns = @JoinColumn(name = "receipt_id"))
    @MapKeyColumn(name = "product_id")
    private Map<String, ReceiptProduct> products; // Map of productId to quantity and price
    
    private LocalDateTime timestamp;
    private double totalPrice;
    
    @Column(length = 1000)
    private String paymentDetails; // Could contain masked card details or payment method

    @Column(length = 1000)
    private String supplyDetails; // Details about the supply method (e.g., delivery address)
    
    protected Receipt() {
        // Required by JPA
    }
    
    /**
     * Creates a new receipt with the given information.
     * 
     * @param clientId The ID of the client making the purchase
     * @param storeId The ID of the store where the purchase was made
     * @param products Map of products to their quantities and prices
     * @param totalPrice The total price of the purchase
     * @param paymentDetails Payment method or masked card details
     */
    public Receipt(String clientId, String storeId, Map<Product, Pair<Integer, Double>> products, 
                  double totalPrice, String paymentDetails, String supplyDetails) {
        this.receiptId = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.storeId = storeId;
        // Convert products map to use productId as key and ReceiptProduct as value
        this.products = new HashMap<>();
        for (Map.Entry<Product, Pair<Integer, Double>> entry : products.entrySet()) {
            Product product = entry.getKey();
            Pair<Integer, Double> qtyPrice = entry.getValue();
            this.products.put(product.getProductId(), 
                            new ReceiptProduct(qtyPrice.getFirst(), qtyPrice.getSecond(), product.getName()));
        }
        this.timestamp = LocalDateTime.now();
        this.totalPrice = totalPrice;
        this.paymentDetails = paymentDetails;
        this.supplyDetails = supplyDetails;
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
     * Gets the products purchased, as a map of product copies to quantities and prices.
     * 
     * @return Map of products to quantities and prices
     */
    public Map<Product, Pair<Integer, Double>> getProducts() {
        // Convert back to the original format
        Map<Product, Pair<Integer, Double>> result = new HashMap<>();
        for (Map.Entry<String, ReceiptProduct> entry : products.entrySet()) {
            String productId = entry.getKey();
            ReceiptProduct receiptProduct = entry.getValue();
            Product product = new Product(productId, receiptProduct.getProductName()); // Use stored product name
            result.put(product, new Pair<>(receiptProduct.getQuantity(), receiptProduct.getPrice()));
        }
        return result;
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

    public String getSupplyDetails() {
        return supplyDetails;
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
        
        // Convert products to map of productId to quantity and price
        Map<String, Pair<Integer, Double>> productQtyPrice = new HashMap<>();
        for (Map.Entry<String, ReceiptProduct> entry : products.entrySet()) {
            ReceiptProduct receiptProduct = entry.getValue();
            productQtyPrice.put(entry.getKey(), 
                              new Pair<>(receiptProduct.getQuantity(), receiptProduct.getPrice()));
        }
        map.put("products", productQtyPrice);
        
        return map;
    }
}