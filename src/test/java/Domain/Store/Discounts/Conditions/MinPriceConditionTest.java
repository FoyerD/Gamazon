package Domain.Store.Discounts.Conditions;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.function.BiFunction;
import java.util.HashMap;
import java.util.Map;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class MinPriceConditionTest {
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Item item;
    
    private MinPriceCondition condition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        condition = new MinPriceCondition("test-id", 100.0);
        
        // Mock the basket's storeId
        when(basket.getStoreId()).thenReturn("store1");
    }
    
    @Test
    public void testIsSatisfiedWhenPriceMeetsMinimum() {
        // Create a map with one product
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(itemGetter.apply("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(50.0);
        when(basket.getQuantity("product1")).thenReturn(2);
        
        // Total price: 50 * 2 = 100, which equals minimum
        assertTrue(condition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testIsSatisfiedWhenPriceAboveMinimum() {
        // Create a map with one product
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(itemGetter.apply("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(60.0);
        when(basket.getQuantity("product1")).thenReturn(2);
        
        // Total price: 60 * 2 = 120, which is above minimum of 100
        assertTrue(condition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testIsSatisfiedWhenPriceBelowMinimum() {
        // Create a map with one product
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(itemGetter.apply("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(30.0);
        when(basket.getQuantity("product1")).thenReturn(2);
        
        // Total price: 30 * 2 = 60, which is below minimum of 100
        assertFalse(condition.isSatisfied(basket, itemGetter));
    }
    
    @Test
    public void testGetters() {
        assertEquals(100.0, condition.getMinPrice(), 0.001);
        assertEquals("test-id", condition.getId());
    }
    
    @Test
    public void testZeroQuantity() {
        // Create an empty map (no products)
        Map<String, Integer> orders = new HashMap<>();
        
        when(basket.getOrders()).thenReturn(orders);
        
        // Total price: 0, which is below minimum
        assertFalse(condition.isSatisfied(basket, itemGetter));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativePrice() {
        new MinPriceCondition("test-id", -10.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroPrice() {
        new MinPriceCondition("test-id", 0.0);
    }
    
    @Test
    public void testHasCorrectId() {
        MinPriceCondition another = new MinPriceCondition("different-id", 100.0);
        assertNotEquals(condition.getId(), another.getId());
        assertEquals("test-id", condition.getId());
        assertEquals("different-id", another.getId());
    }
    
    @Test
    public void testEquals() {
        MinPriceCondition same = new MinPriceCondition("test-id", 200.0);
        MinPriceCondition different = new MinPriceCondition("different-id", 100.0);
        
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
        assertTrue("ToString should contain class name", toString.contains("MinPriceCondition"));
        assertTrue("ToString should contain ID", toString.contains("test-id"));
    }
}