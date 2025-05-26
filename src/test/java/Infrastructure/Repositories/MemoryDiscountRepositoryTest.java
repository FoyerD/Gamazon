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

import Domain.Store.Discounts.Discount;

public class MemoryDiscountRepositoryTest {
    
    @Mock
    private Discount discount1;
    
    @Mock
    private Discount discount2;
    
    private MemoryDiscountRepository repository;
    private UUID discount1Id;
    private UUID discount2Id;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        repository = new MemoryDiscountRepository();
        
        discount1Id = UUID.randomUUID();
        discount2Id = UUID.randomUUID();
        
        when(discount1.getId()).thenReturn(discount1Id);
        when(discount2.getId()).thenReturn(discount2Id);
    }
    
    @After
    public void tearDown() {
        repository.clear();
    }
    
    @Test
    public void testSaveDiscount() {
        repository.save(discount1);
        
        assertTrue(repository.existsById(discount1Id));
        assertEquals(1, repository.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullDiscount() {
        repository.save(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSaveDiscountWithNullId() {
        Discount discountWithNullId = mock(Discount.class);
        when(discountWithNullId.getId()).thenReturn(null);
        
        repository.save(discountWithNullId);
    }
    
    @Test
    public void testFindById() {
        repository.save(discount1);
        
        Optional<Discount> found = repository.findById(discount1Id);
        assertTrue(found.isPresent());
        assertEquals(discount1, found.get());
    }
    
    @Test
    public void testFindByIdNotFound() {
        Optional<Discount> found = repository.findById(UUID.randomUUID());
        assertFalse(found.isPresent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFindByIdWithNull() {
        repository.findById(null);
    }
    
    @Test
    public void testDeleteById() {
        repository.save(discount1);
        assertTrue(repository.existsById(discount1Id));
        
        repository.deleteById(discount1Id);
        assertFalse(repository.existsById(discount1Id));
        assertEquals(0, repository.size());
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
    public void testFindAll() {
        repository.save(discount1);
        repository.save(discount2);
        
        Map<UUID, Discount> all = repository.findAll();
        assertEquals(2, all.size());
        assertTrue(all.containsKey(discount1Id));
        assertTrue(all.containsKey(discount2Id));
        assertEquals(discount1, all.get(discount1Id));
        assertEquals(discount2, all.get(discount2Id));
    }
    
    @Test
    public void testFindAllEmpty() {
        Map<UUID, Discount> all = repository.findAll();
        assertTrue(all.isEmpty());
    }
    
    @Test
    public void testFindAllReturnsDefensiveCopy() {
        repository.save(discount1);
        
        Map<UUID, Discount> all = repository.findAll();
        all.clear(); // Modifying returned map should not affect repository
        
        assertEquals(1, repository.size());
        assertTrue(repository.existsById(discount1Id));
    }
    
    @Test
    public void testExistsById() {
        assertFalse(repository.existsById(discount1Id));
        
        repository.save(discount1);
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
        repository.save(discount1);
        repository.save(discount2);
        assertEquals(2, repository.size());
        
        repository.clear();
        assertEquals(0, repository.size());
        assertFalse(repository.existsById(discount1Id));
        assertFalse(repository.existsById(discount2Id));
    }
    
    @Test
    public void testSize() {
        assertEquals(0, repository.size());
        
        repository.save(discount1);
        assertEquals(1, repository.size());
        
        repository.save(discount2);
        assertEquals(2, repository.size());
        
        repository.deleteById(discount1Id);
        assertEquals(1, repository.size());
    }
    
    @Test
    public void testUpdateExistingDiscount() {
        repository.save(discount1);
        assertEquals(1, repository.size());
        
        // Save again with same ID should update, not create new entry
        repository.save(discount1);
        assertEquals(1, repository.size());
        
        Optional<Discount> found = repository.findById(discount1Id);
        assertTrue(found.isPresent());
        assertEquals(discount1, found.get());
    }
}