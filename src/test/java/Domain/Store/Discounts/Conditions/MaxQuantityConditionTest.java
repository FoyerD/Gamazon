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
    private BiFunction<String, String, Item> itemGetter;
    
    @Mock
    private ShoppingBasket basket;
    
    private MaxQuantityCondition condition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        condition = new MaxQuantityCondition("test-id", "product1", 10);
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
    
    @Test
    public void testGetters() {
        assertEquals("product1", condition.getProductId());
        assertEquals(10, condition.getMaxQuantity());
        assertEquals("test-id", condition.getId());
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
        new MaxQuantityCondition("test-id", "product1", -5);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroQuantity() {
        new MaxQuantityCondition("test-id", "product1", 0);
    }
    
    @Test
    public void testDifferentIds() {
        MaxQuantityCondition another = new MaxQuantityCondition("different-id", "product1", 10);
        assertNotEquals(condition.getId(), another.getId());
    }
    
    @Test
    public void testEquals() {
        MaxQuantityCondition same = new MaxQuantityCondition("test-id", "product2", 5);
        MaxQuantityCondition different = new MaxQuantityCondition("different-id", "product1", 10);
        
        assertEquals("Conditions with same ID should be equal", condition, same);
        assertNotEquals("Conditions with different ID should not be equal", condition, different);
    }
    
    @Test
    public void testHashCode() {
        assertEquals("Hash code should be based on ID", 
                    condition.getId().hashCode(), condition.hashCode());
    }
    
    @Test
    public void testToString() {
        String toString = condition.toString();
        assertTrue("ToString should contain class name", toString.contains("MaxQuantityCondition"));
        assertTrue("ToString should contain ID", toString.contains("test-id"));
    }
}