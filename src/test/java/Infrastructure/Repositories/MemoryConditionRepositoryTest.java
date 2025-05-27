package Infrastructure.Repositories;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.Map;
import java.util.UUID;

import Domain.Store.Discounts.Conditions.Condition;

public class MemoryConditionRepositoryTest {
    
    @Mock
    private Condition condition1;
    
    @Mock
    private Condition condition2;
    
    @Mock
    private Condition condition3;
    
    private MemoryConditionRepository repository;
    private UUID condition1Id;
    private UUID condition2Id;
    private UUID condition3Id;
    private String testStoreId1 = "store1";
    private String testStoreId2 = "store2";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        repository = new MemoryConditionRepository();
        
        condition1Id = UUID.randomUUID();
        condition2Id = UUID.randomUUID();
        condition3Id = UUID.randomUUID();
        
        when(condition1.getId()).thenReturn(condition1Id);
        when(condition2.getId()).thenReturn(condition2Id);
        when(condition3.getId()).thenReturn(condition3Id);
    }
    
    @After
    public void tearDown() {
        repository.clear();
    }
    
    // ===========================================
    // BASIC SAVE TESTS
    // ===========================================
    
    @Test
    public void testSaveCondition() {
        repository.save(testStoreId1, condition1);
        
        assertTrue(repository.existsById(condition1Id));
        assertEquals(1, repository.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullCondition() {
        repository.save(testStoreId1, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveConditionWithNullId() {
        Condition conditionWithNullId = mock(Condition.class);
        when(conditionWithNullId.getId()).thenReturn(null);
        
        repository.save(testStoreId1, conditionWithNullId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveWithNullStoreId() {
        repository.save(null, condition1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveWithEmptyStoreId() {
        repository.save("   ", condition1);
    }
    
    @Test
    public void testSaveToMultipleStores() {
        repository.save(testStoreId1, condition1);
        repository.save(testStoreId2, condition2);
        
        assertTrue(repository.existsById(condition1Id));
        assertTrue(repository.existsById(condition2Id));
        assertEquals(2, repository.size());
    }
    
    @Test
    public void testSaveSameConditionToMultipleStores() {
        repository.save(testStoreId1, condition1);
        repository.save(testStoreId2, condition1);
        
        assertTrue(repository.existsById(condition1Id));
        assertEquals(1, repository.size()); // Same condition, so only one entry globally
        
        // Should be in both stores
        Map<UUID, Condition> store1Conditions = repository.findByStoreId(testStoreId1);
        Map<UUID, Condition> store2Conditions = repository.findByStoreId(testStoreId2);
        
        assertTrue(store1Conditions.containsKey(condition1Id));
        assertTrue(store2Conditions.containsKey(condition1Id));
    }
    
    // ===========================================
    // FIND TESTS
    // ===========================================
    
    @Test
    public void testFindById() {
        repository.save(testStoreId1, condition1);
        
        Condition found = repository.findById(condition1Id);
        assertEquals(condition1, found);
    }
    
    @Test
    public void testFindByIdNotFound() {
        Condition found = repository.findById(UUID.randomUUID());
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
        repository.save(testStoreId1, condition1);
        repository.save(testStoreId1, condition2);
        repository.save(testStoreId2, condition3);
        
        Map<UUID, Condition> store1Conditions = repository.findByStoreId(testStoreId1);
        Map<UUID, Condition> store2Conditions = repository.findByStoreId(testStoreId2);
        
        assertEquals(2, store1Conditions.size());
        assertEquals(1, store2Conditions.size());
        
        assertTrue(store1Conditions.containsKey(condition1Id));
        assertTrue(store1Conditions.containsKey(condition2Id));
        assertFalse(store1Conditions.containsKey(condition3Id));
        
        assertFalse(store2Conditions.containsKey(condition1Id));
        assertFalse(store2Conditions.containsKey(condition2Id));
        assertTrue(store2Conditions.containsKey(condition3Id));
    }
    
    @Test
    public void testFindByStoreIdEmpty() {
        Map<UUID, Condition> conditions = repository.findByStoreId(testStoreId1);
        assertTrue(conditions.isEmpty());
    }
    
    @Test
    public void testFindByStoreIdWithNullStoreId() {
        Map<UUID, Condition> conditions = repository.findByStoreId(null);
        assertTrue(conditions.isEmpty());
    }
    
    @Test
    public void testFindByStoreIdWithEmptyStoreId() {
        Map<UUID, Condition> conditions = repository.findByStoreId("   ");
        assertTrue(conditions.isEmpty());
    }
    
    @Test
    public void testFindByStoreIdReturnsDefensiveCopy() {
        repository.save(testStoreId1, condition1);
        
        Map<UUID, Condition> conditions = repository.findByStoreId(testStoreId1);
        conditions.clear(); // Should not affect repository
        
        Map<UUID, Condition> conditionsAgain = repository.findByStoreId(testStoreId1);
        assertEquals(1, conditionsAgain.size());
        assertTrue(conditionsAgain.containsKey(condition1Id));
    }
    
    // ===========================================
    // DELETE TESTS
    // ===========================================
    
    @Test
    public void testDeleteById() {
        repository.save(testStoreId1, condition1);
        repository.save(testStoreId2, condition1);
        assertTrue(repository.existsById(condition1Id));
        
        repository.deleteById(condition1Id);
        assertFalse(repository.existsById(condition1Id));
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
        repository.save(testStoreId1, condition1);
        repository.save(testStoreId1, condition2);
        repository.save(testStoreId2, condition3);
        
        assertEquals(3, repository.size());
        
        int deleted = repository.deleteByStoreId(testStoreId1);
        
        assertEquals(2, deleted);
        assertEquals(1, repository.size());
        assertFalse(repository.existsById(condition1Id));
        assertFalse(repository.existsById(condition2Id));
        assertTrue(repository.existsById(condition3Id));
        
        assertTrue(repository.findByStoreId(testStoreId1).isEmpty());
        assertFalse(repository.findByStoreId(testStoreId2).isEmpty());
    }
    
    @Test
    public void testDeleteByStoreIdWithNullStoreId() {
        repository.save(testStoreId1, condition1);
        
        int deleted = repository.deleteByStoreId(null);
        
        assertEquals(0, deleted);
        assertEquals(1, repository.size());
    }
    
    @Test
    public void testDeleteByStoreIdWithEmptyStoreId() {
        repository.save(testStoreId1, condition1);
        
        int deleted = repository.deleteByStoreId("   ");
        
        assertEquals(0, deleted);
        assertEquals(1, repository.size());
    }
    
    @Test
    public void testDeleteByStoreIdNotFound() {
        repository.save(testStoreId1, condition1);
        
        int deleted = repository.deleteByStoreId("nonExistentStore");
        
        assertEquals(0, deleted);
        assertEquals(1, repository.size());
    }
    
    // ===========================================
    // GLOBAL OPERATIONS TESTS
    // ===========================================
    
    @Test
    public void testFindAll() {
        repository.save(testStoreId1, condition1);
        repository.save(testStoreId1, condition2);
        repository.save(testStoreId2, condition3);
        
        Map<UUID, Condition> all = repository.findAll();
        assertEquals(3, all.size());
        assertTrue(all.containsKey(condition1Id));
        assertTrue(all.containsKey(condition2Id));
        assertTrue(all.containsKey(condition3Id));
        assertEquals(condition1, all.get(condition1Id));
        assertEquals(condition2, all.get(condition2Id));
        assertEquals(condition3, all.get(condition3Id));
    }
    
    @Test
    public void testFindAllEmpty() {
        Map<UUID, Condition> all = repository.findAll();
        assertTrue(all.isEmpty());
    }
    
    @Test
    public void testFindAllReturnsDefensiveCopy() {
        repository.save(testStoreId1, condition1);
        
        Map<UUID, Condition> all = repository.findAll();
        all.clear(); // Modifying returned map should not affect repository
        
        assertEquals(1, repository.size());
        assertTrue(repository.existsById(condition1Id));
    }
    
    @Test
    public void testExistsById() {
        assertFalse(repository.existsById(condition1Id));
        
        repository.save(testStoreId1, condition1);
        assertTrue(repository.existsById(condition1Id));
        
        repository.deleteById(condition1Id);
        assertFalse(repository.existsById(condition1Id));
    }
    
    @Test
    public void testExistsByIdWithNull() {
        assertFalse(repository.existsById(null));
    }
    
    @Test
    public void testClear() {
        repository.save(testStoreId1, condition1);
        repository.save(testStoreId1, condition2);
        repository.save(testStoreId2, condition3);
        assertEquals(3, repository.size());
        
        repository.clear();
        assertEquals(0, repository.size());
        assertFalse(repository.existsById(condition1Id));
        assertFalse(repository.existsById(condition2Id));
        assertFalse(repository.existsById(condition3Id));
        
        assertTrue(repository.findByStoreId(testStoreId1).isEmpty());
        assertTrue(repository.findByStoreId(testStoreId2).isEmpty());
    }
    
    @Test
    public void testSize() {
        assertEquals(0, repository.size());
        
        repository.save(testStoreId1, condition1);
        assertEquals(1, repository.size());
        
        repository.save(testStoreId1, condition2);
        assertEquals(2, repository.size());
        
        repository.save(testStoreId2, condition3);
        assertEquals(3, repository.size());
        
        repository.deleteById(condition1Id);
        assertEquals(2, repository.size());
    }
    
    // ===========================================
    // STORE UTILITY METHODS TESTS
    // ===========================================
    
    @Test
    public void testGetStoreConditionCount() {
        repository.save(testStoreId1, condition1);
        repository.save(testStoreId1, condition2);
        repository.save(testStoreId2, condition3);
        
        assertEquals(2, repository.getStoreConditionCount(testStoreId1));
        assertEquals(1, repository.getStoreConditionCount(testStoreId2));
        assertEquals(0, repository.getStoreConditionCount("nonExistentStore"));
    }
    
    @Test
    public void testGetStoreConditionCountWithNullStoreId() {
        repository.save(testStoreId1, condition1);
        
        assertEquals(0, repository.getStoreConditionCount(null));
    }
    
    @Test
    public void testGetStoreConditionCountWithEmptyStoreId() {
        repository.save(testStoreId1, condition1);
        
        assertEquals(0, repository.getStoreConditionCount("   "));
    }
    
    @Test
    public void testHasConditionsForStore() {
        assertFalse(repository.hasConditionsForStore(testStoreId1));
        
        repository.save(testStoreId1, condition1);
        assertTrue(repository.hasConditionsForStore(testStoreId1));
        assertFalse(repository.hasConditionsForStore(testStoreId2));
        
        repository.deleteByStoreId(testStoreId1);
        assertFalse(repository.hasConditionsForStore(testStoreId1));
    }
    
    @Test
    public void testHasConditionsForStoreWithNullStoreId() {
        repository.save(testStoreId1, condition1);
        
        assertFalse(repository.hasConditionsForStore(null));
    }
    
    @Test
    public void testHasConditionsForStoreWithEmptyStoreId() {
        repository.save(testStoreId1, condition1);
        
        assertFalse(repository.hasConditionsForStore("   "));
    }
    
    @Test
    public void testGetStoreConditionCounts() {
        repository.save(testStoreId1, condition1);
        repository.save(testStoreId1, condition2);
        repository.save(testStoreId2, condition3);
        
        Map<String, Integer> counts = repository.getStoreConditionCounts();
        
        assertEquals(2, counts.size());
        assertEquals(Integer.valueOf(2), counts.get(testStoreId1));
        assertEquals(Integer.valueOf(1), counts.get(testStoreId2));
    }
    
    @Test
    public void testGetStoreConditionCountsEmpty() {
        Map<String, Integer> counts = repository.getStoreConditionCounts();
        assertTrue(counts.isEmpty());
    }
    
    // ===========================================
    // UPDATE TESTS
    // ===========================================
    
    @Test
    public void testUpdateExistingCondition() {
        repository.save(testStoreId1, condition1);
        assertEquals(1, repository.size());
        assertEquals(1, repository.getStoreConditionCount(testStoreId1));
        
        // Save again with same ID should update, not create new entry
        repository.save(testStoreId1, condition1);
        assertEquals(1, repository.size());
        assertEquals(1, repository.getStoreConditionCount(testStoreId1));
        
        Condition found = repository.findById(condition1Id);
        assertEquals(condition1, found);
    }
    
    @Test
    public void testUpdateConditionInDifferentStore() {
        repository.save(testStoreId1, condition1);
        assertEquals(1, repository.getStoreConditionCount(testStoreId1));
        assertEquals(0, repository.getStoreConditionCount(testStoreId2));
        
        // Save same condition to different store
        repository.save(testStoreId2, condition1);
        assertEquals(1, repository.size()); // Still only one global entry
        assertEquals(1, repository.getStoreConditionCount(testStoreId1));
        assertEquals(1, repository.getStoreConditionCount(testStoreId2));
    }
    
    // ===========================================
    // INTEGRATION TESTS
    // ===========================================
    
    @Test
    public void testCompleteWorkflow() {
        // Add conditions to multiple stores
        repository.save(testStoreId1, condition1);
        repository.save(testStoreId1, condition2);
        repository.save(testStoreId2, condition2);
        repository.save(testStoreId2, condition3);
        
        // Verify global state
        assertEquals(3, repository.size());
        assertTrue(repository.existsById(condition1Id));
        assertTrue(repository.existsById(condition2Id));
        assertTrue(repository.existsById(condition3Id));
        
        // Verify store-specific state
        assertEquals(2, repository.getStoreConditionCount(testStoreId1));
        assertEquals(2, repository.getStoreConditionCount(testStoreId2));
        
        Map<UUID, Condition> store1Conditions = repository.findByStoreId(testStoreId1);
        Map<UUID, Condition> store2Conditions = repository.findByStoreId(testStoreId2);
        
        assertTrue(store1Conditions.containsKey(condition1Id));
        assertTrue(store1Conditions.containsKey(condition2Id));
        assertFalse(store1Conditions.containsKey(condition3Id));
        
        assertFalse(store2Conditions.containsKey(condition1Id));
        assertTrue(store2Conditions.containsKey(condition2Id));
        assertTrue(store2Conditions.containsKey(condition3Id));
        
        // Delete from one store
        repository.deleteByStoreId(testStoreId1);
        assertEquals(2, repository.size()); // condition2 still exists in store2
        assertEquals(0, repository.getStoreConditionCount(testStoreId1));
        assertEquals(2, repository.getStoreConditionCount(testStoreId2));
        
        // Verify global state after partial deletion
        assertFalse(repository.existsById(condition1Id)); // Only in store1
        assertTrue(repository.existsById(condition2Id));  // Still in store2
        assertTrue(repository.existsById(condition3Id));  // Still in store2
    }
}