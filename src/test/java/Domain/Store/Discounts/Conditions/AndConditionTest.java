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

public class AndConditionTest {
    
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
    
    private AndCondition andCondition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        andCondition = new AndCondition("test-id", List.of(condition1, condition2));
    }
    
    @Test
    public void testIsSatisfiedWhenAllConditionsTrue() {
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        assertTrue(andCondition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testIsSatisfiedWhenOneConditionFalse() {
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        assertFalse(andCondition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testIsSatisfiedWhenAllConditionsFalse() {
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        assertFalse(andCondition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testConstructorWithTwoConditions() {
        AndCondition condition = new AndCondition("test-id-2", List.of(condition1, condition2));
        assertEquals("test-id-2", condition.getId());
    }
    
    @Test
    public void testWithThreeConditions() {
        AndCondition threeConditions = new AndCondition("test-id-3", List.of(condition1, condition2, condition3));
        
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition3.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        assertTrue(threeConditions.isSatisfied(basket, itemGetter));
        
        // If any one is false, result should be false
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        assertFalse(threeConditions.isSatisfied(basket, itemGetter));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullId() {
        new AndCondition(null, List.of(condition1, condition2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyId() {
        new AndCondition("", List.of(condition1, condition2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullConditions() {
        new AndCondition("test-id", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyConditions() {
        new AndCondition("test-id", List.of());
    }
    
    @Test
    public void testHasCorrectId() {
        assertEquals("test-id", andCondition.getId());
    }
    
    @Test
    public void testGetConditions() {
        List<Condition> conditions = andCondition.getConditions();
        assertEquals(2, conditions.size());
        assertTrue(conditions.contains(condition1));
        assertTrue(conditions.contains(condition2));
    }
}