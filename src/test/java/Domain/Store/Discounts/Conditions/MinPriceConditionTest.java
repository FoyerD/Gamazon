package Domain.Store.Discounts.Conditions;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Item;

public class MinPriceConditionTest {
    
    @Mock
    private ItemFacade itemFacade;
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Item item;
    
    private MinPriceCondition condition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        condition = new MinPriceCondition(itemFacade, 100.0);
        
        // Mock the basket's storeId
        when(basket.getStoreId()).thenReturn("store1");
    }
    
    @Test
    public void testIsSatisfiedWhenPriceMeetsMinimum() {
        // Create a map with one product
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(50.0);
        when(basket.getQuantity("product1")).thenReturn(2);
        
        // Total price: 50 * 2 = 100, which equals minimum
        assertTrue(condition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenPriceAboveMinimum() {
        // Create a map with one product
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(60.0);
        when(basket.getQuantity("product1")).thenReturn(2);
        
        // Total price: 60 * 2 = 120, which is above minimum of 100
        assertTrue(condition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenPriceBelowMinimum() {
        // Create a map with one product
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(30.0);
        when(basket.getQuantity("product1")).thenReturn(2);
        
        // Total price: 30 * 2 = 60, which is below minimum of 100
        assertFalse(condition.isSatisfied(basket));
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        MinPriceCondition conditionWithId = new MinPriceCondition(
            existingId, itemFacade, 50.0);
        
        assertEquals(existingId.toString(), conditionWithId.getId());
        assertEquals(50.0, conditionWithId.getMinPrice(), 0.001);
    }
    
    @Test
    public void testGetters() {
        assertEquals(100.0, condition.getMinPrice(), 0.001);
        assertNotNull(condition.getId());
    }
    
    @Test
    public void testZeroQuantity() {
        // Create an empty map (no products)
        Map<String, Integer> orders = new HashMap<>();
        
        when(basket.getOrders()).thenReturn(orders);
        
        // Total price: 0, which is below minimum
        assertFalse(condition.isSatisfied(basket));
    }
    
    @Test
    public void testHasUniqueId() {
        MinPriceCondition another = new MinPriceCondition(itemFacade, 100.0);
        assertNotEquals(condition.getId(), another.getId());
    }
}