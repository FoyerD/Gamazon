package Infrastructure.Repositories;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import Domain.Store.Discounts.Conditions.Condition;

public class MemoryConditionRepositoryTest {
    
    @Mock
    private Condition condition1;
    
    @Mock
    private Condition condition2;
    
    private MemoryConditionRepository repository;
    private UUID condition1Id;
    private UUID condition2Id;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        repository = new MemoryConditionRepository();
        
        condition1Id = UUID.randomUUID();
        condition2Id = UUID.randomUUID();
        
        when(condition1.getId()).thenReturn(condition1Id);
        when(condition2.getId()).thenReturn(condition2Id);
    }
    
    @After
    public void tearDown() {
        repository.clear();
    }
    
    @Test
    public void testSaveCondition() {
        repository.save(condition1);
        
        assertTrue(repository.existsById(condition1Id));
        assertEquals(1, repository.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullCondition() {
        repository.save(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveConditionWithNullId() {
        Condition conditionWithNullId = mock(Condition.class);
        when(conditionWithNullId.getId()).thenReturn(null);
        
        repository.save(conditionWithNullId);
    }
    
    @Test
    public void testFindById() {
        repository.save(condition1);
        
        Optional<Condition> found = repository.findById(condition1Id);
        assertTrue(found.isPresent());
        assertEquals(condition1, found.get());
    }
    
    @Test
    public void testFindByIdNotFound() {
        Optional<Condition> found = repository.findById(UUID.randomUUID());
        assertFalse(found.isPresent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFindByIdWithNull() {
        repository.findById(null);
    }
    
    @Test
    public void testDeleteById() {
        repository.save(condition1);
        assertTrue(repository.existsById(condition1Id));
        
        repository.deleteById(condition1Id);
        assertFalse(repository.existsById(condition1Id));
        assertEquals(0, repository.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteByIdWithNull() {
        repository.deleteById(null);
    }
    
    @Test
    public void testFindAll() {
        repository.save(condition1);
        repository.save(condition2);
        
        Map<UUID, Condition> all = repository.findAll();
        assertEquals(2, all.size());
        assertTrue(all.containsKey(condition1Id));
        assertTrue(all.containsKey(condition2Id));
        assertEquals(condition1, all.get(condition1Id));
        assertEquals(condition2, all.get(condition2Id));
    }
    
    @Test
    public void testExistsById() {
        assertFalse(repository.existsById(condition1Id));
        
        repository.save(condition1);
        assertTrue(repository.existsById(condition1Id));
        
        repository.deleteById(condition1Id);
        assertFalse(repository.existsById(condition1Id));
    }
    
    @Test
    public void testClear() {
        repository.save(condition1);
        repository.save(condition2);
        assertEquals(2, repository.size());
        
        repository.clear();
        assertEquals(0, repository.size());
        assertFalse(repository.existsById(condition1Id));
        assertFalse(repository.existsById(condition2Id));
    }
    
    @Test
    public void testSize() {
        assertEquals(0, repository.size());
        
        repository.save(condition1);
        assertEquals(1, repository.size());
        
        repository.save(condition2);
        assertEquals(2, repository.size());
        
        repository.deleteById(condition1Id);
        assertEquals(1, repository.size());
    }
}