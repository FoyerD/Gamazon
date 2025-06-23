package Infrastructure.MemoryRepositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Pair;
import Domain.Repos.IReceiptRepository;
import Domain.Shopping.Receipt;
import Domain.Store.Product;


/**
 * Repository for storing and retrieving purchase receipts.
 */
@Repository
@Profile("dev")
public class MemoryReceiptRepository extends IReceiptRepository {
    // Main storage for all receipts: receiptId -> Receipt
    private final Map<String, Receipt> receipts;
    
    // Index for client purchases: clientId -> set of receiptIds
    private final Map<String, Map<String, String>> clientReceipts;
    
    // Index for store purchases: storeId -> set of receiptIds
    private final Map<String, Map<String, String>> storeReceipts;

    /**
     * Creates a new ReceiptRepository.
     */
    public MemoryReceiptRepository() {
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
    public String savePurchase(String clientId, String storeId, Map<Product, Pair<Integer, Double>> products, 
                       double totalPrice, String paymentDetails, String supplyDetails) {
        Receipt receipt = new Receipt(clientId, storeId, products, totalPrice, paymentDetails, supplyDetails);
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

    @Override
    public boolean add(String id, Receipt value) {
        if (id == null || value == null) {
            throw new IllegalArgumentException("ID and value cannot be null");
        }
        if (!id.equals(value.getReceiptId())) {
            throw new IllegalArgumentException("ID does not match the receipt ID");
        }
        if (receipts.containsKey(id)) {
            throw new IllegalArgumentException("Receipt with this ID already exists");
        }

        receipts.put(id, value);
        return true;
    }

    @Override
    public Receipt remove(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        Receipt removedReceipt = receipts.remove(id);
        if (removedReceipt != null) {
            String clientId = removedReceipt.getClientId();
            String storeId = removedReceipt.getStoreId();
            
            // Remove from client index
            Map<String, String> clientReceiptIds = clientReceipts.get(clientId);
            if (clientReceiptIds != null) {
                clientReceiptIds.remove(id);
                if (clientReceiptIds.isEmpty()) {
                    clientReceipts.remove(clientId);
                }
            }
            
            // Remove from store index
            Map<String, String> storeReceiptIds = storeReceipts.get(storeId);
            if (storeReceiptIds != null) {
                storeReceiptIds.remove(id);
                if (storeReceiptIds.isEmpty()) {
                    storeReceipts.remove(storeId);
                }
            }
        }
        
        return removedReceipt;
    }

    @Override
    public Receipt get(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return receipts.get(id);
    }

    @Override
    public Receipt update(String id, Receipt value) {
        if (id == null || value == null) {
            throw new IllegalArgumentException("ID and value cannot be null");
        }
        if (!id.equals(value.getReceiptId())) {
            throw new IllegalArgumentException("ID does not match the receipt ID");
        }
        if (!receipts.containsKey(id)) {
            throw new IllegalArgumentException("Receipt with this ID does not exist");
        }

        receipts.put(id, value);
        return value;
    }

    @Override
    public void deleteAll() {
        receipts.clear();
        clientReceipts.clear();
        storeReceipts.clear();
        this.deleteAllLocks();
    }
}