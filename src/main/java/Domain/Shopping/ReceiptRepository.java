package Domain.Shopping;

import Domain.Store.Product;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Repository for storing and retrieving purchase receipts.
 */
public class ReceiptRepository implements IReceiptRepository {
    // Main storage for all receipts: receiptId -> Receipt
    private final Map<String, Receipt> receipts;
    
    // Index for client purchases: clientId -> set of receiptIds
    private final Map<String, Map<String, String>> clientReceipts;
    
    // Index for store purchases: storeId -> set of receiptIds
    private final Map<String, Map<String, String>> storeReceipts;

    /**
     * Creates a new ReceiptRepository.
     */
    public ReceiptRepository() {
        this.receipts = new ConcurrentHashMap<>();
        this.clientReceipts = new ConcurrentHashMap<>();
        this.storeReceipts = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String saveReceipt(Receipt receipt) {
        String receiptId = receipt.getReceiptId();
        String clientId = receipt.getClientId();
        String storeId = receipt.getStoreId();
        
        // Save receipt
        receipts.put(receiptId, receipt);
        
        // Update client index
        clientReceipts.computeIfAbsent(clientId, k -> new ConcurrentHashMap<>())
                     .put(receiptId, storeId);
        
        // Update store index
        storeReceipts.computeIfAbsent(storeId, k -> new ConcurrentHashMap<>())
                    .put(receiptId, clientId);
        
        return receiptId;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String savePurchase(String clientId, String storeId, Map<Product, Integer> products, 
                             double totalPrice, String paymentDetails) {
        Receipt receipt = new Receipt(clientId, storeId, products, totalPrice, paymentDetails);
        return saveReceipt(receipt);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Receipt getReceipt(String receiptId) {
        return receipts.get(receiptId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Receipt> getClientReceipts(String clientId) {
        Map<String, String> clientReceiptIds = clientReceipts.getOrDefault(clientId, new HashMap<>());
        
        List<Receipt> result = new ArrayList<>();
        for (String receiptId : clientReceiptIds.keySet()) {
            Receipt receipt = receipts.get(receiptId);
            if (receipt != null) {
                result.add(receipt);
            }
        }
        
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Receipt> getStoreReceipts(String storeId) {
        Map<String, String> storeReceiptIds = storeReceipts.getOrDefault(storeId, new HashMap<>());
        
        List<Receipt> result = new ArrayList<>();
        for (String receiptId : storeReceiptIds.keySet()) {
            Receipt receipt = receipts.get(receiptId);
            if (receipt != null) {
                result.add(receipt);
            }
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Receipt> getClientStoreReceipts(String clientId, String storeId) {
        Map<String, String> clientReceiptIds = clientReceipts.getOrDefault(clientId, new HashMap<>());
        
        List<Receipt> result = new ArrayList<>();
        for (String receiptId : clientReceiptIds.keySet()) {
            Receipt receipt = receipts.get(receiptId);
            if (receipt != null && storeId.equals(receipt.getStoreId())) {
                result.add(receipt);
            }
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        receipts.clear();
        clientReceipts.clear();
        storeReceipts.clear();
    }
}