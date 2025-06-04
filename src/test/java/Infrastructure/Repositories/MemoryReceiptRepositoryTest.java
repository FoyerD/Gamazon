package Infrastructure.Repositories;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Domain.Pair;
import Domain.Shopping.Receipt;
import Domain.Store.Category;
import Domain.Store.Product;
import Infrastructure.MemoryRepositories.MemoryReceiptRepository;

/**
 * Test class for MemoryReceiptRepository
 */
public class MemoryReceiptRepositoryTest {
    
    private MemoryReceiptRepository repository;
    private static final String CLIENT_ID = "client123";
    private static final String STORE_ID = "store123";
    private static final String PAYMENT_DETAILS = "xxxx-xxxx-xxxx-1234";
    
    @Before
    public void setUp() {
        repository = new MemoryReceiptRepository();
    }
    
    @Test
    public void testSaveReceipt() {
        // Create a test receipt
        Map<Product, Pair<Integer, Double>> products = new HashMap<>();
        products.put(createProduct("product1", "Product 1"), new Pair<>(2, 10.0));
        products.put(createProduct("product2", "Product 2"), new Pair<>(1, 15.0));
        
        Receipt receipt = new Receipt(CLIENT_ID, STORE_ID, products, 35.0, PAYMENT_DETAILS);
        
        // Save the receipt
        String receiptId = repository.saveReceipt(receipt);
        
        // Verify the receipt was saved
        assertNotNull(receiptId);
        Receipt savedReceipt = repository.getReceipt(receiptId);
        assertNotNull(savedReceipt);
        assertEquals(CLIENT_ID, savedReceipt.getClientId());
        assertEquals(STORE_ID, savedReceipt.getStoreId());
        assertEquals(35.0, savedReceipt.getTotalPrice(), 0.001);
        assertEquals(PAYMENT_DETAILS, savedReceipt.getPaymentDetails());
        assertEquals(2, savedReceipt.getProducts().size());
    }
    
    @Test
    public void testSavePurchase() {
        // Create test products
        Map<Product, Pair<Integer, Double>> products = new HashMap<>();
        products.put(createProduct("product1", "Product 1"), new Pair<>(2, 10.0));
        products.put(createProduct("product2", "Product 2"), new Pair<>(1, 15.0));
        
        // Save the purchase
        String receiptId = repository.savePurchase(CLIENT_ID, STORE_ID, products, 35.0, PAYMENT_DETAILS);
        
        // Verify the purchase was saved
        assertNotNull(receiptId);
        Receipt savedReceipt = repository.getReceipt(receiptId);
        assertNotNull(savedReceipt);
        assertEquals(CLIENT_ID, savedReceipt.getClientId());
        assertEquals(STORE_ID, savedReceipt.getStoreId());
        assertEquals(35.0, savedReceipt.getTotalPrice(), 0.001);
        assertEquals(PAYMENT_DETAILS, savedReceipt.getPaymentDetails());
        assertEquals(2, savedReceipt.getProducts().size());
    }
    
    @Test
    public void testGetReceipt_ExistingReceipt() {
        // Create and save a receipt
        Map<Product, Pair<Integer, Double>> products = new HashMap<>();
        products.put(createProduct("product1", "Product 1"), new Pair<>(2, 10.0));
        
        Receipt receipt = new Receipt(CLIENT_ID, STORE_ID, products, 20.0, PAYMENT_DETAILS);
        String receiptId = repository.saveReceipt(receipt);
        
        // Get the receipt
        Receipt retrievedReceipt = repository.getReceipt(receiptId);
        
        // Verify the retrieved receipt
        assertNotNull(retrievedReceipt);
        assertEquals(CLIENT_ID, retrievedReceipt.getClientId());
        assertEquals(STORE_ID, retrievedReceipt.getStoreId());
    }
    
    @Test
    public void testGetReceipt_NonExistingReceipt() {
        Receipt retrievedReceipt = repository.getReceipt("nonexistent");
        
        assertNull(retrievedReceipt);
    }
    
