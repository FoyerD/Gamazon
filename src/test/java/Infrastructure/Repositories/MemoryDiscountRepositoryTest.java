package Infrastructure.Repositories;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
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
    }
    
    @After
    public void tearDown() {
        repository.clear();
    }
    
    // ===========================================
    // BASIC SAVE TESTS
    // ===========================================
    
    //TODO! check with aviad if findAll metohd is needed
    public void testDeleteByStoreIdNotFound() {
        repository.save(testStoreId1, discount1);
        
        int deleted = repository.deleteByStoreId("nonExistentStore");
        
        assertEquals(0, deleted);
        assertEquals(1, repository.size());
    }
    
    // ===========================================
    // GLOBAL OPERATIONS TESTS
    // ===========================================
    
    // @Test
    // public void testFindAll() {
    //     repository.save(testStoreId1, discount1);
    //     repository.save(testStoreId1, discount2);
    //     repository.save(testStoreId2, discount3);
        
    //     Map<UUID, Discount> all = repository.findAll();
    //     assertEquals(3, all.size());
    //     assertTrue(all.containsKey(discount1Id));
    //     assertTrue(all.containsKey(discount2Id));
    //     assertTrue(all.containsKey(discount3Id));
    //     assertEquals(discount1, all.get(discount1Id));
    //     assertEquals(discount2, all.get(discount2Id));
    //     assertEquals(discount3, all.get(discount3Id));
    // }
    
    // @Test
    // public void testFindAllEmpty() {
    //     Map<UUID, Discount> all = repository.findAll();
    //     assertTrue(all.isEmpty());
    // }
    
    // @Test
    // public void testFindAllReturnsDefensiveCopy() {
    //     repository.save(testStoreId1, discount1);
        
    //     Map<UUID, Discount> all = repository.findAll();
    //     all.clear(); // Modifying returned map should not affect repository
        
    //     assertEquals(1, repository.size());
    //     assertTrue(repository.exists(discount1Id));
    // }
    
    @Test
    public void testexists() {
        assertFalse(repository.exists(discount1Id));
        
        repository.save(testStoreId1, discount1);
        assertTrue(repository.exists(discount1Id));
        
        repository.delete(discount1Id);
        assertFalse(repository.exists(discount1Id));
    }
    
    @Test
    public void testexistsWithNull() {
        assertFalse(repository.exists(null));
    }
    
    // @Test
    // public void testClear() {
    //     repository.save(testStoreId1, discount1);
    //     repository.save(testStoreId1, discount2);
    //     repository.save(testStoreId2, discount3);
    //     assertEquals(3, repository.size());
        
    //     repository.clear();
    //     assertEquals(0, repository.size());
    //     assertFalse(repository.exists(discount1Id));
    //     assertFalse(repository.exists(discount2Id));
    //     assertFalse(repository.exists(discount3Id));
        
    //     assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
    //     assertTrue(repository.getStoreDiscounts(testStoreId2).isEmpty());
    //     assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
    //     assertTrue(repository.getStoreDiscounts(testStoreId2).isEmpty());
    // }
    
    @Test
    public void testSize() {
        assertEquals(0, repository.size());
        
        repository.save(testStoreId1, discount1);
        assertEquals(1, repository.size());
        
        repository.save(testStoreId1, discount2);
        assertEquals(2, repository.size());
        
        repository.save(testStoreId2, discount3);
        assertEquals(3, repository.size());
        
        repository.delete(discount1Id);
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
        
        Discount found = repository.get(discount1Id);
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
        assertEquals(previousDiscount, discount1);
        assertEquals(1, repository.size());
        assertEquals(discount1, repository.get(discount1Id));
        
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
        repository.save(testStoreId2, discount3);
        
        // Verify global state
        assertEquals(3, repository.size());
        assertTrue(repository.exists(discount1Id));
        assertTrue(repository.exists(discount2Id));
        assertTrue(repository.exists(discount3Id));
        
        // Verify store-specific state
        assertEquals(2, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(1, repository.getStoreDiscountCount(testStoreId2));
        
        Set<Discount> store1Discounts = repository.getStoreDiscounts(testStoreId1);
        Set<Discount> store2Discounts = repository.getStoreDiscounts(testStoreId2);
        
        assertTrue(store1Discounts.contains(discount1));
        assertTrue(store1Discounts.contains(discount2));
        assertFalse(store1Discounts.contains(discount3));
        
        assertFalse(store2Discounts.contains(discount1));
        assertFalse(store2Discounts.contains(discount2));
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
        assertEquals(1, repository.size()); // discount2 still exists in store2
        assertEquals(0, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(1, repository.getStoreDiscountCount(testStoreId2));
        
        // Verify global state after partial deletion
        assertFalse(repository.exists(discount1Id)); // Only in store1
        assertFalse(repository.exists(discount2Id));  // Still in store2
        assertTrue(repository.exists(discount3Id));  // Still in store2
    }
    
    @Test
    public void testStoreIsolation() {
        // Save different discounts to different stores
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId2, discount2);
        
        // Verify isolation
        Set<Discount> store1Discounts = repository.getStoreDiscounts(testStoreId1);
        Set<Discount> store2Discounts = repository.getStoreDiscounts(testStoreId2);
        
        assertEquals(1, store1Discounts.size());
        assertEquals(1, store2Discounts.size());
        assertTrue(store1Discounts.contains(discount1));
        assertTrue(store2Discounts.contains(discount2));
        
        // Delete from one store should not affect the other
        repository.deleteByStoreId(testStoreId1);
        
        assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
        assertEquals(1, repository.getStoreDiscounts(testStoreId2).size());
        assertTrue(repository.exists(discount2Id));
        assertFalse(repository.exists(discount1Id));
    }
    
    @Test
    public void testSharedDiscountAcrossStores() {
        // Save same discount to multiple stores
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId2, discount1);
        
        // Should be one global entry but in both stores
        //assertEquals(1, repository.size());
        assertEquals(1, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(1, repository.getStoreDiscountCount(testStoreId2));
        
        // Delete by ID should remove from all stores
        repository.delete(discount1Id);
        
        //assertEquals(0, repository.size());
        assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
        assertTrue(repository.getStoreDiscounts(testStoreId2).isEmpty());
        assertFalse(repository.exists(discount1Id));
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
        repository.delete(discount2Id);
        
        assertEquals(2, repository.size());
        assertEquals(1, repository.getStoreDiscountCount(testStoreId1));
        assertEquals(1, repository.getStoreDiscountCount(testStoreId2));
        
        // Verify correct discounts remain
        assertTrue(repository.getStoreDiscounts(testStoreId1).contains(discount1));
        assertFalse(repository.getStoreDiscounts(testStoreId1).contains(discount2));
        assertFalse(repository.getStoreDiscounts(testStoreId1).contains(discount3));
        
        assertFalse(repository.getStoreDiscounts(testStoreId2).contains(discount1));
        assertFalse(repository.getStoreDiscounts(testStoreId2).contains(discount2));
        assertTrue(repository.getStoreDiscounts(testStoreId2).contains(discount3));
    }
    public void testSaveDiscount() {
        repository.save(testStoreId1, discount1);
        
        assertTrue(repository.exists(discount1Id));
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
        
        assertTrue(repository.exists(discount1Id));
        assertTrue(repository.exists(discount2Id));
        assertEquals(2, repository.size());
    }
    
    @Test
    public void testSaveSameDiscountToMultipleStores() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId2, discount1);
        
        assertTrue(repository.exists(discount1Id));
        assertEquals(1, repository.size()); // Same discount, so only one entry globally
        
        // Should be in both stores
        Set<Discount> store1Discounts = repository.getStoreDiscounts(testStoreId1);
        Set<Discount> store2Discounts = repository.getStoreDiscounts(testStoreId2);
        
        assertTrue(store1Discounts.contains(discount1));
        assertTrue(store2Discounts.contains(discount1));
    }
    
    // ===========================================
    // FIND TESTS
    // ===========================================
    
    @Test
    public void testget() {
        repository.save(testStoreId1, discount1);
        
        Discount found = repository.get(discount1Id);
        assertEquals(discount1, found);
    }
    
    @Test
    public void testgetNotFound() {
        Discount found = repository.get(UUID.randomUUID().toString());
        assertNull(found);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testgetWithNull() {
        repository.get(null);
    }
    
    // ===========================================
    // STORE-SPECIFIC FIND TESTS
    // ===========================================
    
    @Test
    public void testgetStoreDiscounts() {
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
    public void testgetStoreDiscountsEmpty() {
        Set<Discount> discounts = repository.getStoreDiscounts(testStoreId1);
        assertTrue(discounts.isEmpty());
    }
    
    @Test
    (expected = IllegalArgumentException.class)
    public void testgetStoreDiscountsWithNullStoreId() {
        repository.getStoreDiscounts(null);
    }
    
    @Test
    (expected = IllegalArgumentException.class)
    public void testgetStoreDiscountsWithEmptyStoreId() {
        repository.getStoreDiscounts("   ");
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
    (expected = IllegalArgumentException.class)
    public void testGetStoreDiscountsWithNullStoreId() {
        repository.getStoreDiscounts(null);
    }
    
    @Test
    (expected = IllegalArgumentException.class)
    public void testGetStoreDiscountsWithEmptyStoreId() {
        repository.getStoreDiscounts("   ");
    }
    
    @Test
    public void testgetStoreDiscountsReturnsDefensiveCopy() {
        repository.save(testStoreId1, discount1);
        
        Set<Discount> discounts = repository.getStoreDiscounts(testStoreId1);
        discounts.clear(); // Should not affect repository
        
        Set<Discount> discountsAgain = repository.getStoreDiscounts(testStoreId1);
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
    public void testdelete() {
        repository.save(testStoreId1, discount1);
        repository.save(testStoreId2, discount1);
        assertTrue(repository.exists(discount1Id));
        
        repository.delete(discount1Id);
        assertFalse(repository.exists(discount1Id));
        assertEquals(0, repository.size());
        
        // Should be removed from both stores
        assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
        assertTrue(repository.getStoreDiscounts(testStoreId2).isEmpty());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testdeleteWithNull() {
        repository.delete(null);
    }
    
    @Test
    public void testdeleteNotFound() {
        // Should not throw exception when deleting non-existent ID
        repository.delete(UUID.randomUUID().toString());
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
        assertFalse(repository.exists(discount1Id));
        assertFalse(repository.exists(discount2Id));
        assertTrue(repository.exists(discount3Id));
        
        assertTrue(repository.getStoreDiscounts(testStoreId1).isEmpty());
        assertFalse(repository.getStoreDiscounts(testStoreId2).isEmpty());
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