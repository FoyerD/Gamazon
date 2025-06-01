package Domain.Store.Discounts;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Item;

public class XorDiscountTest {
    
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
    
    private XorDiscount xorDiscount;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        xorDiscount = new XorDiscount(itemFacade, discount1, discount2);
    }
    
    @Test
    public void testConstructorValid() {
        assertNotNull(xorDiscount);
        assertNotNull(xorDiscount.getId());
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        XorDiscount discountWithId = new XorDiscount(existingId, itemFacade, discount1, discount2);
        
        assertEquals(existingId.toString(), discountWithId.getId());
    }
    
    @Test
    public void testCalculatePriceWhenOnlyFirstDiscountApplies() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        // First discount applies with 20% off
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new ItemPriceBreakdown(100.0, 0.2));
        
        // Second discount doesn't apply (0% discount)
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put("product1", new ItemPriceBreakdown(100.0, 0.0));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.calculatePrice(basket)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket)).thenReturn(breakdown2);
        when(discount1.isQualified("product1")).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket);
        
        // Verify - should apply first discount
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.2, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenOnlySecondDiscountApplies() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        // First discount doesn't apply (0% discount)
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new ItemPriceBreakdown(100.0, 0.0));
        
        // Second discount applies with 15% off
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put("product1", new ItemPriceBreakdown(100.0, 0.15));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.calculatePrice(basket)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket)).thenReturn(breakdown2);
        when(discount1.isQualified("product1")).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket);
        
        // Verify - should apply second discount
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.15, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenBothDiscountsApply() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        // Both discounts apply
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new ItemPriceBreakdown(100.0, 0.2));
        
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put("product1", new ItemPriceBreakdown(100.0, 0.15));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.calculatePrice(basket)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket)).thenReturn(breakdown2);
        when(discount1.isQualified("product1")).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket);
        
        // Verify - should apply no discount when both apply (XOR logic)
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenNeitherDiscountApplies() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        
        // Neither discount applies
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new ItemPriceBreakdown(100.0, 0.0));
        
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put("product1", new ItemPriceBreakdown(100.0, 0.0));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.calculatePrice(basket)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket)).thenReturn(breakdown2);
        when(discount1.isQualified("product1")).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket);
        
        // Verify - should apply no discount when neither applies
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get("product1");
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
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket);
        
        // Verify
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testIsQualifiedWhenAnyDiscountQualifies() {
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(true);
        
        assertTrue(xorDiscount.isQualified("product1"));
    }
    
    @Test
    public void testIsQualifiedWhenNoDiscountsQualify() {
        when(discount1.isQualified("product1")).thenReturn(false);
        when(discount2.isQualified("product1")).thenReturn(false);
        
        assertFalse(xorDiscount.isQualified("product1"));
    }
    
    @Test
    public void testHasUniqueId() {
        XorDiscount another = new XorDiscount(itemFacade, discount1, discount2);
        assertNotEquals(xorDiscount.getId(), another.getId());
    }
}