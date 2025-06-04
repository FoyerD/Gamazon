package Domain.Store.Discounts;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Discount.MergeType;

public class XorDiscountTest {
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Discount discount1;
    
    @Mock
    private Discount discount2;
    
    @Mock
    private Item item;
    
    @Mock
    private Condition condition;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    private XorDiscount xorDiscount;
    private final String STORE_ID = "store123";
    private final String PRODUCT_ID = "product1";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        xorDiscount = new XorDiscount("test-id", STORE_ID, discount1, discount2, condition, MergeType.MAX);
    }
    
    @Test
    public void testConstructorValid() {
        assertNotNull(xorDiscount);
        assertEquals("test-id", xorDiscount.getId());
        assertEquals(STORE_ID, xorDiscount.getStoreId());
        assertEquals(condition, xorDiscount.getCondition());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullId() {
        new XorDiscount(null, STORE_ID, discount1, discount2, condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyId() {
        new XorDiscount("", STORE_ID, discount1, discount2, condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStoreId() {
        new XorDiscount("test-id", null, discount1, discount2, condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullCondition() {
        new XorDiscount("test-id", STORE_ID, discount1, discount2, null, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullMergeType() {
        new XorDiscount("test-id", STORE_ID, discount1, discount2, condition, null);
    }
    
    @Test
    public void testCalculatePriceWhenConditionNotSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 1);
        
        // First discount result (should be used when condition not satisfied)
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.2));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(breakdown1);
        
        // Condition not satisfied
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should apply first discount when condition not satisfied
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.2, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenConditionSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 1);
        
        // Second discount result (should be used when condition satisfied)
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.15));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(discount2.calculatePrice(basket, itemGetter)).thenReturn(breakdown2);
        
        // Condition satisfied
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should apply second discount when condition satisfied
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.15, breakdown.getDiscount(), 0.001);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithNullBasket() {
        xorDiscount.calculatePrice(null, itemGetter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithBasketNullStoreId() {
        when(basket.getStoreId()).thenReturn(null);
        xorDiscount.calculatePrice(basket, itemGetter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithBasketNullOrders() {
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getOrders()).thenReturn(null);
        xorDiscount.calculatePrice(basket, itemGetter);
    }
    
    @Test
    public void testGetDiscounts() {
        // XorDiscount extends CompositeDiscount with exactly 2 discounts
        assertEquals(2, xorDiscount.getDiscounts().size());
        assertTrue(xorDiscount.getDiscounts().contains(discount1));
        assertTrue(xorDiscount.getDiscounts().contains(discount2));
    }
    
    @Test
    public void testGetMergeType() {
        assertEquals(MergeType.MAX, xorDiscount.getMergeType());
    }
    
    @Test
    public void testMultipleProducts() {
        // Setup
        String product2Id = "product2";
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 1);
        orders.put(product2Id, 2);
        
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.1));
        breakdown1.put(product2Id, new ItemPriceBreakdown(50.0, 0.2));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(breakdown1);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket, itemGetter);
        
        // Verify
        assertEquals(2, result.size());
        assertEquals(0.1, result.get(PRODUCT_ID).getDiscount(), 0.001);
        assertEquals(0.2, result.get(product2Id).getDiscount(), 0.001);
    }
    
    @Test
    public void testEquals() {
        XorDiscount same = new XorDiscount("test-id", "other-store", discount2, discount1, condition, MergeType.MUL);
        XorDiscount different = new XorDiscount("different-id", STORE_ID, discount1, discount2, condition, MergeType.MAX);
        
        assertEquals("Discounts with same ID should be equal", xorDiscount, same);
        assertNotEquals("Discounts with different ID should not be equal", xorDiscount, different);
    }
    
    @Test
    public void testHashCode() {
        assertEquals("Hash code should be based on ID", 
                    xorDiscount.getId().hashCode(), xorDiscount.hashCode());
    }
    
    @Test
    public void testToString() {
        String toString = xorDiscount.toString();
        assertTrue("ToString should contain class name", toString.contains("XorDiscount"));
        assertTrue("ToString should contain ID", toString.contains("test-id"));
    }
}