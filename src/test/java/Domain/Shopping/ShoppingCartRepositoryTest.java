package Domain.Shopping;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.HashSet;

/**
 * Test class for ShoppingCartRepository
 */
public class ShoppingCartRepositoryTest {
    
    private ShoppingCartRepository repository;
    private IShoppingCart cart;
    private static final String CLIENT_ID = "client123";
    private static final String STORE_ID = "store123";
    
    @Before
    public void setUp() {
        repository = new ShoppingCartRepository();
        cart = new ShoppingCart(CLIENT_ID);
        ((ShoppingCart)cart).addStore(STORE_ID);
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
        
        IShoppingCart anotherCart = new ShoppingCart(CLIENT_ID);
        ((ShoppingCart)anotherCart).addStore("store456");
        
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
        
        IShoppingCart updatedCart = new ShoppingCart(CLIENT_ID);
        ((ShoppingCart)updatedCart).addStore("store456");
        
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
        IShoppingCart updatedCart = new ShoppingCart(CLIENT_ID);
        ((ShoppingCart)updatedCart).addStore("store456");
        
        IShoppingCart result = repository.update("nonexistent", updatedCart);
        
        assertNull(result);
    }
    
    @Test
    public void testClear() {
        repository.add(CLIENT_ID, cart);
        
        String clientId2 = "client456";
        IShoppingCart cart2 = new ShoppingCart(clientId2);
        repository.add(clientId2, cart2);
        
        repository.clear();
        
        assertNull(repository.get(CLIENT_ID));
        assertNull(repository.get(clientId2));
    }
}