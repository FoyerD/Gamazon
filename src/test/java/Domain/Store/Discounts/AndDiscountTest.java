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

public class AndDiscountTest {
    
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
    private Condition condition1;
    
    @Mock
    private Condition condition2;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    private AndDiscount andDiscount;
    private final String STORE_ID = "store123";
    private final String PRODUCT_ID = "product1";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        when(discount1.getCondition()).thenReturn(condition1);
        when(discount2.getCondition()).thenReturn(condition2);
        
        List<Discount> discounts = List.of(discount1, discount2);
        
        andDiscount = new AndDiscount("test-id", STORE_ID, discounts, condition, MergeType.MAX);
    }
    
    @Test
    public void testConstructorValid() {
        assertNotNull(andDiscount);
        assertEquals("test-id", andDiscount.getId());
        assertEquals(STORE_ID, andDiscount.getStoreId());
        assertEquals(condition, andDiscount.getCondition());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullId() {
        new AndDiscount(null, STORE_ID, List.of(discount1), condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyId() {
        new AndDiscount("", STORE_ID, List.of(discount1), condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStoreId() {
        new AndDiscount("test-id", null, List.of(discount1), condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullCondition() {
        new AndDiscount("test-id", STORE_ID, List.of(discount1), null, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDiscounts() {
        new AndDiscount("test-id", STORE_ID, null, condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyDiscounts() {
        new AndDiscount("test-id", STORE_ID, List.of(), condition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullMergeType() {
        new AndDiscount("test-id", STORE_ID, List.of(discount1), condition, null);
    }
    
    @Test
    public void testCalculatePriceWhenConditionNotSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getPriceBreakdowns(itemGetter)).thenReturn(createOriginalPriceBreakdown());
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        // Main condition not satisfied
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = andDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should return original prices when main condition not satisfied
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenNotAllSubConditionsSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getPriceBreakdowns(itemGetter)).thenReturn(createOriginalPriceBreakdown());
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        // Main condition satisfied but not all sub-conditions
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = andDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should return original prices when not all conditions satisfied
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculatePriceWhenAllConditionsSatisfiedWithMaxMerge() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 2);
        
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.1));
        
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.2));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket, itemGetter)).thenReturn(breakdown2);
        
        // All conditions satisfied
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = andDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should apply maximum discount with MAX merge type
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.2, breakdown.getDiscount(), 0.001); // Should pick the higher discount
    }
    
    @Test
    public void testCalculatePriceWhenAllConditionsSatisfiedWithMulMerge() {
        // Setup with MUL merge type
        AndDiscount mulAndDiscount = new AndDiscount("test-mul-id", STORE_ID, 
                                                    List.of(discount1, discount2), condition, MergeType.MUL);
        
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 1);
        
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.2));
        
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.1));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket, itemGetter)).thenReturn(breakdown2);
        
        // All conditions satisfied
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(condition2.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = mulAndDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should apply multiplicative discount: 1 - (1-0.2)*(1-0.1) = 1 - 0.8*0.9 = 0.28
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.28, breakdown.getDiscount(), 0.001);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithNullBasket() {
        andDiscount.calculatePrice(null, itemGetter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithBasketNullStoreId() {
        when(basket.getStoreId()).thenReturn(null);
        andDiscount.calculatePrice(basket, itemGetter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithBasketNullOrders() {
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getOrders()).thenReturn(null);
        andDiscount.calculatePrice(basket, itemGetter);
    }
    
    @Test
    public void testGetDiscounts() {
        List<Discount> discounts = andDiscount.getDiscounts();
        assertEquals(2, discounts.size());
        assertTrue(discounts.contains(discount1));
        assertTrue(discounts.contains(discount2));
    }
    
    @Test
    public void testGetMergeType() {
        assertEquals(MergeType.MAX, andDiscount.getMergeType());
    }
    
    @Test
    public void testEquals() {
        AndDiscount same = new AndDiscount("test-id", "other-store", List.of(discount1), condition, MergeType.MUL);
        AndDiscount different = new AndDiscount("different-id", STORE_ID, List.of(discount1), condition, MergeType.MAX);
        
        assertEquals("Discounts with same ID should be equal", andDiscount, same);
        assertNotEquals("Discounts with different ID should not be equal", andDiscount, different);
    }
    
    @Test
    public void testHashCode() {
        assertEquals("Hash code should be based on ID", 
                    andDiscount.getId().hashCode(), andDiscount.hashCode());
    }
    
    @Test
    public void testToString() {
        String toString = andDiscount.toString();
        assertTrue("ToString should contain class name", toString.contains("AndDiscount"));
        assertTrue("ToString should contain ID", toString.contains("test-id"));
    }
    
    private Map<String, ItemPriceBreakdown> createOriginalPriceBreakdown() {
        Map<String, ItemPriceBreakdown> breakdown = new HashMap<>();
        breakdown.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.0));
        return breakdown;
    }
}