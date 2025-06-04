package Domain.Store.Discounts;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Discount.MergeType;

public class OrDiscountTest {
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Discount discount1;
    
    @Mock
    private Discount discount2;
    
    @Mock
    private Condition condition;
    
    @Mock
    private Condition condition1;
    
    @Mock
    private Condition condition2;
    
    @Mock
    private Item item;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    private OrDiscount orDiscount;
    private final String STORE_ID = "store123";
    private final String PRODUCT_ID = "product1";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        when(discount1.getCondition()).thenReturn(condition1);
        when(discount2.getCondition()).thenReturn(condition2);
        
        List<Discount> discounts = List.of(discount1, discount2);
        orDiscount = new OrDiscount("test-id", STORE_ID, discounts, condition, MergeType.MAX);
    }
    
    @Test
    public void testConstructorValid() {
        assertNotNull(orDiscount);
        assertEquals("test-id", orDiscount.getId());
        assertEquals(STORE_ID, orDiscount.getStoreId());
        assertEquals(condition, orDiscount.getCondition());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullId() {
        new OrDiscount(null, STORE_ID, List.of(discount1), condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyId() {
        new OrDiscount("", STORE_ID, List.of(discount1), condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStoreId() {
        new OrDiscount("test-id", null, List.of(discount1), condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullCondition() {
        new OrDiscount("test-id", STORE_ID, List.of(discount1), null, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDiscounts() {
        new OrDiscount("test-id", STORE_ID, null, condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyDiscounts() {
        new OrDiscount("test-id", STORE_ID, List.of(), condition, MergeType.MAX);
    }
    
    @Test
    public void testCalculatePriceWhenConditionNotSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 1);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getPriceBreakdowns(itemGetter)).thenReturn(createOriginalPriceBreakdown());
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        // Main condition not satisfied
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = orDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should return original prices when main condition not satisfied
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenOneSubConditionSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 1);
        
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.15));
        
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.25));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket, itemGetter)).thenReturn(breakdown2);
        
        // Main condition satisfied and one sub-condition satisfied
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = orDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should apply discount since at least one condition is satisfied
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.25, breakdown.getDiscount(), 0.001); // Should get the max discount
    }
    
    @Test
    public void testCalculatePriceWhenNoSubConditionsSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 1);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getPriceBreakdowns(itemGetter)).thenReturn(createOriginalPriceBreakdown());
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        // Main condition satisfied but no sub-conditions satisfied
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = orDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should return original prices when no sub-conditions satisfied
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWithMultipleConditionsSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 1);
        
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.1));
        
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.3));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket, itemGetter)).thenReturn(breakdown2);
        
        // Main condition and both sub-conditions satisfied
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = orDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should apply maximum discount with MAX merge type
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.3, breakdown.getDiscount(), 0.001); // Should pick the higher discount
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithNullBasket() {
        orDiscount.calculatePrice(null, itemGetter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithBasketNullStoreId() {
        when(basket.getStoreId()).thenReturn(null);
        orDiscount.calculatePrice(basket, itemGetter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithBasketNullOrders() {
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getOrders()).thenReturn(null);
        orDiscount.calculatePrice(basket, itemGetter);
    }
    
    @Test
    public void testGetDiscounts() {
        List<Discount> discounts = orDiscount.getDiscounts();
        assertEquals(2, discounts.size());
        assertTrue(discounts.contains(discount1));
        assertTrue(discounts.contains(discount2));
    }
    
    @Test
    public void testGetMergeType() {
        assertEquals(MergeType.MAX, orDiscount.getMergeType());
    }
    
    @Test
    public void testEquals() {
        OrDiscount same = new OrDiscount("test-id", "other-store", List.of(discount1), condition, MergeType.MUL);
        OrDiscount different = new OrDiscount("different-id", STORE_ID, List.of(discount1), condition, MergeType.MAX);
        
        assertEquals("Discounts with same ID should be equal", orDiscount, same);
        assertNotEquals("Discounts with different ID should not be equal", orDiscount, different);
    }
    
    @Test
    public void testHashCode() {
        assertEquals("Hash code should be based on ID", 
                    orDiscount.getId().hashCode(), orDiscount.hashCode());
    }
    
    @Test
    public void testToString() {
        String toString = orDiscount.toString();
        assertTrue("ToString should contain class name", toString.contains("OrDiscount"));
        assertTrue("ToString should contain ID", toString.contains("test-id"));
    }
    
    private Map<String, ItemPriceBreakdown> createOriginalPriceBreakdown() {
        Map<String, ItemPriceBreakdown> breakdown = new HashMap<>();
        breakdown.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.0));
        return breakdown;
    }

}