package Domain.Store.Discounts.Conditions;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.function.BiFunction;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class MaxQuantityConditionTest {
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    private MaxQuantityCondition condition;
    private String conditionId;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        conditionId = "test-condition-id";
        condition = new MaxQuantityCondition(conditionId, "product1", 10);
    }
    
    @Test
    public void testIsSatisfiedWhenQuantityBelowMaximum() {
        when(basket.getQuantity("product1")).thenReturn(5);
        assertTrue(condition.isSatisfied(basket, itemGetter));
        
        when(basket.getQuantity("product1")).thenReturn(0);
        assertTrue(condition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testIsSatisfiedWhenQuantityEqualsMaximum() {
        when(basket.getQuantity("product1")).thenReturn(10);
        assertTrue(condition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testIsSatisfiedWhenQuantityAboveMaximum() {
        when(basket.getQuantity("product1")).thenReturn(15);
        assertFalse(condition.isSatisfied(basket, itemGetter));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullProductId() {
        new MaxQuantityCondition("test-id", null, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyProductId() {
        new MaxQuantityCondition("test-id", "", 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeQuantity() {
        new MaxQuantityCondition("test-id", "product1", -1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroQuantity() {
        new MaxQuantityCondition("test-id", "product1", 0);
    }
    
    @Test
    public void testGetters() {
        assertEquals("product1", condition.getProductId());
        assertEquals(10, condition.getMaxQuantity());
        assertEquals(conditionId, condition.getId());
    }
    
    @Test
    public void testHasUniqueId() {
        MaxQuantityCondition another = new MaxQuantityCondition("different-id", "product1", 10);
        assertNotEquals(condition.getId(), another.getId());
    }
}