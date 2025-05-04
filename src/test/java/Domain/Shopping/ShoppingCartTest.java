package Domain.Shopping;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

/**
 * Test class for ShoppingCart
 */
public class ShoppingCartTest {
    
    private ShoppingCart cart;
    private static final String CLIENT_ID = "client123";
    private static final String STORE_ID = "store123";
    private static final String STORE_ID2 = "store456";
    
    @Before
    public void setUp() {
        cart = new ShoppingCart(CLIENT_ID);
    }
    
    @Test
    public void testConstructor_Default() {
        assertEquals(CLIENT_ID, cart.getClientId());
        assertTrue(cart.getCart().isEmpty());
    }
    
    @Test
    public void testConstructor_WithBaskets() {
        Set<String> stores = new HashSet<>();
        stores.add(STORE_ID);
        stores.add(STORE_ID2);
        
        ShoppingCart cartWithStores = new ShoppingCart(CLIENT_ID, stores);
        
        assertEquals(CLIENT_ID, cartWithStores.getClientId());
        assertEquals(2, cartWithStores.getCart().size());
        assertTrue(cartWithStores.getCart().contains(STORE_ID));
        assertTrue(cartWithStores.getCart().contains(STORE_ID2));
    }
    
    @Test
    public void testGetCart_ReturnsCopy() {
        cart.addStore(STORE_ID);
        
        Set<String> returnedCart = cart.getCart();
        returnedCart.add(STORE_ID2); // This should not affect the original cart
        
        assertEquals(1, cart.getCart().size());
        assertTrue(cart.getCart().contains(STORE_ID));
        assertFalse(cart.getCart().contains(STORE_ID2));
    }
    
    @Test
    public void testAddStore() {
        cart.addStore(STORE_ID);
        
        assertEquals(1, cart.getCart().size());
        assertTrue(cart.getCart().contains(STORE_ID));
    }
    
    @Test
    public void testAddStore_DuplicateStore() {
        cart.addStore(STORE_ID);
        cart.addStore(STORE_ID); // Add the same store again
        
        assertEquals(1, cart.getCart().size());
        assertTrue(cart.getCart().contains(STORE_ID));
    }
    
    @Test
    public void testRemoveStore_ExistingStore() {
        cart.addStore(STORE_ID);
        cart.addStore(STORE_ID2);
        
        cart.removeStore(STORE_ID);
        
        assertEquals(1, cart.getCart().size());
        assertFalse(cart.getCart().contains(STORE_ID));
        assertTrue(cart.getCart().contains(STORE_ID2));
    }
    
    @Test
    public void testRemoveStore_NonExistingStore() {
        cart.addStore(STORE_ID);
        
        cart.removeStore("nonexistent");
        
        assertEquals(1, cart.getCart().size());
        assertTrue(cart.getCart().contains(STORE_ID));
    }
    
    @Test
    public void testHasStore_ExistingStore() {
        cart.addStore(STORE_ID);
        
        assertTrue(cart.hasStore(STORE_ID));
    }
    
    @Test
    public void testHasStore_NonExistingStore() {
        assertFalse(cart.hasStore(STORE_ID));
    }
    
    @Test
    public void testClear() {
        cart.addStore(STORE_ID);
        cart.addStore(STORE_ID2);
        
        cart.clear();
        
        assertTrue(cart.getCart().isEmpty());
    }
    
    @Test
    public void testIsEmpty_Empty() {
        assertTrue(cart.isEmpty());
    }
    
    @Test
    public void testIsEmpty_NotEmpty() {
        cart.addStore(STORE_ID);
        
        assertFalse(cart.isEmpty());
    }
}