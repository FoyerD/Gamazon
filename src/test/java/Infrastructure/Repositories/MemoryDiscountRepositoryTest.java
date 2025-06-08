package Infrastructure.Repositories;


import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import Domain.Store.Discounts.Discount;
import Infrastructure.MemoryRepositories.MemoryDiscountRepository;

public class MemoryDiscountRepositoryTest {
    
    @Mock
    private Discount discount1;
    
    @Mock
    private Discount discount2;
    
    @Mock
    private Discount discount3;
    
    private MemoryDiscountRepository repository;
    private String discount1Id;
    private String discount2Id;
    private String discount3Id;
    private String testStoreId1 = "store1";
    private String testStoreId2 = "store2";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        repository = new MemoryDiscountRepository();
        
        discount1Id = UUID.randomUUID().toString();
        discount2Id = UUID.randomUUID().toString();
        discount3Id = UUID.randomUUID().toString();
        
        when(discount1.getId()).thenReturn(discount1Id);
        when(discount2.getId()).thenReturn(discount2Id);
        when(discount3.getId()).thenReturn(discount3Id);
        
        when(discount1.getStoreId()).thenReturn(testStoreId1);
        when(discount2.getStoreId()).thenReturn(testStoreId1);
        when(discount3.getStoreId()).thenReturn(testStoreId2);
    }
    
    @After
    public void tearDown() {
        repository.deleteAll();
    }
    
    // ===========================================
    // ADD TESTS
    // ===========================================
    
    @Test
    public void testAddDiscount() {
        boolean result = repository.add(discount1Id, discount1);
        
        assertTrue(result);
        assertTrue(repository.exists(discount1Id));
        assertEquals(1, repository.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddNullDiscount() {
        repository.add(discount1Id, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddDiscountWithNullId() {
        Discount discountWithNullId = mock(Discount.class);
        when(discountWithNullId.getId()).thenReturn(null);
        
        repository.add(discount1Id, discountWithNullId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddWithNullDiscountId() {
        repository.add(null, discount1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddWithEmptyDiscountId() {
        repository.add("   ", discount1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddWithMismatchedIds() {
        String differentId = UUID.randomUUID().toString();
        repository.add(differentId, discount1);
    }
    
    @Test
    public void testAddMultipleDiscounts() {
        repository.add(discount1Id, discount1);
        repository.add(discount2Id, discount2);
        
        assertTrue(repository.exists(discount1Id));
        assertTrue(repository.exists(discount2Id));
        assertEquals(2, repository.size());
    }
    
    @Test
    public void testAddSameDiscountTwice() {
        // First add should return true (new entry)
        boolean firstAdd = repository.add(discount1Id, discount1);
        assertTrue(firstAdd);
        
        // Second add should return false (existing entry)
        boolean secondAdd = repository.add(discount1Id, discount1);
        assertFalse(secondAdd);
        
        assertEquals(1, repository.size());
    }
    
    // ===========================================
    // GET TESTS
    // ===========================================
    
    @Test
    public void testGet() {
        repository.add(discount1Id, discount1);
        
        Discount found = repository.get(discount1Id);
        assertEquals(discount1, found);
    }
    
    @Test
    public void testGetNotFound() {
        Discount found = repository.get(UUID.randomUUID().toString());
        assertNull(found);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetWithNull() {
        repository.get(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetWithEmpty() {
        repository.get("   ");
    }
    
    // ===========================================
    // REMOVE TESTS
    // ===========================================
    
    @Test
    public void testRemove() {
        repository.add(discount1Id, discount1);
        assertTrue(repository.exists(discount1Id));
        
        Discount removed = repository.remove(discount1Id);
        assertEquals(discount1, removed);
        assertFalse(repository.exists(discount1Id));
        assertEquals(0, repository.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithNull() {
        repository.remove(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithEmpty() {
        repository.remove("   ");
    }
    
    @Test
    public void testRemoveNotFound() {
        Discount removed = repository.remove(UUID.randomUUID().toString());
        assertNull(removed);
        assertEquals(0, repository.size());
    }
    
    // ===========================================
    // EXISTS TESTS
    // ===========================================
    
    @Test
    public void testExists() {
        assertFalse(repository.exists(discount1Id));
        
        repository.add(discount1Id, discount1);
        assertTrue(repository.exists(discount1Id));
        
        repository.remove(discount1Id);
        assertFalse(repository.exists(discount1Id));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testExistsWithNull() {
        repository.exists(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExistsWithEmpty() {
        repository.exists("");
    }

    // ===========================================
    // SIZE TESTS
    // ===========================================
    
    @Test
    public void testSize() {
        assertEquals(0, repository.size());
        
        repository.add(discount1Id, discount1);
        assertEquals(1, repository.size());
        
        repository.add(discount2Id, discount2);
        assertEquals(2, repository.size());
        
        repository.add(discount3Id, discount3);
        assertEquals(3, repository.size());
        
        repository.remove(discount1Id);
        assertEquals(2, repository.size());
    }
    
    // ===========================================
    // DELETE ALL TESTS
    // ===========================================
    
    @Test
    public void testDeleteAll() {
        repository.add(discount1Id, discount1);
        repository.add(discount2Id, discount2);
        repository.add(discount3Id, discount3);
        assertEquals(3, repository.size());
        
        repository.deleteAll();
        assertEquals(0, repository.size());
        assertFalse(repository.exists(discount1Id));
        assertFalse(repository.exists(discount2Id));
        assertFalse(repository.exists(discount3Id));
        
        assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
        assertTrue(repository.getStoreDiscounts(testStoreId2).isEmpty());
    }
    
    // ===========================================
    // STORE-SPECIFIC TESTS
    // ===========================================
    
    @Test
    public void testGetStoreDiscounts() {
        repository.add(discount1Id, discount1);
        repository.add(discount2Id, discount2);
        repository.add(discount3Id, discount3);
        
        List<Discount> store1Discounts = repository.getStoreDiscounts(testStoreId1);
        List<Discount> store2Discounts = repository.getStoreDiscounts(testStoreId2);
        
        assertEquals(2, store1Discounts.size());
        assertEquals(1, store2Discounts.size());
        
        assertTrue(store1Discounts.contains(discount1));
        assertTrue(store1Discounts.contains(discount2));
        assertFalse(store1Discounts.contains(discount3));
        
        assertFalse(store2Discounts.contains(discount1));
        assertFalse(store2Discounts.contains(discount2));
        assertTrue(store2Discounts.contains(discount3));
    }
    
    @Test
    public void testGetStoreDiscountsEmpty() {
        List<Discount> discounts = repository.getStoreDiscounts(testStoreId1);
        assertTrue(discounts.isEmpty());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetStoreDiscountsWithNullStoreId() {
        repository.getStoreDiscounts(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetStoreDiscountsWithEmptyStoreId() {
        repository.getStoreDiscounts("   ");
    }
    
    @Test
    public void testGetStoreDiscountsReturnsDefensiveCopy() {
        repository.add(discount1Id, discount1);
        
        List<Discount> discounts = repository.getStoreDiscounts(testStoreId1);
        discounts.clear(); // Should not affect repository
        
        List<Discount> discountsAgain = repository.getStoreDiscounts(testStoreId1);
        assertEquals(1, discountsAgain.size());
        assertTrue(discountsAgain.contains(discount1));
    }
    
    // ===========================================
    // UPDATE TESTS
    // ===========================================
    
    @Test
    public void testUpdate() {
        repository.add(discount1Id, discount1);
        
        Discount previousDiscount = repository.update(discount1Id, discount1);
        assertEquals(discount1, previousDiscount);
        assertEquals(1, repository.size());
        assertEquals(discount1, repository.get(discount1Id));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithNullId() {
        repository.update(null, discount1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithEmptyId() {
        repository.update("   ", discount1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithNullDiscount() {
        repository.update(discount1Id, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithMismatchedIds() {
        String differentId = UUID.randomUUID().toString();
        repository.update(differentId, discount1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNonExistentDiscount() {
        repository.update(discount1Id, discount1);
    }
    
    // ===========================================
    // INTEGRATION TESTS
    // ===========================================
    
    @Test
    public void testCompleteWorkflow() {
        // Add discounts
        repository.add(discount1Id, discount1);
        repository.add(discount2Id, discount2);
        repository.add(discount3Id, discount3);
        
        // Verify global state
        assertEquals(3, repository.size());
        assertTrue(repository.exists(discount1Id));
        assertTrue(repository.exists(discount2Id));
        assertTrue(repository.exists(discount3Id));
        
        // Verify store-specific state
        List<Discount> store1Discounts = repository.getStoreDiscounts(testStoreId1);
        List<Discount> store2Discounts = repository.getStoreDiscounts(testStoreId2);
        
        assertEquals(2, store1Discounts.size());
        assertEquals(1, store2Discounts.size());
        
        assertTrue(store1Discounts.contains(discount1));
        assertTrue(store1Discounts.contains(discount2));
        assertFalse(store1Discounts.contains(discount3));
        
        assertFalse(store2Discounts.contains(discount1));
        assertFalse(store2Discounts.contains(discount2));
        assertTrue(store2Discounts.contains(discount3));
        
        // Update a discount
        repository.update(discount1Id, discount1);
        assertEquals(3, repository.size());
        
        // Remove specific discount
        repository.remove(discount1Id);
        assertEquals(2, repository.size());
        assertFalse(repository.exists(discount1Id));
        
        // Verify store state after removal
        store1Discounts = repository.getStoreDiscounts(testStoreId1);
        assertEquals(1, store1Discounts.size());
        assertFalse(store1Discounts.contains(discount1));
        assertTrue(store1Discounts.contains(discount2));
        
        // Clear all
        repository.deleteAll();
        assertEquals(0, repository.size());
        assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
        assertTrue(repository.getStoreDiscounts(testStoreId2).isEmpty());
    }
    
    @Test
    public void testStoreIsolation() {
        // Add discounts to different stores
        repository.add(discount1Id, discount1);
        repository.add(discount3Id, discount3);
        
        // Verify isolation
        List<Discount> store1Discounts = repository.getStoreDiscounts(testStoreId1);
        List<Discount> store2Discounts = repository.getStoreDiscounts(testStoreId2);
        
        assertEquals(1, store1Discounts.size());
        assertEquals(1, store2Discounts.size());
        assertTrue(store1Discounts.contains(discount1));
        assertTrue(store2Discounts.contains(discount3));
        
        // Remove one discount should not affect the other store
        repository.remove(discount1Id);
        
        assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
        assertEquals(1, repository.getStoreDiscounts(testStoreId2).size());
        assertTrue(repository.exists(discount3Id));
        assertFalse(repository.exists(discount1Id));
    }
    
    @Test
    public void testMultipleDiscountsSameStore() {
        repository.add(discount1Id, discount1);
        repository.add(discount2Id, discount2);
        
        List<Discount> storeDiscounts = repository.getStoreDiscounts(testStoreId1);
        assertEquals(2, storeDiscounts.size());
        assertTrue(storeDiscounts.contains(discount1));
        assertTrue(storeDiscounts.contains(discount2));
        
        // Remove one discount
        repository.remove(discount1Id);
        
        storeDiscounts = repository.getStoreDiscounts(testStoreId1);
        assertEquals(1, storeDiscounts.size());
        assertFalse(storeDiscounts.contains(discount1));
        assertTrue(storeDiscounts.contains(discount2));
    }
    
    @Test
    public void testEmptyRepository() {
        assertEquals(0, repository.size());
        assertFalse(repository.exists(discount1Id));
        assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
        assertNull(repository.get(discount1Id));
        assertNull(repository.remove(discount1Id));
    }
    
    // ===========================================
    // EDGE CASE TESTS
    // ===========================================
    
    @Test
    public void testWhitespaceHandling() {
        String whitespaceId = "   ";
        
        try {
            repository.add(whitespaceId, discount1);
            fail("Should throw IllegalArgumentException for whitespace ID");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            repository.get(whitespaceId);
            fail("Should throw IllegalArgumentException for whitespace ID");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            repository.remove(whitespaceId);
            fail("Should throw IllegalArgumentException for whitespace ID");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            repository.getStoreDiscounts(whitespaceId);
            fail("Should throw IllegalArgumentException for whitespace store ID");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test
    public void testConcurrentModification() {
        repository.add(discount1Id, discount1);
        repository.add(discount2Id, discount2);
        
        List<Discount> discounts = repository.getStoreDiscounts(testStoreId1);
        
        // Modify repository while holding reference to returned list
        repository.add(discount3Id, discount3);
        
        // Original list should not be affected (defensive copy)
        assertEquals(2, discounts.size());
        
        // New query should show updated state
        List<Discount> updatedDiscounts = repository.getStoreDiscounts(testStoreId1);
        assertEquals(2, updatedDiscounts.size()); // discount3 is in different store
    }
}