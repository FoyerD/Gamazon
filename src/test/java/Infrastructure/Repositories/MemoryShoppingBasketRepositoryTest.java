package Infrastructure.Repositories;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import Domain.Pair;
import Domain.Shopping.ShoppingBasket;
import Infrastructure.Repositories.MemoryShoppingBasketRepository;

/**
 * Test class for MemoryShoppingBasketRepository
 */
public class MemoryShoppingBasketRepositoryTest {
    
    private MemoryShoppingBasketRepository repository;
    private ShoppingBasket basket;
    private Pair<String, String> id;
    private static final String CLIENT_ID = "client123";
    private static final String STORE_ID = "store123";
    private static final String PRODUCT_ID = "product123";
    
    @Before
    public void setUp() {
        repository = new MemoryShoppingBasketRepository();
        basket = new ShoppingBasket(STORE_ID, CLIENT_ID);
        basket.addOrder(PRODUCT_ID, 3);
        id = new Pair<>(CLIENT_ID, STORE_ID);
    }
    
    @Test
    public void testAdd_Success() {
        assertTrue(repository.add(id, basket));
        
        // Verify basket was added
        ShoppingBasket retrievedBasket = repository.get(id);
        assertNotNull(retrievedBasket);
        assertEquals(STORE_ID, retrievedBasket.getStoreId());
        assertEquals(CLIENT_ID, retrievedBasket.getClientId());
        assertEquals(3, retrievedBasket.getProduct(PRODUCT_ID));
    }
    
    @Test
    public void testAdd_DuplicateId() {
        repository.add(id, basket);
        
        ShoppingBasket anotherBasket = new ShoppingBasket(STORE_ID, CLIENT_ID);
        anotherBasket.addOrder(PRODUCT_ID, 5);
        
        assertFalse(repository.add(id, anotherBasket));
        
        // Verify original basket is still there
        ShoppingBasket retrievedBasket = repository.get(id);
        assertEquals(3, retrievedBasket.getProduct(PRODUCT_ID));
    }
    
    @Test
    public void testRemove_ExistingId() {
        repository.add(id, basket);
        
        ShoppingBasket removedBasket = repository.remove(id);
        
        assertNotNull(removedBasket);
        assertEquals(basket.getStoreId(), removedBasket.getStoreId());
        assertEquals(basket.getClientId(), removedBasket.getClientId());
        
        // Verify basket is gone
        assertNull(repository.get(id));
    }
    
    @Test
    public void testRemove_NonExistingId() {
        Pair<String, String> nonExistingId = new Pair<>("nonexistent", "nonexistent");
        
        ShoppingBasket removedBasket = repository.remove(nonExistingId);
        
        assertNull(removedBasket);
    }
    
    @Test
    public void testGet_ExistingId() {
        repository.add(id, basket);
        
        ShoppingBasket retrievedBasket = repository.get(id);
        
        assertNotNull(retrievedBasket);
        assertEquals(basket.getStoreId(), retrievedBasket.getStoreId());
        assertEquals(basket.getClientId(), retrievedBasket.getClientId());
        assertEquals(3, retrievedBasket.getProduct(PRODUCT_ID));
    }
    
    @Test
    public void testGet_NonExistingId() {
        Pair<String, String> nonExistingId = new Pair<>("nonexistent", "nonexistent");
        
        ShoppingBasket retrievedBasket = repository.get(nonExistingId);
        
        assertNull(retrievedBasket);
    }
    
    @Test
    public void testUpdate_ExistingId() {
        repository.add(id, basket);
        
        ShoppingBasket updatedBasket = new ShoppingBasket(STORE_ID, CLIENT_ID);
        updatedBasket.addOrder(PRODUCT_ID, 5);
        
        ShoppingBasket result = repository.update(id, updatedBasket);
        
        assertNotNull(result);
        assertEquals(updatedBasket.getStoreId(), result.getStoreId());
        assertEquals(updatedBasket.getClientId(), result.getClientId());
        
        // Verify basket was updated
        ShoppingBasket retrievedBasket = repository.get(id);
        assertEquals(5, retrievedBasket.getProduct(PRODUCT_ID));
    }
    
    @Test
    public void testUpdate_NonExistingId() {
        Pair<String, String> nonExistingId = new Pair<>("nonexistent", "nonexistent");
        
        ShoppingBasket updatedBasket = new ShoppingBasket(STORE_ID, CLIENT_ID);
        updatedBasket.addOrder(PRODUCT_ID, 5);
        
        ShoppingBasket result = repository.update(nonExistingId, updatedBasket);
        
        assertNull(result);
    }
    
    @Test
    public void testClear() {
        repository.add(id, basket);
        
        Pair<String, String> id2 = new Pair<>(CLIENT_ID, "store456");
        ShoppingBasket basket2 = new ShoppingBasket("store456", CLIENT_ID);
        repository.add(id2, basket2);
        
        repository.clear();
        
        assertNull(repository.get(id));
        assertNull(repository.get(id2));
    }
}