    @Test
    public void testGetClientReceipts() {
        // Create and save receipts for multiple clients
        Map<Product, Pair<Integer, Double>> products1 = new HashMap<>();
        products1.put(createProduct("product1", "Product 1"), new Pair<>(2, 10.0));
        
        Map<Product, Pair<Integer, Double>> products2 = new HashMap<>();
        products2.put(createProduct("product2", "Product 2"), new Pair<>(1, 15.0));
        
        repository.savePurchase(CLIENT_ID, STORE_ID, products1, 20.0, PAYMENT_DETAILS);
        repository.savePurchase(CLIENT_ID, "store456", products2, 15.0, PAYMENT_DETAILS);
        repository.savePurchase("client456", STORE_ID, products1, 20.0, PAYMENT_DETAILS);
        
        // Get the client's receipts
        List<Receipt> clientReceipts = repository.getClientReceipts(CLIENT_ID);
        
        // Verify the retrieved receipts
        assertEquals(2, clientReceipts.size());
        for (Receipt receipt : clientReceipts) {
            assertEquals(CLIENT_ID, receipt.getClientId());
        }
    }
    
    @Test
    public void testGetStoreReceipts() {
        // Create and save receipts for multiple stores
        Map<Product, Pair<Integer, Double>> products1 = new HashMap<>();
        products1.put(createProduct("product1", "Product 1"), new Pair<>(2, 10.0));
        
        Map<Product, Pair<Integer, Double>> products2 = new HashMap<>();
        products2.put(createProduct("product2", "Product 2"), new Pair<>(1, 15.0));
        
        repository.savePurchase(CLIENT_ID, STORE_ID, products1, 20.0, PAYMENT_DETAILS);
        repository.savePurchase("client456", STORE_ID, products2, 15.0, PAYMENT_DETAILS);
        repository.savePurchase(CLIENT_ID, "store456", products1, 20.0, PAYMENT_DETAILS);
        
        // Get the store's receipts
        List<Receipt> storeReceipts = repository.getStoreReceipts(STORE_ID);
        
        // Verify the retrieved receipts
        assertEquals(2, storeReceipts.size());
        for (Receipt receipt : storeReceipts) {
            assertEquals(STORE_ID, receipt.getStoreId());
        }
    }
    
    @Test
    public void testGetClientStoreReceipts() {
        // Create and save receipts for multiple stores
        Map<Product, Pair<Integer, Double>> products1 = new HashMap<>();
        products1.put(createProduct("product1", "Product 1"), new Pair<>(2, 10.0));
        
        Map<Product, Pair<Integer, Double>> products2 = new HashMap<>();
        products2.put(createProduct("product2", "Product 2"), new Pair<>(1, 15.0));
        
        repository.savePurchase(CLIENT_ID, STORE_ID, products1, 20.0, PAYMENT_DETAILS);
        repository.savePurchase(CLIENT_ID, STORE_ID, products2, 15.0, PAYMENT_DETAILS);
        repository.savePurchase(CLIENT_ID, "store456", products1, 20.0, PAYMENT_DETAILS);
        repository.savePurchase("client456", STORE_ID, products1, 20.0, PAYMENT_DETAILS);
        
        // Get the client-store receipts
        List<Receipt> clientStoreReceipts = repository.getClientStoreReceipts(CLIENT_ID, STORE_ID);
        
        // Verify the retrieved receipts
        assertEquals(2, clientStoreReceipts.size());
        for (Receipt receipt : clientStoreReceipts) {
            assertEquals(CLIENT_ID, receipt.getClientId());
            assertEquals(STORE_ID, receipt.getStoreId());
        }
    }
    
    @Test
    public void testClear() {
        // Create and save receipts
        Map<Product, Pair<Integer, Double>> products = new HashMap<>();
        products.put(createProduct("product1", "Product 1"), new Pair<>(2, 10.0));
        
        repository.savePurchase(CLIENT_ID, STORE_ID, products, 20.0, PAYMENT_DETAILS);
        repository.savePurchase("client456", "store456", products, 20.0, PAYMENT_DETAILS);
        
        // Verify receipts were saved
        assertFalse(repository.getClientReceipts(CLIENT_ID).isEmpty());
        
        // Clear the repository
        repository.clear();
        
        // Verify all receipts were removed
        assertTrue(repository.getClientReceipts(CLIENT_ID).isEmpty());
        assertTrue(repository.getStoreReceipts(STORE_ID).isEmpty());
    }
    
    /**
     * Helper method to create a product for testing
     */
    private Product createProduct(String id, String name) {
        Set<Category> categories = new LinkedHashSet<>();
        categories.add(new Category("cat1", "Category 1"));
        return new Product(id, name, categories);
    }
}