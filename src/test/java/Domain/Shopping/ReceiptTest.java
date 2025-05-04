package Domain.Shopping;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import Domain.Store.Category;
import Domain.Store.Product;

/**
 * Test class for Receipt
 */
public class ReceiptTest {
    
    private Receipt receipt;
    private Map<Product, Integer> products;
    private static final String CLIENT_ID = "client123";
    private static final String STORE_ID = "store123";
    private static final double TOTAL_PRICE = 35.0;
    private static final String PAYMENT_DETAILS = "xxxx-xxxx-xxxx-1234";
    
    @Before
    public void setUp() {
        products = new HashMap<>();
        Product product1 = createProduct("product1", "Product 1");
        Product product2 = createProduct("product2", "Product 2");
        
        products.put(product1, 2);
        products.put(product2, 1);
        
        receipt = new Receipt(CLIENT_ID, STORE_ID, products, TOTAL_PRICE, PAYMENT_DETAILS);
    }
    
    /**
     * Helper method to create a product for testing
     */
    private Product createProduct(String id, String name) {
        Set<Category> categories = new LinkedHashSet<>();
        categories.add(new Category("cat1", "Category 1"));
        return new Product(id, name, categories);
    }
    
    @Test
    public void testConstructor() {
        assertNotNull(receipt.getReceiptId());
        assertEquals(CLIENT_ID, receipt.getClientId());
        assertEquals(STORE_ID, receipt.getStoreId());
        assertEquals(TOTAL_PRICE, receipt.getTotalPrice(), 0.001);
        assertEquals(PAYMENT_DETAILS, receipt.getPaymentDetails());
        
        // Check that products map is not the same instance but contains the same products
        assertNotSame(products, receipt.getProducts());
        assertEquals(products.size(), receipt.getProducts().size());
        
        // Verify timestamp is set reasonably (within the last few seconds)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timestamp = receipt.getTimestamp();
        assertTrue(timestamp.isBefore(now) || timestamp.isEqual(now));
        assertTrue(timestamp.isAfter(now.minusSeconds(10)));
    }
    
    @Test
    public void testGetReceiptId() {
        assertNotNull(receipt.getReceiptId());
        assertTrue(receipt.getReceiptId().length() > 0);
    }
    
    @Test
    public void testGetClientId() {
        assertEquals(CLIENT_ID, receipt.getClientId());
    }
    
    @Test
    public void testGetStoreId() {
        assertEquals(STORE_ID, receipt.getStoreId());
    }
    
    @Test
    public void testGetProducts() {
        Map<Product, Integer> retrievedProducts = receipt.getProducts();
        
        assertEquals(2, retrievedProducts.size());
        
        // Verify that the returned map is a copy (products are immutable)
        retrievedProducts.clear();
        assertEquals(2, receipt.getProducts().size());
    }
    
    @Test
    public void testGetTimestamp() {
        assertNotNull(receipt.getTimestamp());
        
        // Check that timestamp is recent
        LocalDateTime now = LocalDateTime.now();
        assertTrue(receipt.getTimestamp().isBefore(now.plusSeconds(1)));
        assertTrue(receipt.getTimestamp().isAfter(now.minusMinutes(1)));
    }
    
    @Test
    public void testGetTotalPrice() {
        assertEquals(TOTAL_PRICE, receipt.getTotalPrice(), 0.001);
    }
    
    @Test
    public void testGetPaymentDetails() {
        assertEquals(PAYMENT_DETAILS, receipt.getPaymentDetails());
    }
    
    @Test
    public void testToMap() {
        Map<String, Object> map = receipt.toMap();
        
        assertEquals(receipt.getReceiptId(), map.get("receiptId"));
        assertEquals(CLIENT_ID, map.get("clientId"));
        assertEquals(STORE_ID, map.get("storeId"));
        assertEquals(receipt.getTimestamp(), map.get("timestamp"));
        assertEquals(TOTAL_PRICE, map.get("totalPrice"));
        assertEquals(PAYMENT_DETAILS, map.get("paymentDetails"));
        
        @SuppressWarnings("unchecked")
        Map<String, Integer> productQuantities = (Map<String, Integer>) map.get("products");
        assertNotNull(productQuantities);
        assertEquals(2, productQuantities.size());
        assertTrue(productQuantities.containsKey("product1"));
        assertTrue(productQuantities.containsKey("product2"));
        assertEquals(Integer.valueOf(2), productQuantities.get("product1"));
        assertEquals(Integer.valueOf(1), productQuantities.get("product2"));
    }
}