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

public class MaxDiscountTest {
    
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
    
    private MaxDiscount maxDiscount;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        discounts.add(discount2);
        maxDiscount = new MaxDiscount(itemFacade, discounts);
    }
    
    @Test
    public void testConstructorWithSet() {
        assertNotNull(maxDiscount);
        assertNotNull(maxDiscount.getId());
    }
    
    @Test
    public void testConstructorWithTwoDiscounts() {
        MaxDiscount discount = new MaxDiscount(itemFacade, discount1, discount2);
        assertNotNull(discount);
        assertNotNull(discount.getId());
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        MaxDiscount discount = new MaxDiscount(existingId, itemFacade, discounts);
        
        assertEquals(existingId.toString(), discount.getId());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDiscounts() {
        new MaxDiscount(itemFacade, (Set<Discount>) null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyDiscounts() {
        new MaxDiscount(itemFacade, new HashSet<>());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDiscount1() {
        new MaxDiscount(itemFacade, null, discount2);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDiscount2() {
        new MaxDiscount(itemFacade, discount1, null);
    }
    
    @Test
    public void testCalculatePriceSelectsMaximumDiscount() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        Map<String, PriceBreakDown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new PriceBreakDown(100.0, 0.1));
        
        Map<String, PriceBreakDown> breakdown2 = new HashMap<>();
        breakdown2.put("product1", new PriceBreakDown(100.0, 0.3));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.calculatePrice(basket)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket)).thenReturn(breakdown2);
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount2.isQualified("product1")).thenReturn(true);
        
        // Execute
        Map<String, PriceBreakDown> result = maxDiscount.calculatePrice(basket);
        
        // Verify
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.3, breakdown.getDiscount(), 0.001); // Should select maximum
    }
    
    @Test
    public void testCalculatePriceNotQualified() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        // Execute
        Map<String, PriceBreakDown> result = maxDiscount.calculatePrice(basket);
        
        // Verify
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testIsQualifiedWhenAnyDiscountQualifies() {
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(true);
        
        assertTrue(maxDiscount.isQualified("product1"));
    }
    
    @Test
    public void testIsQualifiedWhenNoDiscountsQualify() {
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        assertFalse(maxDiscount.isQualified("product1"));
    }
    
    @Test
    public void testCalculatePriceWithZeroDiscounts() {
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        Map<String, PriceBreakDown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new PriceBreakDown(100.0, 0.0));
        
        Map<String, PriceBreakDown> breakdown2 = new HashMap<>();
        breakdown2.put("product1", new PriceBreakDown(100.0, 0.0));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.calculatePrice(basket)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket)).thenReturn(breakdown2);
        when(discount1.isQualified("product1")).thenReturn(true);
        
        Map<String, PriceBreakDown> result = maxDiscount.calculatePrice(basket);
        
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testHasUniqueId() {
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        discounts.add(discount2);
        MaxDiscount another = new MaxDiscount(itemFacade, discounts);
        
        assertNotEquals(maxDiscount.getId(), another.getId());
    }
}