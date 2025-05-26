package Domain.Store.Discounts.Conditions;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Item;

public class MaxPriceConditionTest {
    
    @Mock
    private ItemFacade itemFacade;
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Item item;
    
    private MaxPriceCondition condition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        condition = new MaxPriceCondition(itemFacade, "store1", "product1", 100.0);
    }
    
    @Test
    public void testIsSatisfiedWhenPriceBelowMaximum() {
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(30.0);
        when(basket.getQuantity("product1")).thenReturn(2);
        
        // Total price: 30 * 2 = 60, which is below maximum of 100
        assertTrue(condition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenPriceEqualsMaximum() {
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(50.0);
        when(basket.getQuantity("product1")).thenReturn(2);
        
        // Total price: 50 * 2 = 100, which equals maximum
        assertTrue(condition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenPriceAboveMaximum() {
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(60.0);
        when(basket.getQuantity("product1")).thenReturn(2);
        
        // Total price: 60 * 2 = 120, which is above maximum of 100
        assertFalse(condition.isSatisfied(basket));
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        MaxPriceCondition conditionWithId = new MaxPriceCondition(
            existingId, itemFacade, "store1", "product1", 75.0);
        
        assertEquals(existingId, conditionWithId.getId());
        assertEquals("store1", conditionWithId.getStoreId());
        assertEquals("product1", conditionWithId.getProductId());
        assertEquals(75.0, conditionWithId.getMaxPrice(), 0.001);
    }
    
    @Test
    public void testGetters() {
        assertEquals("store1", condition.getStoreId());
        assertEquals("product1", condition.getProductId());
        assertEquals(100.0, condition.getMaxPrice(), 0.001);
        assertNotNull(condition.getId());
    }
    
    @Test
    public void testZeroQuantity() {
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(150.0);
        when(basket.getQuantity("product1")).thenReturn(0);
        
        // Total price: 150 * 0 = 0, which is below maximum
        assertTrue(condition.isSatisfied(basket));
    }
    
    @Test
    public void testHasUniqueId() {
        MaxPriceCondition another = new MaxPriceCondition(itemFacade, "store1", "product1", 100.0);
        assertNotEquals(condition.getId(), another.getId());
    }
}