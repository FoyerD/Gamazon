package Domain.Store.Discounts.Conditions;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;

public class TrueConditionTest {
    
    @Mock
    private ShoppingBasket basket;
    
    private TrueCondition trueCondition;
    
    @Before
    public void setUp() {
        trueCondition = new TrueCondition();
    }
    
    @Test
    public void testIsSatisfiedAlwaysReturnsTrue() {
        assertTrue(trueCondition.isSatisfied(basket));
        assertTrue(trueCondition.isSatisfied(null)); // Should handle null gracefully
    }
    
    @Test
    public void testHasUniqueId() {
        assertNotNull(trueCondition.getId());
        
        TrueCondition anotherCondition = new TrueCondition();
        assertNotEquals(trueCondition.getId(), anotherCondition.getId());
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        TrueCondition conditionWithId = new TrueCondition(existingId);
        
        assertEquals(existingId.toString(), conditionWithId.getId());
        assertTrue(conditionWithId.isSatisfied(basket));
    }
    
    @Test
    public void testEquals() {
        TrueCondition condition1 = new TrueCondition();
        TrueCondition condition2 = new TrueCondition();
        
        assertNotEquals(condition1, condition2); // Different UUIDs
        assertEquals(condition1, condition1); // Same instance
    }
    
    @Test
    public void testHashCode() {
        assertEquals(trueCondition.getId().hashCode(), trueCondition.hashCode());
    }
    
    @Test
    public void testToString() {
        String toString = trueCondition.toString();
        assertTrue(toString.contains("TrueCondition"));
        assertTrue(toString.contains("id="));
    }
}