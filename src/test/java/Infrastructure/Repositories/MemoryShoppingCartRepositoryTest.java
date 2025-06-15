package Infrastructure.Repositories;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import Domain.Shopping.IShoppingCart;
import Domain.Shopping.ShoppingCartFactory;
import Infrastructure.MemoryRepositories.MemoryShoppingCartRepository;

/**
 * Test class for MemoryShoppingCartRepository.
 * Tests basic CRUD operations for shopping cart repository.
 */
public class MemoryShoppingCartRepositoryTest {
    
    private MemoryShoppingCartRepository repository;
    private final String testClientId = "client123";
    private IShoppingCart testCart;
    
    @Before
    public void setUp() {
        repository = new MemoryShoppingCartRepository();
        testCart = createTestCart();
    }
    
    /**
     * Helper method to create a test shopping cart.
     * Uses the ShoppingCartFactory to maintain encapsulation.
     */
    private IShoppingCart createTestCart() {
        try {
            Set<String> testStores = new HashSet<>();
            testStores.add("store1");
            testStores.add("store2");
            
            // Use the factory to create a shopping cart instead of direct instantiation
            return ShoppingCartFactory.createShoppingCart(testClientId, testStores);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test cart", e);
        }
    }
    
    @Test
    public void testAdd_Success() {
        // Arrange & Act
        boolean result = repository.add(testClientId, testCart);
        
        // Assert
        assertTrue("Should return true when adding a new cart", result);
        assertEquals("Should be able to retrieve the added cart", testCart, repository.get(testClientId));
    }
    
    @Test
    public void testAdd_DuplicateId() {
        // Arrange
        repository.add(testClientId, testCart);
        IShoppingCart anotherCart = ShoppingCartFactory.createShoppingCart(testClientId);
        
        // Act
        boolean result = repository.add(testClientId, anotherCart);
        
        // Assert
        assertFalse("Should return false when adding a cart with existing ID", result);
        assertEquals("Should not replace the existing cart", testCart, repository.get(testClientId));
    }
    
    @Test
    public void testGet_ExistingId() {
        // Arrange
        repository.add(testClientId, testCart);
        
        // Act
        IShoppingCart result = repository.get(testClientId);
        
        // Assert
        assertEquals("Should return the correct cart", testCart, result);
    }
    
    @Test
    public void testGet_NonExistingId() {
        // Act
        IShoppingCart result = repository.get("nonexistent");
        
        // Assert
        assertNull("Should return null for non-existing ID", result);
    }
    
    @Test
    public void testUpdate_ExistingId() {
        // Arrange
        repository.add(testClientId, testCart);
        Set<String> newStores = new HashSet<>();
        newStores.add("store3");
        IShoppingCart updatedCart = ShoppingCartFactory.createShoppingCart(testClientId, newStores);
        
        // Act
        IShoppingCart result = repository.update(testClientId, updatedCart);
        
        // Assert
        assertEquals("Should return the updated cart", updatedCart, result);
        assertEquals("Should update the cart in the repository", updatedCart, repository.get(testClientId));
    }
    
    @Test
    public void testUpdate_NonExistingId() {
        // Arrange
        IShoppingCart nonExistentCart = ShoppingCartFactory.createShoppingCart("nonexistent");
        
        // Act
        IShoppingCart result = repository.update("nonexistent", nonExistentCart);
        
        // Assert
        assertNull("Should return null for updating non-existing ID", result);
        assertNull("Should not add the cart to the repository", repository.get("nonexistent"));
    }
    
    @Test
    public void testRemove_ExistingId() {
        // Arrange
        repository.add(testClientId, testCart);
        
        // Act
        IShoppingCart result = repository.remove(testClientId);
        
        // Assert
        assertEquals("Should return the removed cart", testCart, result);
        assertNull("Should remove the cart from the repository", repository.get(testClientId));
    }
    
    @Test
    public void testRemove_NonExistingId() {
        // Act
        IShoppingCart result = repository.remove("nonexistent");
        
        // Assert
        assertNull("Should return null for removing non-existing ID", result);
    }
    
    @Test
    public void testClear() {
        // Arrange
        repository.add(testClientId, testCart);
        repository.add("client456", ShoppingCartFactory.createShoppingCart("client456"));
        
        // Act
        repository.clear();
        
        // Assert
        assertNull("Should remove all carts", repository.get(testClientId));
        assertNull("Should remove all carts", repository.get("client456"));
    }
}