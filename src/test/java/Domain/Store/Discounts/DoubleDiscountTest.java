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

public class DoubleDiscountTest {
    
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
    
    private DoubleDiscount doubleDiscount;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        discounts.add(discount2);
        doubleDiscount = new DoubleDiscount(itemFacade, discounts);
    }
    
    @Test
    public void testConstructorValid() {
        assertNotNull(doubleDiscount);
        assertNotNull(doubleDiscount.getId());
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        DoubleDiscount discount = new DoubleDiscount(existingId, itemFacade, discounts);
        
        assertEquals(existingId, discount.getId());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDiscounts() {
        new DoubleDiscount(itemFacade, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullItemFacade() {
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        new DoubleDiscount(null, discounts);
    }
    
    @Test
    public void testCalculatePriceAppliesBothDiscounts() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        // First discount gives 20% off
        Map<String, PriceBreakDown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new PriceBreakDown(100.0, 0.2));
        
        // Second discount gives 10% off
        Map<String, PriceBreakDown> breakdown2 = new HashMap<>();
        breakdown2.put("product1", new PriceBreakDown(100.0, 0.1));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.calculatePrice(basket)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket)).thenReturn(breakdown2);
        when(discount1.isQualified("product1")).thenReturn(true);
        when(discount2.isQualified("product1")).thenReturn(true);
        
        // Execute
        Map<String, PriceBreakDown> result = doubleDiscount.calculatePrice(basket);
        
        // Verify
        // Combined discount: 1 - (1-0.2)*(1-0.1) = 1 - 0.8*0.9 = 1 - 0.72 = 0.28 (28%)
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.28, breakdown.getDiscount(), 0.001);
        assertEquals(72.0, breakdown.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testCalculatePriceNotQualified() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        // Execute
        Map<String, PriceBreakDown> result = doubleDiscount.calculatePrice(basket);
        
        // Verify
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWithSingleDiscount() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        // Only first discount applies
        Map<String, PriceBreakDown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new PriceBreakDown(100.0, 0.25));
        
        // Second discount doesn't apply to this product
        Map<String, PriceBreakDown> breakdown2 = new HashMap<>();
        // product1 not in breakdown2, so it gets 0 discount from discount2
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.calculatePrice(basket)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket)).thenReturn(breakdown2);
        when(discount1.isQualified("product1")).thenReturn(true);
        
        // Execute
        Map<String, PriceBreakDown> result = doubleDiscount.calculatePrice(basket);
        
        // Verify - only first discount should apply
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.25, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testIsQualifiedWhenAnyDiscountQualifies() {
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(true);
        
        assertTrue(doubleDiscount.isQualified("product1"));
    }
    
    @Test
    public void testIsQualifiedWhenNoDiscountsQualify() {
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        assertFalse(doubleDiscount.isQualified("product1"));
    }
    
    @Test
    public void testHasUniqueId() {
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        discounts.add(discount2);
        DoubleDiscount another = new DoubleDiscount(itemFacade, discounts);
        
        assertNotEquals(doubleDiscount.getId(), another.getId());
    }
}