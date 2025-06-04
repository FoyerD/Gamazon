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
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class SimpleDiscountTest {
    
    @Mock
    private DiscountQualifier qualifier;
    
    @Mock
    private Condition condition;
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Item item;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    private SimpleDiscount simpleDiscount;
    private final String STORE_ID = "store123";
    private final String PRODUCT_ID = "product1";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        simpleDiscount = new SimpleDiscount("test-id", STORE_ID, 0.2, qualifier, condition);
    }
    
    @Test
    public void testConstructorValid() {
        assertNotNull(simpleDiscount);
        assertEquals("test-id", simpleDiscount.getId());
        assertEquals(STORE_ID, simpleDiscount.getStoreId());
        assertEquals(condition, simpleDiscount.getCondition());
        assertEquals(0.2, simpleDiscount.getDiscountPercentage(), 0.001);
        assertEquals(qualifier, simpleDiscount.getQualifier());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullId() {
        new SimpleDiscount(null, STORE_ID, 0.2, qualifier, condition);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyId() {
        new SimpleDiscount("", STORE_ID, 0.2, qualifier, condition);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStoreId() {
        new SimpleDiscount("test-id", null, 0.2, qualifier, condition);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyStoreId() {
        new SimpleDiscount("test-id", "", 0.2, qualifier, condition);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullCondition() {
        new SimpleDiscount("test-id", STORE_ID, 0.2, qualifier, null);
    }
    
    @Test
    public void testCalculatePriceQualifiedWithCondition() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(qualifier.isQualified(item)).thenReturn(true);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = simpleDiscount.calculatePrice(basket, itemGetter);
        
        // Verify
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.2, breakdown.getDiscount(), 0.001);
        assertEquals(80.0, breakdown.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testCalculatePriceNotQualified() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(qualifier.isQualified(item)).thenReturn(false);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = simpleDiscount.calculatePrice(basket, itemGetter);
        
        // Verify
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
        assertEquals(100.0, breakdown.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testCalculatePriceConditionNotSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(qualifier.isQualified(item)).thenReturn(true);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(basket.getPriceBreakdowns(itemGetter)).thenReturn(createOriginalPriceBreakdown());
        
        // Execute
        Map<String, ItemPriceBreakdown> result = simpleDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should return original prices when condition not satisfied
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get(PRODUCT_ID);
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testIsQualified() {
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(qualifier.isQualified(item)).thenReturn(true);
        
        assertTrue(simpleDiscount.isQualified(STORE_ID, PRODUCT_ID, itemGetter));
    }
    
    @Test
    public void testIsNotQualified() {
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(qualifier.isQualified(item)).thenReturn(false);
        
        assertFalse(simpleDiscount.isQualified(STORE_ID, PRODUCT_ID, itemGetter));
    }
    
    @Test
    public void testMultipleProductsInBasket() {
        String product2Id = "product2";
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 1);
        orders.put(product2Id, 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        
        // Setup for product1 (qualified)
        when(itemGetter.apply(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(50.0);
        when(qualifier.isQualified(item)).thenReturn(true);
        
        // Setup for product2 (not qualified) 
        Item item2 = mock(Item.class);
        when(itemGetter.apply(STORE_ID, product2Id)).thenReturn(item2);
        when(item2.getPrice()).thenReturn(75.0);
        when(qualifier.isQualified(item2)).thenReturn(false);
        
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        Map<String, ItemPriceBreakdown> result = simpleDiscount.calculatePrice(basket, itemGetter);
        
        assertEquals(2, result.size());
        
        // Product1 should get discount
        ItemPriceBreakdown breakdown1 = result.get(PRODUCT_ID);
        assertEquals(0.2, breakdown1.getDiscount(), 0.001);
        
        // Product2 should not get discount
        ItemPriceBreakdown breakdown2 = result.get(product2Id);
        assertEquals(0.0, breakdown2.getDiscount(), 0.001);
    }
    
    @Test
    public void testConditionApplies() {
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        assertTrue(simpleDiscount.conditionApplies(basket, itemGetter));
        
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        assertFalse(simpleDiscount.conditionApplies(basket, itemGetter));
    }
    
    @Test
    public void testSettersAndGetters() {
        // Test setStoreId
        simpleDiscount.setStoreId("newStore");
        assertEquals("newStore", simpleDiscount.getStoreId());
        
        // Test setCondition
        Condition newCondition = mock(Condition.class);
        simpleDiscount.setCondition(newCondition);
        assertEquals(newCondition, simpleDiscount.getCondition());
    }
    
    @Test
    public void testEquals() {
        SimpleDiscount same = new SimpleDiscount("test-id", "other-store", 0.5, qualifier, condition);
        SimpleDiscount different = new SimpleDiscount("different-id", STORE_ID, 0.2, qualifier, condition);
        
        assertEquals("Discounts with same ID should be equal", simpleDiscount, same);
        assertNotEquals("Discounts with different ID should not be equal", simpleDiscount, different);
    }
    
    @Test
    public void testHashCode() {
        assertEquals("Hash code should be based on ID", 
                    simpleDiscount.getId().hashCode(), simpleDiscount.hashCode());
    }
    
    @Test
    public void testToString() {
        String toString = simpleDiscount.toString();
        assertTrue("ToString should contain class name", toString.contains("SimpleDiscount"));
        assertTrue("ToString should contain ID", toString.contains("test-id"));
    }
    
    private Map<String, ItemPriceBreakdown> createOriginalPriceBreakdown() {
        Map<String, ItemPriceBreakdown> breakdown = new HashMap<>();
        breakdown.put(PRODUCT_ID, new ItemPriceBreakdown(100.0, 0.0));
        return breakdown;
    }
}