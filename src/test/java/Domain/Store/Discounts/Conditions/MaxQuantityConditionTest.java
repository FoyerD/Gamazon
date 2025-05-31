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

public class MaxQuantityConditionTest {
    
    @Mock
    private ItemFacade itemFacade;
    
    @Mock
    private ShoppingBasket basket;
    
    private MaxQuantityCondition condition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        condition = new MaxQuantityCondition(itemFacade, "product1", 10);
    }
    
    @Test
    public void testIsSatisfiedWhenQuantityBelowMaximum() {
        when(basket.getQuantity("product1")).thenReturn(5);
        assertTrue(condition.isSatisfied(basket));
        
        when(basket.getQuantity("product1")).thenReturn(0);
        assertTrue(condition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenQuantityEqualsMaximum() {
        when(basket.getQuantity("product1")).thenReturn(10);
        assertTrue(condition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenQuantityAboveMaximum() {
        when(basket.getQuantity("product1")).thenReturn(15);
        assertFalse(condition.isSatisfied(basket));
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        MaxQuantityCondition conditionWithId = new MaxQuantityCondition(
            existingId, itemFacade, "product1", 7);
        
        assertEquals(existingId.toString().toString(), conditionWithId.getId());
        assertEquals("product1", conditionWithId.getProductId());
        assertEquals(7, conditionWithId.getMaxQuantity());
    }
    
    @Test
    public void testGetters() {
        assertEquals("product1", condition.getProductId());
        assertEquals(10, condition.getMaxQuantity());
        assertNotNull(condition.getId());
    }
    
    @Test
    public void testMaxQuantityOfZero() {
        MaxQuantityCondition zeroMax = new MaxQuantityCondition(itemFacade, "product1", 0);
        
        when(basket.getQuantity("product1")).thenReturn(0);
        assertTrue(zeroMax.isSatisfied(basket));
        
        when(basket.getQuantity("product1")).thenReturn(1);
        assertFalse(zeroMax.isSatisfied(basket));
    }
    
    @Test
    public void testHasUniqueId() {
        MaxQuantityCondition another = new MaxQuantityCondition(itemFacade, "product1", 10);
        assertNotEquals(condition.getId(), another.getId());
    }
}