package Infrastructure.MemoryRepositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import Domain.Pair;
import Domain.Repos.IReceiptRepository;
import Domain.Shopping.Receipt;
import Domain.Store.Product;


/**
 * Repository for storing and retrieving purchase receipts.
 */
@Repository
public class MemoryReceiptRepository implements IReceiptRepository {
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