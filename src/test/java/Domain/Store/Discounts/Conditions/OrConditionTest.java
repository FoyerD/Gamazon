package Domain.Store.Discounts.Conditions;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class OrConditionTest {
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Condition condition1;
    
    @Mock
    private Condition condition2;
    
    @Mock
    private Condition condition3;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    private OrCondition orCondition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        orCondition = new OrCondition("test-id", List.of(condition1, condition2));
    }
    
    @Test
    public void testIsSatisfiedWhenAllConditionsTrue() {
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        assertTrue(orCondition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testIsSatisfiedWhenOneConditionTrue() {
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        assertTrue(orCondition.isSatisfied(basket, itemGetter));
        
        // Test the other way around
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        assertTrue(orCondition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testIsSatisfiedWhenAllConditionsFalse() {
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        assertFalse(orCondition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testWithThreeConditions() {
        OrCondition threeConditions = new OrCondition("test-id-3", List.of(condition1, condition2, condition3));
        
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(condition3.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        assertTrue(threeConditions.isSatisfied(basket, itemGetter));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullId() {
        new OrCondition(null, List.of(condition1, condition2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyId() {
        new OrCondition("", List.of(condition1, condition2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullConditions() {
        new OrCondition("test-id", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyConditions() {
        new OrCondition("test-id", List.of());
    }
    
    @Test
    public void testHasCorrectId() {
        assertEquals("test-id", orCondition.getId());
        
        OrCondition anotherCondition = new OrCondition("different-id", List.of(condition1, condition2));
        assertNotEquals(orCondition.getId(), anotherCondition.getId());
    }
    
    @Test
    public void testGetConditions() {
        List<Condition> conditions = orCondition.getConditions();
        assertEquals(2, conditions.size());
        assertTrue(conditions.contains(condition1));
        assertTrue(conditions.contains(condition2));
    }
    
    @Test
    public void testEquals() {
        OrCondition same = new OrCondition("test-id", List.of(condition3));
        OrCondition different = new OrCondition("different-id", List.of(condition1, condition2));
        
        assertEquals("Conditions with same ID should be equal", orCondition, same);
        assertNotEquals("Conditions with different ID should not be equal", orCondition, different);
    }
    
    @Test
    public void testHashCode() {
        assertEquals("Hash code should be based on ID", 
                    orCondition.getId().hashCode(), orCondition.hashCode());
    }
    
    @Test
    public void testToString() {
        String toString = orCondition.toString();
        assertTrue("ToString should contain class name", toString.contains("OrCondition"));
        assertTrue("ToString should contain ID", toString.contains("test-id"));
        assertTrue("ToString should contain condition count", toString.contains("2"));
    }

}