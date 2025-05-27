package Domain.Store.Discounts.Conditions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.UUID;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Item;

public class MaxPriceConditionTest {

    private MaxPriceCondition maxPriceCondition;
    
    @Mock
    private ItemFacade mockItemFacade;
    
    private ShoppingBasket validShoppingBasket;
    private String storeId;
    private String clientId;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Create condition with max price of 100.0
        maxPriceCondition = new MaxPriceCondition(mockItemFacade, 100.0);
        
        // Create valid shopping basket
        storeId = "store123";
        clientId = "client456";
        validShoppingBasket = new ShoppingBasket(storeId, clientId);
        
        // Mock items with specific prices
        Item item1 = new Item(storeId, "item1", 30.0, 10, "Test item 1");
        Item item2 = new Item(storeId, "item2", 20.0, 10, "Test item 2");
        Item item3 = new Item(storeId, "item3", 20.0, 10, "Test item 3");
        Item item4 = new Item(storeId, "item4", 50.0, 10, "Test item 4");
        
        when(mockItemFacade.getItem(storeId, "item1")).thenReturn(item1);
        when(mockItemFacade.getItem(storeId, "item2")).thenReturn(item2);
        when(mockItemFacade.getItem(storeId, "item3")).thenReturn(item3);
        when(mockItemFacade.getItem(storeId, "item4")).thenReturn(item4);
        
        // Add some items to the basket for testing
        validShoppingBasket.addOrder("item1", 2); // 2 items at 30.0 each = 60.0
        validShoppingBasket.addOrder("item2", 1); // 1 item at 20.0 each = 20.0
        // Total basket price = 80.0
    }

    @Test
    public void testIsSatisfiedWhenPriceBelowMaximum() {
        // Basket total is 80.0, which is below maximum of 100.0
        assertTrue("Should be satisfied when price is below maximum", 
                  maxPriceCondition.isSatisfied(validShoppingBasket));
    }

    @Test
    public void testIsSatisfiedWhenPriceEqualsMaximum() {
        // Add more items to reach exactly 100.0
        validShoppingBasket.addOrder("item3", 1); // 1 item at 20.0 = 20.0
        // Total basket price = 100.0
        
        assertTrue("Should be satisfied when price equals maximum", 
                  maxPriceCondition.isSatisfied(validShoppingBasket));
    }

    @Test
    public void testIsSatisfiedWhenPriceAboveMaximum() {
        // Add more items to exceed 100.0
        validShoppingBasket.addOrder("item3", 2); // 2 items at 20.0 each = 40.0
        validShoppingBasket.addOrder("item4", 1); // 1 item at 50.0 each = 50.0
        // Total basket price = 170.0
        
        assertFalse("Should not be satisfied when price is above maximum", 
                   maxPriceCondition.isSatisfied(validShoppingBasket));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSatisfiedWhenShoppingBasketIsNull() {
        // This should throw IllegalArgumentException: "ShoppingBasket and StoreId cannot be null"
        maxPriceCondition.isSatisfied(null);
    }

    @Test(expected = IllegalArgumentException.class) 
    public void testIsSatisfiedWhenStoreIdIsNull() {
        // Create basket with null store ID
        ShoppingBasket basketWithNullStoreId = new ShoppingBasket(null, clientId);
        basketWithNullStoreId.addOrder("item1", 1);
        
        // This should throw IllegalArgumentException: "ShoppingBasket and StoreId cannot be null"
        maxPriceCondition.isSatisfied(basketWithNullStoreId);
    }

    @Test
    public void testZeroQuantity() {
        // Create empty basket (no items)
        ShoppingBasket emptyBasket = new ShoppingBasket(storeId, clientId);
        
        assertTrue("Should be satisfied when basket is empty (total price = 0)", 
                  maxPriceCondition.isSatisfied(emptyBasket));
    }

    @Test
    public void testGetters() {
        assertEquals("Max price getter should return correct value", 
                    100.0, maxPriceCondition.getMaxPrice(), 0.001);
    }

    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        MaxPriceCondition conditionWithId = new MaxPriceCondition(existingId, mockItemFacade, 150.0);
        
        assertEquals("Should use provided UUID", existingId, conditionWithId.getId());
        assertEquals("Should set max price correctly", 150.0, conditionWithId.getMaxPrice(), 0.001);
    }

    @Test
    public void testHasUniqueId() {
        MaxPriceCondition condition1 = new MaxPriceCondition(mockItemFacade, 100.0);
        MaxPriceCondition condition2 = new MaxPriceCondition(mockItemFacade, 100.0);
        
        assertNotEquals("Each condition should have unique ID", 
                       condition1.getId(), condition2.getId());
        assertNotNull("ID should not be null", condition1.getId());
        assertNotNull("ID should not be null", condition2.getId());
    }
}