package Domain.Store.Discounts.Conditions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.function.BiFunction;
import java.util.HashMap;
import java.util.Map;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class MaxPriceConditionTest {

    private MaxPriceCondition maxPriceCondition;
    
    @Mock
    private BiFunction<String, String, Item> mockItemGetter;
    
    @Mock
    private ShoppingBasket mockBasket;
    
    @Mock
    private Item mockItem1;
    
    @Mock
    private Item mockItem2;
    
    private String storeId;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Create condition with max price of 100.0
        maxPriceCondition = new MaxPriceCondition("test-id", 100.0);
        
        storeId = "store123";
        
        // Mock items with specific prices
        when(mockItem1.getPrice()).thenReturn(30.0);
        when(mockItem2.getPrice()).thenReturn(20.0);
        
        when(mockItemGetter.apply(storeId, "item1")).thenReturn(mockItem1);
        when(mockItemGetter.apply(storeId, "item2")).thenReturn(mockItem2);
        
        when(mockBasket.getStoreId()).thenReturn(storeId);
    }

    @Test
    public void testIsSatisfiedWhenPriceBelowMaximum() {
        // Setup basket with total price of 80.0 (below maximum of 100.0)
        Map<String, Integer> orders = new HashMap<>();
        orders.put("item1", 2); // 2 * 30.0 = 60.0
        orders.put("item2", 1); // 1 * 20.0 = 20.0
        // Total: 80.0
        
        when(mockBasket.getOrders()).thenReturn(orders);
        when(mockBasket.getQuantity("item1")).thenReturn(2);
        when(mockBasket.getQuantity("item2")).thenReturn(1);
        
        assertTrue("Should be satisfied when price is below maximum", 
                  maxPriceCondition.isSatisfied(mockBasket, mockItemGetter));
    }

    @Test
    public void testIsSatisfiedWhenPriceEqualsMaximum() {
        // Setup basket with total price of exactly 100.0
        Map<String, Integer> orders = new HashMap<>();
        orders.put("item1", 2); // 2 * 30.0 = 60.0
        orders.put("item2", 2); // 2 * 20.0 = 40.0
        // Total: 100.0
        
        when(mockBasket.getOrders()).thenReturn(orders);
        when(mockBasket.getQuantity("item1")).thenReturn(2);
        when(mockBasket.getQuantity("item2")).thenReturn(2);
        
        assertTrue("Should be satisfied when price equals maximum", 
                  maxPriceCondition.isSatisfied(mockBasket, mockItemGetter));
    }

    @Test
    public void testIsSatisfiedWhenPriceAboveMaximum() {
        // Setup basket with total price of 150.0 (above maximum of 100.0)
        Map<String, Integer> orders = new HashMap<>();
        orders.put("item1", 3); // 3 * 30.0 = 90.0
        orders.put("item2", 3); // 3 * 20.0 = 60.0
        // Total: 150.0
        
        when(mockBasket.getOrders()).thenReturn(orders);
        when(mockBasket.getQuantity("item1")).thenReturn(3);
        when(mockBasket.getQuantity("item2")).thenReturn(3);
        
        assertFalse("Should not be satisfied when price is above maximum", 
                   maxPriceCondition.isSatisfied(mockBasket, mockItemGetter));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSatisfiedWhenShoppingBasketIsNull() {
        maxPriceCondition.isSatisfied(null, mockItemGetter);
    }

    @Test(expected = IllegalArgumentException.class) 
    public void testIsSatisfiedWhenStoreIdIsNull() {
        when(mockBasket.getStoreId()).thenReturn(null);
        maxPriceCondition.isSatisfied(mockBasket, mockItemGetter);
    }

    @Test
    public void testZeroQuantity() {
        // Empty basket (no items)
        Map<String, Integer> emptyOrders = new HashMap<>();
        when(mockBasket.getOrders()).thenReturn(emptyOrders);
        
        assertTrue("Should be satisfied when basket is empty (total price = 0)", 
                  maxPriceCondition.isSatisfied(mockBasket, mockItemGetter));
    }

    @Test
    public void testGetters() {
        assertEquals("Max price getter should return correct value", 
                    100.0, maxPriceCondition.getMaxPrice(), 0.001);
        assertEquals("ID should be correct", "test-id", maxPriceCondition.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativePrice() {
        new MaxPriceCondition("test-id", -10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroPrice() {
        new MaxPriceCondition("test-id", 0.0);
    }

    @Test
    public void testHasCorrectId() {
        MaxPriceCondition condition1 = new MaxPriceCondition("id-1", 100.0);
        MaxPriceCondition condition2 = new MaxPriceCondition("id-2", 100.0);
        
        assertEquals("id-1", condition1.getId());
        assertEquals("id-2", condition2.getId());
        assertNotEquals("Each condition should have different ID", 
                       condition1.getId(), condition2.getId());
    }
}