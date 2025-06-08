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

public class OrConditionTest {
    
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
    
    private OrCondition orCondition;
    private String conditionId;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        conditionId = "test-or-condition-id";
        List<Condition> conditions = Arrays.asList(condition1, condition2);
        orCondition = new OrCondition(conditionId, conditions);
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
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullId() {
        List<Condition> conditions = Arrays.asList(condition1, condition2);
        new OrCondition(null, conditions);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyId() {
        List<Condition> conditions = Arrays.asList(condition1, condition2);
        new OrCondition("", conditions);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullConditions() {
        new OrCondition("test-id", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyConditions() {
        new OrCondition("test-id", Arrays.asList());
    }
    
    @Test
    public void testWithThreeConditions() {
        List<Condition> threeConditions = Arrays.asList(condition1, condition2, condition3);
        OrCondition orWithThree = new OrCondition("test-three-conditions", threeConditions);
        
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(condition3.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        assertTrue(orWithThree.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testGetters() {
        assertEquals(conditionId, orCondition.getId());
        
        List<Condition> retrievedConditions = orCondition.getConditions();
        assertEquals(2, retrievedConditions.size());
        assertTrue(retrievedConditions.contains(condition1));
        assertTrue(retrievedConditions.contains(condition2));
    }
    
    @Test
    public void testHasUniqueId() {
        assertNotNull(orCondition.getId());
        
        List<Condition> conditions = Arrays.asList(condition1, condition2);
        OrCondition anotherCondition = new OrCondition("different-id", conditions);
        assertNotEquals(orCondition.getId(), anotherCondition.getId());
    }
}