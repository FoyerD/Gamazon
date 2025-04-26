package Domain.Shopping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReceiptRepository implements IReceiptRepository {
    // Main storage for all receipts: receiptId -> receipt details
    private final Map<String, Map<String, Object>> receipts;
    
    // Index for client purchases: clientId -> set of receiptIds
    private final Map<String, Map<String, String>> clientReceipts;
    
    // Index for store purchases: storeId -> set of receiptIds
    private final Map<String, Map<String, String>> storeReceipts;

    public ReceiptRepository() {
        this.receipts = new ConcurrentHashMap<>();
        this.clientReceipts = new ConcurrentHashMap<>();
        this.storeReceipts = new ConcurrentHashMap<>();
    }

    @Override
    public String savePurchase(String clientId, String storeId, Map<String, Integer> products, double totalPrice) {
        // Generate unique receipt ID
        String receiptId = UUID.randomUUID().toString();
        
        // Create receipt details
        Map<String, Object> receiptDetails = new HashMap<>();
        receiptDetails.put("clientId", clientId);
        receiptDetails.put("storeId", storeId);
        receiptDetails.put("products", new HashMap<>(products));
        receiptDetails.put("timestamp", LocalDateTime.now());
        receiptDetails.put("totalPrice", totalPrice);
        
        // Save receipt
        receipts.put(receiptId, receiptDetails);
        
        // Update client index
        clientReceipts.computeIfAbsent(clientId, k -> new ConcurrentHashMap<>())
                     .put(receiptId, storeId);
        
        // Update store index
        storeReceipts.computeIfAbsent(storeId, k -> new ConcurrentHashMap<>())
                    .put(receiptId, clientId);
        
        return receiptId;
    }

    @Override
    public Map<String, Map<String, Object>> getClientPurchaseHistory(String clientId) {
        Map<String, String> clientReceiptIds = clientReceipts.getOrDefault(clientId, new HashMap<>());
        
        // Return only the receipts for this client
        Map<String, Map<String, Object>> result = new ConcurrentHashMap<>();
        for (String receiptId : clientReceiptIds.keySet()) {
            if (receipts.containsKey(receiptId)) {
                result.put(receiptId, receipts.get(receiptId));
            }
        }
        
        return result;
    }

    @Override
    public Map<String, Map<String, Object>> getStorePurchaseHistory(String storeId) {
        Map<String, String> storeReceiptIds = storeReceipts.getOrDefault(storeId, new HashMap<>());
        
        // Return only the receipts for this store
        Map<String, Map<String, Object>> result = new ConcurrentHashMap<>();
        for (String receiptId : storeReceiptIds.keySet()) {
            if (receipts.containsKey(receiptId)) {
                result.put(receiptId, receipts.get(receiptId));
            }
        }
        
        return result;
    }
    
    // Utility method to clear all receipts (for testing or admin purposes)
    public void clear() {
        receipts.clear();
        clientReceipts.clear();
        storeReceipts.clear();
    }
}