package Infrastructure.Repositories;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import Domain.Shopping.IShoppingCart;
import Domain.Shopping.IShoppingCart;

/**
 * Test class for MemoryShoppingCartRepository
 */
public class MemoryShoppingCartRepositoryTest {
    
    private MemoryShoppingCartRepository repository;
    private IShoppingCart cart;
    private static final String CLIENT_ID = "client123";
    private static final String STORE_ID = "store123";
    
    @Before
    public void setUp() {
        repository = new MemoryShoppingCartRepository();
        // Since ShoppingCart constructor is not public in the package, we need to use reflection or a factory method
        // Here's a simplified approach to create a test cart
        cart = createTestCart(CLIENT_ID, STORE_ID);
    }
    
    /**
     * Helper method to create a test cart since ShoppingCart constructor is not public
     */
    private IShoppingCart createTestCart(String clientId, String storeId) {
        try {
            // Use reflection to create a ShoppingCart instance
            Class<?> cartClass = Class.forName("Domain.Shopping.ShoppingCart");
            Object cartInstance = cartClass.getDeclaredConstructor(String.class)
                    .newInstance(clientId);
            
            // Add the store to the cart
            cartClass.getMethod("addStore", String.class).invoke(cartInstance, storeId);
            
            return (IShoppingCart) cartInstance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test cart", e);
        }
    }
    
    @Test
    public void testAdd_Success() {
        assertTrue(repository.add(CLIENT_ID, cart));
        
        // Verify cart was added
        IShoppingCart retrievedCart = repository.get(CLIENT_ID);
        assertNotNull(retrievedCart);
        assertEquals(CLIENT_ID, retrievedCart.getClientId());
        assertTrue(retrievedCart.hasStore(STORE_ID));
    }
    
    @Test
    public void testAdd_DuplicateId() {
        repository.add(CLIENT_ID, cart);
        
        IShoppingCart anotherCart = createTestCart(CLIENT_ID, "store456");
        
        assertFalse(repository.add(CLIENT_ID, anotherCart));
        
        // Verify original cart is still there
        IShoppingCart retrievedCart = repository.get(CLIENT_ID);
        assertTrue(retrievedCart.hasStore(STORE_ID));
        assertFalse(retrievedCart.hasStore("store456"));
    }
    
    @Test
    public void testRemove_ExistingId() {
        repository.add(CLIENT_ID, cart);
        
        IShoppingCart removedCart = repository.remove(CLIENT_ID);
        
        assertNotNull(removedCart);
        assertEquals(CLIENT_ID, removedCart.getClientId());
        
        // Verify cart is gone
        assertNull(repository.get(CLIENT_ID));
    }
    
    @Test
    public void testRemove_NonExistingId() {
        IShoppingCart removedCart = repository.remove("nonexistent");
        
        assertNull(removedCart);
    }
    
    @Test
    public void testGet_ExistingId() {
        repository.add(CLIENT_ID, cart);
        
        IShoppingCart retrievedCart = repository.get(CLIENT_ID);
        
        assertNotNull(retrievedCart);
        assertEquals(CLIENT_ID, retrievedCart.getClientId());
        assertTrue(retrievedCart.hasStore(STORE_ID));
    }
    
    @Test
    public void testGet_NonExistingId() {
        IShoppingCart retrievedCart = repository.get("nonexistent");
        
        assertNull(retrievedCart);
    }
    
    @Test
    public void testUpdate_ExistingId() {
        repository.add(CLIENT_ID, cart);
        
        IShoppingCart updatedCart = createTestCart(CLIENT_ID, "store456");
        
        IShoppingCart result = repository.update(CLIENT_ID, updatedCart);
        
        assertNotNull(result);
        assertEquals(CLIENT_ID, result.getClientId());
        
        // Verify cart was updated
        IShoppingCart retrievedCart = repository.get(CLIENT_ID);
        assertFalse(retrievedCart.hasStore(STORE_ID));
        assertTrue(retrievedCart.hasStore("store456"));
    }
    
    @Test
    public void testUpdate_NonExistingId() {
        IShoppingCart updatedCart = createTestCart(CLIENT_ID, "store456");
        
        IShoppingCart result = repository.update("nonexistent", updatedCart);
        
        assertNull(result);
    }
    
    @Test
    public void testClear() {
        repository.add(CLIENT_ID, cart);
        
        String clientId2 = "client456";
        IShoppingCart cart2 = createTestCart(clientId2, STORE_ID);
        repository.add(clientId2, cart2);
        
        repository.clear();
        
        assertNull(repository.get(CLIENT_ID));
        assertNull(repository.get(clientId2));
    }
}