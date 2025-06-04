package Domain.Store.Discounts.Conditions;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

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
    }
    
    @Test
    public void testHasUniqueId() {
        assertNotNull(trueCondition.getId());
        
        TrueCondition anotherCondition = new TrueCondition();
        assertNotEquals(trueCondition.getId(), anotherCondition.getId());
    }
    
    @Test
    public void testConstructorWithExistingId() {
        String existingId = "existing-id-123";
        TrueCondition conditionWithId = new TrueCondition(existingId);
        
        assertEquals(existingId, conditionWithId.getId());
        assertTrue(conditionWithId.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testEquals() {
        TrueCondition condition1 = new TrueCondition();
        TrueCondition condition2 = new TrueCondition();
        TrueCondition sameId = new TrueCondition(condition1.getId());
        
        assertNotEquals("Different instances with different IDs should not be equal", condition1, condition2);
        assertEquals("Same instance should equal itself", condition1, condition1);
        assertEquals("Different instances with same ID should be equal", condition1, sameId);
    }
    
    @Test
    public void testHashCode() {
        assertEquals("Hash code should be based on ID", 
                    trueCondition.getId().hashCode(), trueCondition.hashCode());
    }
    
    @Test
    public void testToString() {
        String toString = trueCondition.toString();
        assertTrue("ToString should contain class name", toString.contains("TrueCondition"));
        assertTrue("ToString should contain 'id='", toString.contains("id="));
        assertTrue("ToString should contain the actual ID", toString.contains(trueCondition.getId()));
    }
    
    @Test
    public void testConsistentBehavior() {
        // Test that multiple calls return consistent results
        assertTrue("First call should return true", trueCondition.isSatisfied(basket, itemGetter));
        assertTrue("Second call should return true", trueCondition.isSatisfied(basket, itemGetter));
        assertTrue("Third call should return true", trueCondition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testWithDifferentBaskets() {
        // Test that result is independent of basket contents
        ShoppingBasket anotherBasket = mock(ShoppingBasket.class);
        
        assertTrue("Should return true for original basket", trueCondition.isSatisfied(basket, itemGetter));
        assertTrue("Should return true for different basket", trueCondition.isSatisfied(anotherBasket, itemGetter));
    }
}