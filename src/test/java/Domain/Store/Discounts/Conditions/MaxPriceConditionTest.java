package Domain.Store.Discounts.Conditions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class MaxPriceConditionTest {

    private MaxPriceCondition maxPriceCondition;
    
    @Mock
    private ShoppingBasket mockShoppingBasket;
    
    @Mock
    private BiFunction<String, String, Item> mockItemGetter;
    
    @Mock
    private Item mockItem1;
    
    @Mock
    private Item mockItem2;
    
    @Mock
    private Item mockItem3;
    
    @Mock
    private Item mockItem4;
    
    private String storeId;
    private String conditionId;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Create condition with max price of 100.0
        conditionId = "test-condition-id";
        maxPriceCondition = new MaxPriceCondition(conditionId, 100.0);
        
        // Setup store ID
        storeId = "store123";
        
        // Mock items with specific prices
        when(mockItem1.getPrice()).thenReturn(30.0);
        when(mockItem2.getPrice()).thenReturn(20.0);
        when(mockItem3.getPrice()).thenReturn(20.0);
        when(mockItem4.getPrice()).thenReturn(50.0);
        
        when(mockItemGetter.apply(storeId, "item1")).thenReturn(mockItem1);
        when(mockItemGetter.apply(storeId, "item2")).thenReturn(mockItem2);
        when(mockItemGetter.apply(storeId, "item3")).thenReturn(mockItem3);
        when(mockItemGetter.apply(storeId, "item4")).thenReturn(mockItem4);
        
        // Setup shopping basket
        when(mockShoppingBasket.getStoreId()).thenReturn(storeId);
        
        // Setup orders map - basket with 2 items at 30.0 each = 60.0, 1 item at 20.0 = 20.0, total = 80.0
        Map<String, Integer> orders = new HashMap<>();
        orders.put("item1", 2);
        orders.put("item2", 1);
        when(mockShoppingBasket.getOrders()).thenReturn(orders);
        when(mockShoppingBasket.getQuantity("item1")).thenReturn(2);
        when(mockShoppingBasket.getQuantity("item2")).thenReturn(1);
    }

    @Test
    public void testIsSatisfiedWhenPriceBelowMaximum() {
        // Basket total is 80.0, which is below maximum of 100.0
        assertTrue("Should be satisfied when price is below maximum", 
                  maxPriceCondition.isSatisfied(mockShoppingBasket, mockItemGetter));
    }

    @Test
    public void testIsSatisfiedWhenPriceEqualsMaximum() {
        // Add more items to reach exactly 100.0
        Map<String, Integer> orders = new HashMap<>();
        orders.put("item1", 2); // 2 * 30 = 60
        orders.put("item2", 2); // 2 * 20 = 40
        when(mockShoppingBasket.getOrders()).thenReturn(orders);
        when(mockShoppingBasket.getQuantity("item1")).thenReturn(2);
        when(mockShoppingBasket.getQuantity("item2")).thenReturn(2);
        // Total basket price = 100.0
        
        assertTrue("Should be satisfied when price equals maximum", 
                  maxPriceCondition.isSatisfied(mockShoppingBasket, mockItemGetter));
    }

    @Test
    public void testIsSatisfiedWhenPriceAboveMaximum() {
        // Add more items to exceed 100.0
        Map<String, Integer> orders = new HashMap<>();
        orders.put("item1", 2); // 2 * 30 = 60
        orders.put("item2", 2); // 2 * 20 = 40
        orders.put("item4", 1); // 1 * 50 = 50
        when(mockShoppingBasket.getOrders()).thenReturn(orders);
        when(mockShoppingBasket.getQuantity("item1")).thenReturn(2);
        when(mockShoppingBasket.getQuantity("item2")).thenReturn(2);
        when(mockShoppingBasket.getQuantity("item4")).thenReturn(1);
        // Total basket price = 150.0
        
        assertFalse("Should not be satisfied when price is above maximum", 
                   maxPriceCondition.isSatisfied(mockShoppingBasket, mockItemGetter));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSatisfiedWhenShoppingBasketIsNull() {
        maxPriceCondition.isSatisfied(null, mockItemGetter);
    }

    @Test(expected = IllegalArgumentException.class) 
    public void testIsSatisfiedWhenStoreIdIsNull() {
        when(mockShoppingBasket.getStoreId()).thenReturn(null);
        
        maxPriceCondition.isSatisfied(mockShoppingBasket, mockItemGetter);
    }

    @Test
    public void testZeroQuantity() {
        // Create empty basket (no items)
        Map<String, Integer> emptyOrders = new HashMap<>();
        when(mockShoppingBasket.getOrders()).thenReturn(emptyOrders);
        
        assertTrue("Should be satisfied when basket is empty (total price = 0)", 
                  maxPriceCondition.isSatisfied(mockShoppingBasket, mockItemGetter));
    }

    @Test
    public void testGetters() {
        assertEquals("Max price getter should return correct value", 
                    100.0, maxPriceCondition.getMaxPrice(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativePrice() {
        new MaxPriceCondition("test-id", -10.0);
    }

    @Test
    public void testGetId() {
        assertEquals("Should return correct ID", conditionId, maxPriceCondition.getId());
    }
}