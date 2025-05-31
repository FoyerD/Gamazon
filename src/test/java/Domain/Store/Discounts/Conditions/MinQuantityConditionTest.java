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

public class MinQuantityConditionTest {
    
    @Mock
    private ItemFacade itemFacade;
    
    @Mock
    private ShoppingBasket basket;
    
    private MinQuantityCondition condition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        condition = new MinQuantityCondition(itemFacade, "product1", 5);
    }
    
    @Test
    public void testIsSatisfiedWhenQuantityMeetsMinimum() {
        when(basket.getQuantity("product1")).thenReturn(5);
        assertTrue(condition.isSatisfied(basket));
        
        when(basket.getQuantity("product1")).thenReturn(10);
        assertTrue(condition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenQuantityBelowMinimum() {
        when(basket.getQuantity("product1")).thenReturn(3);
        assertFalse(condition.isSatisfied(basket));
        
        when(basket.getQuantity("product1")).thenReturn(0);
        assertFalse(condition.isSatisfied(basket));
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        MinQuantityCondition conditionWithId = new MinQuantityCondition(
            existingId, itemFacade, "product1", 3);
        
        assertEquals(existingId.toString(), conditionWithId.getId());
        assertEquals("product1", conditionWithId.getProductId());
        assertEquals(3, conditionWithId.getMinQuantity());
    }
    
    @Test
    public void testGetters() {
        assertEquals("product1", condition.getProductId());
        assertEquals(5, condition.getMinQuantity());
        assertNotNull(condition.getId());
    }
    
    @Test
    public void testMinQuantityOfZero() {
        MinQuantityCondition zeroMin = new MinQuantityCondition(itemFacade, "product1", 0);
        
        when(basket.getQuantity("product1")).thenReturn(0);
        assertTrue(zeroMin.isSatisfied(basket));
        
        when(basket.getQuantity("product1")).thenReturn(1);
        assertTrue(zeroMin.isSatisfied(basket));
    }
    
    @Test
    public void testHasUniqueId() {
        MinQuantityCondition another = new MinQuantityCondition(itemFacade, "product1", 5);
        assertNotEquals(condition.getId(), another.getId());
    }
}