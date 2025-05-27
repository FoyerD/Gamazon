package Infrastructure.Repositories;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import Domain.Store.Discounts.Discount;

public class MemoryDiscountRepositoryTest {
    
    @Mock
    private Discount discount1;
    
    @Mock
    private Discount discount2;
    
    @Mock
    private Discount discount3;
    
    private MemoryDiscountRepository repository;
    private UUID discount1Id;
    private UUID discount2Id;
    private UUID discount3Id;
    private String testStoreId1 = "store1";
    private String testStoreId2 = "store2";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        repository = new MemoryDiscountRepository();
        
        discount1Id = UUID.randomUUID();
        discount2Id = UUID.randomUUID();
        discount3Id = UUID.randomUUID();
        
        when(discount1.getId()).thenReturn(discount1Id);
        when(discount2.getId()).thenReturn(discount2Id);
        when(discount3.getId()).thenReturn(discount3Id);
    }
    
    @After
    public void tearDown() {
        repository.clear();
    }
    
    // ===========================================
    // BASIC SAVE TESTS
    // ===========================================
    
    public void testDeleteByStoreIdNotFound() {
        repository.save(testStoreId1, discount1);
        
        int deleted = repository.deleteByStoreId("nonExistentStore");
        
        assertEquals(0, deleted);
        assertEquals(1, repository.size());
    }
    
    // ===========================================
    // GLOBAL OPERATIONS TESTS
    // ===========================================
    
    @Test
    public void testFindAll() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId1, discount2);
        repository.save(testStoreId2, discount3);
        
        Map<UUID, Discount> all = repository.findAll();
        assertEquals(3, all.size());
        assertTrue(all.containsKey(discount1Id));
        assertTrue(all.containsKey(discount2Id));
        assertTrue(all.containsKey(discount3Id));
        assertEquals(discount1, all.get(discount1Id));
        assertEquals(discount2, all.get(discount2Id));
        assertEquals(discount3, all.get(discount3Id));
    }
    
    @Test
    public void testFindAllEmpty() {
        Map<UUID, Discount> all = repository.findAll();
        assertTrue(all.isEmpty());
    }
    
    @Test
    public void testFindAllReturnsDefensiveCopy() {
        repository.save(testStoreId1, discount1);
        
        Map<UUID, Discount> all = repository.findAll();
        all.clear(); // Modifying returned map should not affect repository
        
        assertEquals(1, repository.size());
        assertTrue(repository.existsById(discount1Id));
    }
    
    @Test
    public void testExistsById() {
        assertFalse(repository.existsById(discount1Id));
        
        repository.save(testStoreId1, discount1);
        assertTrue(repository.existsById(discount1Id));
        
        repository.deleteById(discount1Id);
        assertFalse(repository.existsById(discount1Id));
    }
    
    @Test
    public void testExistsByIdWithNull() {
        assertFalse(repository.existsById(null));
    }
    
    @Test
    public void testClear() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId1, discount2);
        repository.save(testStoreId2, discount3);
        assertEquals(3, repository.size());
        
        repository.clear();
        assertEquals(0, repository.size());
        assertFalse(repository.existsById(discount1Id));
        assertFalse(repository.existsById(discount2Id));
        assertFalse(repository.existsById(discount3Id));
        
        assertTrue(repository.findByStoreId(testStoreId1).isEmpty());
        assertTrue(repository.findByStoreId(testStoreId2).isEmpty());
        assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
        assertTrue(repository.getStoreDiscounts(testStoreId2).isEmpty());
    }
    
    @Test
    public void testSize() {
        assertEquals(0, repository.size());
        
        repository.save(testStoreId1, discount1);
        assertEquals(1, repository.size());
        
        repository.save(testStoreId1, discount2);
        assertEquals(2, repository.size());
        
        repository.save(testStoreId2, discount3);
        assertEquals(3, repository.size());
        
        repository.deleteById(discount1Id);
        assertEquals(2, repository.size());
    }
    
    // ===========================================
    // STORE UTILITY METHODS TESTS
    // ===========================================
    
    @Test
    public void testGetStoreDiscountCount() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId1, discount2);
        repository.save(testStoreId2, discount3);
        
        assertEquals(2, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(1, repository.getStoreDiscountCount(testStoreId2));
        assertEquals(0, repository.getStoreDiscountCount("nonExistentStore"));
    }
    
    @Test
    public void testGetStoreDiscountCountWithNullStoreId() {
        repository.save(testStoreId1, discount1);
        
        assertEquals(0, repository.getStoreDiscountCount(null));
    }
    
    @Test
    public void testGetStoreDiscountCountWithEmptyStoreId() {
        repository.save(testStoreId1, discount1);
        
        assertEquals(0, repository.getStoreDiscountCount("   "));
    }
    
    @Test
    public void testHasDiscountsForStore() {
        assertFalse(repository.hasDiscountsForStore(testStoreId1));
        
        repository.save(testStoreId1, discount1);
        assertTrue(repository.hasDiscountsForStore(testStoreId1));
        assertFalse(repository.hasDiscountsForStore(testStoreId2));
        
        repository.deleteByStoreId(testStoreId1);
        assertFalse(repository.hasDiscountsForStore(testStoreId1));
    }
    
    @Test
    public void testHasDiscountsForStoreWithNullStoreId() {
        repository.save(testStoreId1, discount1);
        
        assertFalse(repository.hasDiscountsForStore(null));
    }
    
    @Test
    public void testHasDiscountsForStoreWithEmptyStoreId() {
        repository.save(testStoreId1, discount1);
        
        assertFalse(repository.hasDiscountsForStore("   "));
    }
    
    @Test
    public void testGetStoreDiscountCounts() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId1, discount2);
        repository.save(testStoreId2, discount3);
        
        Map<String, Integer> counts = repository.getStoreDiscountCounts();
        
        assertEquals(2, counts.size());
        assertEquals(Integer.valueOf(2), counts.get(testStoreId1));
        assertEquals(Integer.valueOf(1), counts.get(testStoreId2));
    }
    
    @Test
    public void testGetStoreDiscountCountsEmpty() {
        Map<String, Integer> counts = repository.getStoreDiscountCounts();
        assertTrue(counts.isEmpty());
    }
    
    // ===========================================
    // UPDATE TESTS
    // ===========================================
    
    @Test
    public void testUpdateExistingDiscount() {
        repository.save(testStoreId1, discount1);
        assertEquals(1, repository.size());
        assertEquals(1, repository.getStoreDiscountCount(testStoreId1));
        
        // Save again with same ID should update, not create new entry
        repository.save(testStoreId1, discount1);
        assertEquals(1, repository.size());
        assertEquals(1, repository.getStoreDiscountCount(testStoreId1));
        
        Discount found = repository.findById(discount1Id);
        assertEquals(discount1, found);
    }
    
    @Test
    public void testUpdateDiscountInDifferentStore() {
        repository.save(testStoreId1, discount1);
        assertEquals(1, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(0, repository.getStoreDiscountCount(testStoreId2));
        
        // Save same discount to different store
        repository.save(testStoreId2, discount1);
        assertEquals(1, repository.size()); // Still only one global entry
        assertEquals(1, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(1, repository.getStoreDiscountCount(testStoreId2));
    }
    
    @Test
    public void testUpdateDiscountMethod() {
        repository.save(testStoreId1, discount1);
        
        Discount previousDiscount = repository.updateDiscount(testStoreId1, discount1);
        
        assertNull(previousDiscount); // First time saving to this store
        assertEquals(1, repository.size());
        assertEquals(discount1, repository.findById(discount1Id));
        
        // Update again - should return the previous discount
        Discount secondPrevious = repository.updateDiscount(testStoreId1, discount1);
        assertEquals(discount1, secondPrevious);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDiscountWithNullDiscount() {
        repository.updateDiscount(testStoreId1, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDiscountWithNullDiscountId() {
        Discount discountWithNullId = mock(Discount.class);
        when(discountWithNullId.getId()).thenReturn(null);
        
        repository.updateDiscount(testStoreId1, discountWithNullId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDiscountWithNullStoreId() {
        repository.updateDiscount(null, discount1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDiscountWithEmptyStoreId() {
        repository.updateDiscount("   ", discount1);
    }
    
    @Test
    public void testFindByStoreAndDiscountId() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId2, discount2);
        
        Discount found1 = repository.findByStoreAndDiscountId(testStoreId1, discount1Id);
        Discount found2 = repository.findByStoreAndDiscountId(testStoreId2, discount2Id);
        Discount notFound1 = repository.findByStoreAndDiscountId(testStoreId1, discount2Id);
        Discount notFound2 = repository.findByStoreAndDiscountId(testStoreId2, discount1Id);
        
        assertEquals(discount1, found1);
        assertEquals(discount2, found2);
        assertNull(notFound1);
        assertNull(notFound2);
    }
    
    @Test
    public void testFindByStoreAndDiscountIdWithNullStoreId() {
        repository.save(testStoreId1, discount1);
        
        Discount found = repository.findByStoreAndDiscountId(null, discount1Id);
        assertNull(found);
    }
    
    @Test
    public void testFindByStoreAndDiscountIdWithEmptyStoreId() {
        repository.save(testStoreId1, discount1);
        
        Discount found = repository.findByStoreAndDiscountId("   ", discount1Id);
        assertNull(found);
    }
    
    @Test
    public void testFindByStoreAndDiscountIdWithNullDiscountId() {
        repository.save(testStoreId1, discount1);
        
        Discount found = repository.findByStoreAndDiscountId(testStoreId1, null);
        assertNull(found);
    }
    
    // ===========================================
    // INTEGRATION TESTS
    // ===========================================
    
    @Test
    public void testCompleteWorkflow() {
        // Add discounts to multiple stores
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId1, discount2);
        repository.save(testStoreId2, discount2);
        repository.save(testStoreId2, discount3);
        
        // Verify global state
        assertEquals(3, repository.size());
        assertTrue(repository.existsById(discount1Id));
        assertTrue(repository.existsById(discount2Id));
        assertTrue(repository.existsById(discount3Id));
        
        // Verify store-specific state
        assertEquals(2, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(2, repository.getStoreDiscountCount(testStoreId2));
        
        List<Discount> store1Discounts = repository.findByStoreId(testStoreId1);
        List<Discount> store2Discounts = repository.findByStoreId(testStoreId2);
        
        assertTrue(store1Discounts.contains(discount1));
        assertTrue(store1Discounts.contains(discount2));
        assertFalse(store1Discounts.contains(discount3));
        
        assertFalse(store2Discounts.contains(discount1));
        assertTrue(store2Discounts.contains(discount2));
        assertTrue(store2Discounts.contains(discount3));
        
        // Verify Set-based retrieval matches List-based retrieval
        Set<Discount> store1DiscountSet = repository.getStoreDiscounts(testStoreId1);
        Set<Discount> store2DiscountSet = repository.getStoreDiscounts(testStoreId2);
        
        assertEquals(store1Discounts.size(), store1DiscountSet.size());
        assertEquals(store2Discounts.size(), store2DiscountSet.size());
        
        for (Discount discount : store1Discounts) {
            assertTrue(store1DiscountSet.contains(discount));
        }
        for (Discount discount : store2Discounts) {
            assertTrue(store2DiscountSet.contains(discount));
        }
        
        // Delete from one store
        repository.deleteByStoreId(testStoreId1);
        assertEquals(2, repository.size()); // discount2 still exists in store2
        assertEquals(0, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(2, repository.getStoreDiscountCount(testStoreId2));
        
        // Verify global state after partial deletion
        assertFalse(repository.existsById(discount1Id)); // Only in store1
        assertTrue(repository.existsById(discount2Id));  // Still in store2
        assertTrue(repository.existsById(discount3Id));  // Still in store2
    }
    
    @Test
    public void testStoreIsolation() {
        // Save different discounts to different stores
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId2, discount2);
        
        // Verify isolation
        List<Discount> store1Discounts = repository.findByStoreId(testStoreId1);
        List<Discount> store2Discounts = repository.findByStoreId(testStoreId2);
        
        assertEquals(1, store1Discounts.size());
        assertEquals(1, store2Discounts.size());
        assertEquals(discount1, store1Discounts.get(0));
        assertEquals(discount2, store2Discounts.get(0));
        
        // Delete from one store should not affect the other
        repository.deleteByStoreId(testStoreId1);
        
        assertTrue(repository.findByStoreId(testStoreId1).isEmpty());
        assertEquals(1, repository.findByStoreId(testStoreId2).size());
        assertTrue(repository.existsById(discount2Id));
        assertFalse(repository.existsById(discount1Id));
    }
    
    @Test
    public void testSharedDiscountAcrossStores() {
        // Save same discount to multiple stores
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId2, discount1);
        
        // Should be one global entry but in both stores
        assertEquals(1, repository.size());
        assertEquals(1, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(1, repository.getStoreDiscountCount(testStoreId2));
        
        // Delete by ID should remove from all stores
        repository.deleteById(discount1Id);
        
        assertEquals(0, repository.size());
        assertTrue(repository.findByStoreId(testStoreId1).isEmpty());
        assertTrue(repository.findByStoreId(testStoreId2).isEmpty());
        assertFalse(repository.existsById(discount1Id));
    }
    
    @Test
    public void testMixedScenario() {
        // Complex scenario: some shared discounts, some unique
        repository.save(testStoreId1, discount1);  // unique to store1
        repository.save(testStoreId1, discount2);  // shared
        repository.save(testStoreId2, discount2);  // shared
        repository.save(testStoreId2, discount3);  // unique to store2
        
        assertEquals(3, repository.size());
        assertEquals(2, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(2, repository.getStoreDiscountCount(testStoreId2));
        
        // Delete shared discount by ID
        repository.deleteById(discount2Id);
        
        assertEquals(2, repository.size());
        assertEquals(1, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(1, repository.getStoreDiscountCount(testStoreId2));
        
        // Verify correct discounts remain
        assertTrue(repository.findByStoreId(testStoreId1).contains(discount1));
        assertFalse(repository.findByStoreId(testStoreId1).contains(discount2));
        assertFalse(repository.findByStoreId(testStoreId1).contains(discount3));
        
        assertFalse(repository.findByStoreId(testStoreId2).contains(discount1));
        assertFalse(repository.findByStoreId(testStoreId2).contains(discount2));
        assertTrue(repository.findByStoreId(testStoreId2).contains(discount3));
    }
    public void testSaveDiscount() {
        repository.save(testStoreId1, discount1);
        
        assertTrue(repository.existsById(discount1Id));
        assertEquals(1, repository.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullDiscount() {
        repository.save(testStoreId1, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveDiscountWithNullId() {
        Discount discountWithNullId = mock(Discount.class);
        when(discountWithNullId.getId()).thenReturn(null);
        
        repository.save(testStoreId1, discountWithNullId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveWithNullStoreId() {
        repository.save(null, discount1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveWithEmptyStoreId() {
        repository.save("   ", discount1);
    }
    
    @Test
    public void testSaveToMultipleStores() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId2, discount2);
        
        assertTrue(repository.existsById(discount1Id));
        assertTrue(repository.existsById(discount2Id));
        assertEquals(2, repository.size());
    }
    
    @Test
    public void testSaveSameDiscountToMultipleStores() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId2, discount1);
        
        assertTrue(repository.existsById(discount1Id));
        assertEquals(1, repository.size()); // Same discount, so only one entry globally
        
        // Should be in both stores
        List<Discount> store1Discounts = repository.findByStoreId(testStoreId1);
        List<Discount> store2Discounts = repository.findByStoreId(testStoreId2);
        
        assertTrue(store1Discounts.contains(discount1));
        assertTrue(store2Discounts.contains(discount1));
    }
    
    // ===========================================
    // FIND TESTS
    // ===========================================
    
    @Test
    public void testFindById() {
        repository.save(testStoreId1, discount1);
        
        Discount found = repository.findById(discount1Id);
        assertEquals(discount1, found);
    }
    
    @Test
    public void testFindByIdNotFound() {
        Discount found = repository.findById(UUID.randomUUID());
        assertNull(found);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFindByIdWithNull() {
        repository.findById(null);
    }
    
    // ===========================================
    // STORE-SPECIFIC FIND TESTS
    // ===========================================
    
    @Test
    public void testFindByStoreId() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId1, discount2);
        repository.save(testStoreId2, discount3);
        
        List<Discount> store1Discounts = repository.findByStoreId(testStoreId1);
        List<Discount> store2Discounts = repository.findByStoreId(testStoreId2);
        
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
    public void testFindByStoreIdEmpty() {
        List<Discount> discounts = repository.findByStoreId(testStoreId1);
        assertTrue(discounts.isEmpty());
    }
    
    @Test
    public void testFindByStoreIdWithNullStoreId() {
        List<Discount> discounts = repository.findByStoreId(null);
        assertTrue(discounts.isEmpty());
    }
    
    @Test
    public void testFindByStoreIdWithEmptyStoreId() {
        List<Discount> discounts = repository.findByStoreId("   ");
        assertTrue(discounts.isEmpty());
    }
    
    @Test
    public void testGetStoreDiscounts() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId1, discount2);
        repository.save(testStoreId2, discount3);
        
        Set<Discount> store1Discounts = repository.getStoreDiscounts(testStoreId1);
        Set<Discount> store2Discounts = repository.getStoreDiscounts(testStoreId2);
        
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
        Set<Discount> discounts = repository.getStoreDiscounts(testStoreId1);
        assertTrue(discounts.isEmpty());
    }
    
    @Test
    public void testGetStoreDiscountsWithNullStoreId() {
        Set<Discount> discounts = repository.getStoreDiscounts(null);
        assertTrue(discounts.isEmpty());
    }
    
    @Test
    public void testGetStoreDiscountsWithEmptyStoreId() {
        Set<Discount> discounts = repository.getStoreDiscounts("   ");
        assertTrue(discounts.isEmpty());
    }
    
    @Test
    public void testFindByStoreIdReturnsDefensiveCopy() {
        repository.save(testStoreId1, discount1);
        
        List<Discount> discounts = repository.findByStoreId(testStoreId1);
        discounts.clear(); // Should not affect repository
        
        List<Discount> discountsAgain = repository.findByStoreId(testStoreId1);
        assertEquals(1, discountsAgain.size());
        assertTrue(discountsAgain.contains(discount1));
    }
    
    @Test
    public void testGetStoreDiscountsReturnsDefensiveCopy() {
        repository.save(testStoreId1, discount1);
        
        Set<Discount> discounts = repository.getStoreDiscounts(testStoreId1);
        discounts.clear(); // Should not affect repository
        
        Set<Discount> discountsAgain = repository.getStoreDiscounts(testStoreId1);
        assertEquals(1, discountsAgain.size());
        assertTrue(discountsAgain.contains(discount1));
    }
    
    // ===========================================
    // DELETE TESTS
    // ===========================================
    
    @Test
    public void testDeleteById() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId2, discount1);
        assertTrue(repository.existsById(discount1Id));
        
        repository.deleteById(discount1Id);
        assertFalse(repository.existsById(discount1Id));
        assertEquals(0, repository.size());
        
        // Should be removed from both stores
        assertTrue(repository.findByStoreId(testStoreId1).isEmpty());
        assertTrue(repository.findByStoreId(testStoreId2).isEmpty());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteByIdWithNull() {
        repository.deleteById(null);
    }
    
    @Test
    public void testDeleteByIdNotFound() {
        // Should not throw exception when deleting non-existent ID
        repository.deleteById(UUID.randomUUID());
        assertEquals(0, repository.size());
    }
    
    @Test
    public void testDeleteByStoreId() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId1, discount2);
        repository.save(testStoreId2, discount3);
        
        assertEquals(3, repository.size());
        
        int deleted = repository.deleteByStoreId(testStoreId1);
        
        assertEquals(2, deleted);
        assertEquals(1, repository.size());
        assertFalse(repository.existsById(discount1Id));
        assertFalse(repository.existsById(discount2Id));
        assertTrue(repository.existsById(discount3Id));
        
        assertTrue(repository.findByStoreId(testStoreId1).isEmpty());
        assertFalse(repository.findByStoreId(testStoreId2).isEmpty());
    }
    
    @Test
    public void testDeleteByStoreIdWithNullStoreId() {
        repository.save(testStoreId1, discount1);
        
        int deleted = repository.deleteByStoreId(null);
        
        assertEquals(0, deleted);
        assertEquals(1, repository.size());
    }
    
    @Test
    public void testDeleteByStoreIdWithEmptyStoreId() {
        repository.save(testStoreId1, discount1);
        
        int deleted = repository.deleteByStoreId("   ");
        
        assertEquals(0, deleted);
        assertEquals(1, repository.size());
    }
}