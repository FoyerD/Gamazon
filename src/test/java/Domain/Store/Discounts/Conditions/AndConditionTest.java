package Domain.Store.Discounts.Conditions;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class AndConditionTest {
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    @Mock
    private Condition condition1;
    
    @Mock
    private Condition condition2;
    
    @Mock
    private Condition condition3;
    
    private AndCondition andCondition;
    private String conditionId;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        conditionId = "test-and-condition-id";
        List<Condition> conditions = Arrays.asList(condition1, condition2);
        andCondition = new AndCondition(conditionId, conditions);
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
        
        // Test the other way around
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        assertFalse(andCondition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testIsSatisfiedWhenAllConditionsFalse() {
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        assertFalse(andCondition.isSatisfied(basket, itemGetter));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullId() {
        List<Condition> conditions = Arrays.asList(condition1, condition2);
        new AndCondition(null, conditions);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyId() {
        List<Condition> conditions = Arrays.asList(condition1, condition2);
        new AndCondition("", conditions);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullConditions() {
        new AndCondition("test-id", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyConditions() {
        new AndCondition("test-id", Arrays.asList());
    }
    
    @Test
    public void testWithThreeConditions() {
        List<Condition> threeConditions = Arrays.asList(condition1, condition2, condition3);
        AndCondition andWithThree = new AndCondition("test-three-conditions", threeConditions);
        
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition3.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        assertTrue(andWithThree.isSatisfied(basket, itemGetter));
        
        // If any one is false, result should be false
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        assertFalse(andWithThree.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testGetters() {
        assertEquals(conditionId, andCondition.getId());
        
        List<Condition> retrievedConditions = andCondition.getConditions();
        assertEquals(2, retrievedConditions.size());
        assertTrue(retrievedConditions.contains(condition1));
        assertTrue(retrievedConditions.contains(condition2));
    }
    
    @Test
    public void testHasUniqueId() {
        assertNotNull(andCondition.getId());
        
        List<Condition> conditions = Arrays.asList(condition1, condition2);
        AndCondition anotherCondition = new AndCondition("different-id", conditions);
        assertNotEquals(andCondition.getId(), anotherCondition.getId());
    }

}