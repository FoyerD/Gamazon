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
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class AndDiscountTest {
    
    @Mock
    private ItemFacade itemFacade;
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Discount discount1;
    
    @Mock
    private Discount discount2;
    
    @Mock
    private Item item;
    
    @Mock
    private DiscountQualifier qualifier;
    
    @Mock
    private Condition condition1;
    
    @Mock
    private Condition condition2;
    
    private AndDiscount andDiscount;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        when(discount1.getCondition()).thenReturn(condition1);
        when(discount2.getCondition()).thenReturn(condition2);
        
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        discounts.add(discount2);
        
        andDiscount = new AndDiscount(itemFacade, 0.2f, qualifier, discounts);
    }
    
    @Test
    public void testConstructorWithSet() {
        assertNotNull(andDiscount);
        assertNotNull(andDiscount.getId());
        assertNotNull(andDiscount.getCondition());
    }
    
    @Test
    public void testConstructorWithTwoDiscounts() {
        AndDiscount discount = new AndDiscount(itemFacade, 0.15f, qualifier, discount1, discount2);
        assertNotNull(discount);
        assertNotNull(discount.getId());
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        discounts.add(discount2);
        
        AndDiscount discount = new AndDiscount(existingId, itemFacade, discounts);
        assertEquals(existingId, discount.getId());
    }
    
    @Test
    public void testCalculatePriceWhenConditionNotSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        // Mock the AND condition to return false
        when(condition1.isSatisfied(basket)).thenReturn(true);
        when(condition2.isSatisfied(basket)).thenReturn(false); // This makes AND condition false
        
        // Execute
        Map<String, PriceBreakDown> result = andDiscount.calculatePrice(basket);
        
        // Verify - should return no discount when condition not satisfied
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenConditionSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        Map<String, PriceBreakDown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new PriceBreakDown(100.0, 0.1));
        
        Map<String, PriceBreakDown> breakdown2 = new HashMap<>();
        breakdown2.put("product1", new PriceBreakDown(100.0, 0.2));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        when(discount1.calculatePrice(basket)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket)).thenReturn(breakdown2);
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount2.isQualified("product1")).thenReturn(true);
        
        // Mock the AND condition to return true
        when(condition1.isSatisfied(basket)).thenReturn(true);
        when(condition2.isSatisfied(basket)).thenReturn(true);
        
        // Execute
        Map<String, PriceBreakDown> result = andDiscount.calculatePrice(basket);
        
        // Verify - should apply the maximum discount when all conditions satisfied
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.2, breakdown.getDiscount(), 0.001); // Should pick the higher discount
    }
    
    @Test
    public void testIsQualifiedWhenAllDiscountsQualify() {
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount2.isQualified("product1")).thenReturn(true);
        
        assertTrue(andDiscount.isQualified("product1"));
    }
    
    @Test
    public void testIsQualifiedWhenOneDiscountDoesNotQualify() {
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        // This tests the FIXED logic - should return false when ANY discount is not qualified
        assertFalse(andDiscount.isQualified("product1"));
    }
    
    @Test
    public void testIsQualifiedWhenNoDiscountsQualify() {
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        assertFalse(andDiscount.isQualified("product1"));
    }
    
    @Test
    public void testIsQualifiedCompletelyOppositeOfOriginalBuggyBehavior() {
        // Test case 1: Both qualify -> should return true (FIXED)
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount2.isQualified("product1")).thenReturn(true);
        assertTrue("When both discounts qualify, AndDiscount should qualify", 
                  andDiscount.isQualified("product1"));
        
        // Test case 2: First qualifies, second doesn't -> should return false (FIXED)
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount2.isQualified("product1")).thenReturn(false);
        assertFalse("When any discount doesn't qualify, AndDiscount should not qualify", 
                   andDiscount.isQualified("product1"));
        
        // Test case 3: First doesn't qualify, second does -> should return false (FIXED)
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(true);
        assertFalse("When any discount doesn't qualify, AndDiscount should not qualify", 
                   andDiscount.isQualified("product1"));
        
        // Test case 4: Neither qualifies -> should return false (FIXED)
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(false);
        assertFalse("When no discounts qualify, AndDiscount should not qualify", 
                   andDiscount.isQualified("product1"));
    }
    
    @Test
    public void testHasUniqueId() {
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        discounts.add(discount2);
        AndDiscount another = new AndDiscount(itemFacade, 0.2f, qualifier, discounts);
        
        assertNotEquals(andDiscount.getId(), another.getId());
    }
}