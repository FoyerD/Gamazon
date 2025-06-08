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

public class MinQuantityConditionTest {
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    private MinQuantityCondition condition;
    private String conditionId;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        conditionId = "test-condition-id";
        condition = new MinQuantityCondition(conditionId, "product1", 5);
    }
    
    @Test
    public void testIsSatisfiedWhenQuantityMeetsMinimum() {
        when(basket.getQuantity("product1")).thenReturn(5);
        assertTrue(condition.isSatisfied(basket, itemGetter));
        
        when(basket.getQuantity("product1")).thenReturn(10);
        assertTrue(condition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testIsSatisfiedWhenQuantityBelowMinimum() {
        when(basket.getQuantity("product1")).thenReturn(3);
        assertFalse(condition.isSatisfied(basket, itemGetter));
        
        when(basket.getQuantity("product1")).thenReturn(0);
        assertFalse(condition.isSatisfied(basket, itemGetter));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullProductId() {
        new MinQuantityCondition("test-id", null, 5);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyProductId() {
        new MinQuantityCondition("test-id", "", 5);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeQuantity() {
        new MinQuantityCondition("test-id", "product1", -1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroQuantity() {
        new MinQuantityCondition("test-id", "product1", 0);
    }
    
    @Test
    public void testGetters() {
        assertEquals("product1", condition.getProductId());
        assertEquals(5, condition.getMinQuantity());
        assertEquals(conditionId, condition.getId());
    }
    
    @Test
    public void testHasUniqueId() {
        MinQuantityCondition another = new MinQuantityCondition("different-id", "product1", 5);
        assertNotEquals(condition.getId(), another.getId());
    }
}