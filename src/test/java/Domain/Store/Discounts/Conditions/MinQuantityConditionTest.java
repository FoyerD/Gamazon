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
    private BiFunction<String, String, Item> itemGetter;
    
    @Mock
    private ShoppingBasket basket;
    
    private MinQuantityCondition condition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        condition = new MinQuantityCondition("test-id", "product1", 5);
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
    
    @Test
    public void testGetters() {
        assertEquals("product1", condition.getProductId());
        assertEquals(5, condition.getMinQuantity());
        assertEquals("test-id", condition.getId());
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
    public void testHasCorrectId() {
        MinQuantityCondition another = new MinQuantityCondition("different-id", "product1", 5);
        assertNotEquals(condition.getId(), another.getId());
        assertEquals("test-id", condition.getId());
        assertEquals("different-id", another.getId());
    }
    
    @Test
    public void testEquals() {
        MinQuantityCondition same = new MinQuantityCondition("test-id", "product2", 10);
        MinQuantityCondition different = new MinQuantityCondition("different-id", "product1", 5);
        
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
        assertTrue("ToString should contain class name", toString.contains("MinQuantityCondition"));
        assertTrue("ToString should contain ID", toString.contains("test-id"));
    }
}