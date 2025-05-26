package Domain.Store.Discounts;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;

public class OrDiscountTest {
    
    @Mock
    private ItemFacade itemFacade;
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Discount discount;
    
    @Mock
    private Condition condition1;
    
    @Mock
    private Condition condition2;
    
    @Mock
    private Item item;
    
    private OrDiscount orDiscount;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Set<Condition> conditions = new HashSet<>();
        conditions.add(condition1);
        conditions.add(condition2);
        orDiscount = new OrDiscount(itemFacade, discount, conditions);
    }
    
    @Test
    public void testConstructorValid() {
        assertNotNull(orDiscount);
        assertNotNull(orDiscount.getId());
        assertNotNull(orDiscount.getCondition());
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        Set<Condition> conditions = new HashSet<>();
        conditions.add(condition1);
        OrDiscount discountWithId = new OrDiscount(existingId, itemFacade, discount, conditions);
        
        assertEquals(existingId, discountWithId.getId());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullItemFacade() {
        Set<Condition> conditions = new HashSet<>();
        conditions.add(condition1);
        new OrDiscount(null, discount, conditions);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDiscount() {
        Set<Condition> conditions = new HashSet<>();
        conditions.add(condition1);
        new OrDiscount(itemFacade, null, conditions);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullConditions() {
        new OrDiscount(itemFacade, discount, null);
    }
    
    @Test
    public void testCalculatePriceWhenConditionSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        Map<String, PriceBreakDown> subDiscountResult = new HashMap<>();
        subDiscountResult.put("product1", new PriceBreakDown(100.0, 0.15));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount.calculatePrice(basket)).thenReturn(subDiscountResult);
        when(discount.isQualified("product1")).thenReturn(true);
        
        // Mock OR condition to be satisfied (at least one condition is true)
        when(condition1.isSatisfied(basket)).thenReturn(false);
        when(condition2.isSatisfied(basket)).thenReturn(true);
        
        // Execute
        Map<String, PriceBreakDown> result = orDiscount.calculatePrice(basket);
        
        // Verify
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.15, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenConditionNotSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        // Mock OR condition to not be satisfied (all conditions are false)
        when(condition1.isSatisfied(basket)).thenReturn(false);
        when(condition2.isSatisfied(basket)).thenReturn(false);
        
        // Execute
        Map<String, PriceBreakDown> result = orDiscount.calculatePrice(basket);
        
        // Verify - should not apply discount when condition not satisfied
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenNotQualified() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount.isQualified("product1")).thenReturn(false);
        
        // Execute
        Map<String, PriceBreakDown> result = orDiscount.calculatePrice(basket);
        
        // Verify
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testIsQualifiedDelegatesToUnderlyingDiscount() {
        when(discount.isQualified("product1")).thenReturn(true);
        assertTrue(orDiscount.isQualified("product1"));
        
        when(discount.isQualified("product1")).thenReturn(false);
        assertFalse(orDiscount.isQualified("product1"));
    }
    
    @Test
    public void testHasUniqueId() {
        Set<Condition> conditions = new HashSet<>();
        conditions.add(condition1);
        conditions.add(condition2);
        OrDiscount another = new OrDiscount(itemFacade, discount, conditions);
        
        assertNotEquals(orDiscount.getId(), another.getId());
    }
}