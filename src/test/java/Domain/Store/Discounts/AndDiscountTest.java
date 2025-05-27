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
        
        andDiscount = new AndDiscount(itemFacade, discounts);
    }
    
    @Test
    public void testConstructorWithSet() {
        assertNotNull(andDiscount);
        assertNotNull(andDiscount.getId());
        assertNotNull(andDiscount.getCondition());
    }
    
    @Test
    public void testConstructorWithTwoDiscounts() {
        AndDiscount discount = new AndDiscount(itemFacade, discount1, discount2);
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
        
        // Mock the AND condition to return false (not all conditions satisfied)
        when(condition1.isSatisfied(basket)).thenReturn(true);
        when(condition2.isSatisfied(basket)).thenReturn(false);
        
        // Execute
        Map<String, PriceBreakDown> result = andDiscount.calculatePrice(basket);
        
        // Verify - should return no discount when conditions not satisfied
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenAllConditionsSatisfied() {
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
        
        // Mock all conditions to be satisfied
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
    public void testIsQualifiedWhenAnyDiscountQualifies() {
        // CORRECT LOGIC: isQualified should return true if ANY discount qualifies
        // This is for efficiency - if any discount could apply, it's worth processing
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        assertTrue("AndDiscount should qualify if ANY discount qualifies", 
                  andDiscount.isQualified("product1"));
        
        // Test the other way around
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(true);
        
        assertTrue("AndDiscount should qualify if ANY discount qualifies", 
                  andDiscount.isQualified("product1"));
    }
    
    @Test
    public void testIsQualifiedWhenBothDiscountsQualify() {
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount2.isQualified("product1")).thenReturn(true);
        
        assertTrue("AndDiscount should qualify when both discounts qualify", 
                  andDiscount.isQualified("product1"));
    }
    
    @Test
    public void testIsQualifiedWhenNoDiscountsQualify() {
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        assertFalse("AndDiscount should not qualify when no discounts qualify", 
                   andDiscount.isQualified("product1"));
    }
    
    @Test
    public void testAndCompositionLogic() {
        // This test demonstrates the AND composition logic:
        // - CONDITIONS: ALL conditions must be satisfied for any discounts to apply
        // - QUALIFICATION: ANY discount qualifying means processing is worthwhile
        // - APPLICATION: Individual discounts apply to products they qualify for
        
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1); // qualifies for discount1 only
        orders.put("product2", 1); // qualifies for discount2 only  
        orders.put("product3", 1); // qualifies for both discounts
        orders.put("product4", 1); // qualifies for neither discount
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        
        // Setup items
        Item item1 = mock(Item.class);
        Item item2 = mock(Item.class);
        Item item3 = mock(Item.class);
        Item item4 = mock(Item.class);
        
        when(itemFacade.getItem("store1", "product1")).thenReturn(item1);
        when(itemFacade.getItem("store1", "product2")).thenReturn(item2);
        when(itemFacade.getItem("store1", "product3")).thenReturn(item3);
        when(itemFacade.getItem("store1", "product4")).thenReturn(item4);
        
        when(item1.getPrice()).thenReturn(100.0);
        when(item2.getPrice()).thenReturn(100.0);
        when(item3.getPrice()).thenReturn(100.0);
        when(item4.getPrice()).thenReturn(100.0);
        
        // Setup discount qualifications
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount1.isQualified("product2")).thenReturn(false);
        when(discount1.isQualified("product3")).thenReturn(true);
        when(discount1.isQualified("product4")).thenReturn(false);
        
        when(discount2.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product2")).thenReturn(true);
        when(discount2.isQualified("product3")).thenReturn(true);
        when(discount2.isQualified("product4")).thenReturn(false);
        
        // Test qualification logic (efficiency check)
        assertTrue("Product1 should qualify (discount1 qualifies)", 
                  andDiscount.isQualified("product1"));
        assertTrue("Product2 should qualify (discount2 qualifies)", 
                  andDiscount.isQualified("product2"));
        assertTrue("Product3 should qualify (both qualify)", 
                  andDiscount.isQualified("product3"));
        assertFalse("Product4 should not qualify (neither qualifies)", 
                   andDiscount.isQualified("product4"));
    }
    
    @Test
    public void testConditionsApplyToAllDiscounts() {
        // Test that when conditions are not satisfied, no discounts apply
        // even if individual discounts would qualify for specific products
        
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        // Product qualifies for discount1
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        // But not all conditions are satisfied
        when(condition1.isSatisfied(basket)).thenReturn(true);
        when(condition2.isSatisfied(basket)).thenReturn(false); // This fails the AND
        
        // Execute
        Map<String, PriceBreakDown> result = andDiscount.calculatePrice(basket);
        
        // Verify - no discount should apply because conditions aren't met
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testHasUniqueId() {
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        discounts.add(discount2);
        AndDiscount another = new AndDiscount(itemFacade, discounts);
        
        assertNotEquals(andDiscount.getId(), another.getId());
    }
}