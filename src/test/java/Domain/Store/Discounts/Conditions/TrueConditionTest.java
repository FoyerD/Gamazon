package Domain.Store.Discounts.Conditions;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;

import java.util.function.BiFunction;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class TrueConditionTest {
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    private TrueCondition trueCondition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        trueCondition = new TrueCondition();
    }
    
    @Test
    public void testIsSatisfiedAlwaysReturnsTrue() {
        assertTrue(trueCondition.isSatisfied(basket, itemGetter));
        assertTrue(trueCondition.isSatisfied(null, itemGetter)); // Should handle null gracefully
        assertTrue(trueCondition.isSatisfied(basket, null)); // Should handle null itemGetter gracefully
        assertTrue(trueCondition.isSatisfied(null, null)); // Should handle both null
    }
    
    @Test
    public void testHasUniqueId() {
        assertNotNull(trueCondition.getId());
        
        TrueCondition anotherCondition = new TrueCondition();
        assertNotEquals(trueCondition.getId(), anotherCondition.getId());
    }
    
    @Test
    public void testConstructorWithExistingId() {
        String existingId = "existing-test-id";
        TrueCondition conditionWithId = new TrueCondition(existingId);
        
        assertEquals(existingId, conditionWithId.getId());
        assertTrue(conditionWithId.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testEquals() {
        TrueCondition condition1 = new TrueCondition();
        TrueCondition condition2 = new TrueCondition();
        
        assertNotEquals(condition1, condition2); // Different IDs
        assertEquals(condition1, condition1); // Same instance
        
        // Test with same ID
        String sameId = "same-id";
        TrueCondition conditionA = new TrueCondition(sameId);
        TrueCondition conditionB = new TrueCondition(sameId);
        assertEquals(conditionA, conditionB); // Same ID
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
        assertTrue(toString.contains(trueCondition.getId()));
    }
}