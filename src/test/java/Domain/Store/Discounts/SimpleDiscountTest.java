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
    private final String DISCOUNT_ID = "simple-discount-1";
    private final String STORE_ID = "store123";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        simpleDiscount = new SimpleDiscount(DISCOUNT_ID, STORE_ID, 0.2, qualifier, condition);
    }
    
    @Test
    public void testConstructorWithCondition() {
        assertNotNull(simpleDiscount);
        assertEquals(DISCOUNT_ID, simpleDiscount.getId());
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
        new SimpleDiscount(DISCOUNT_ID, null, 0.2, qualifier, condition);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullCondition() {
        new SimpleDiscount(DISCOUNT_ID, STORE_ID, 0.2, qualifier, null);
    }
    
    @Test
    public void testCalculatePriceQualifiedWithCondition() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(qualifier.isQualified(item)).thenReturn(true);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = simpleDiscount.calculatePrice(basket, itemGetter);
        
        // Verify
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.2, breakdown.getDiscount(), 0.001);
        assertEquals(80.0, breakdown.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testCalculatePriceNotQualified() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(itemGetter.apply(STORE_ID, "product1")).thenReturn(item);
        when(item.getPrice()).thenReturn(100.0);
        when(qualifier.isQualified(item)).thenReturn(false);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = simpleDiscount.calculatePrice(basket, itemGetter);
        
        // Verify
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
        assertEquals(100.0, breakdown.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testCalculatePriceConditionNotSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        Map<String, ItemPriceBreakdown> originalPrices = new HashMap<>();
        originalPrices.put("product1", new ItemPriceBreakdown(item));
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getBestPrice(itemGetter)).thenReturn(originalPrices);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = simpleDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should return original prices when condition not satisfied
        assertEquals(originalPrices, result);
    }
    
    @Test
    public void testIsQualified() {
        when(itemGetter.apply(STORE_ID, "product1")).thenReturn(item);
        when(qualifier.isQualified(item)).thenReturn(true);
        
        assertTrue(simpleDiscount.isQualified(STORE_ID, "product1", itemGetter));
    }
    
    @Test
    public void testIsNotQualified() {
        when(itemGetter.apply(STORE_ID, "product1")).thenReturn(item);
        when(qualifier.isQualified(item)).thenReturn(false);
        
        assertFalse(simpleDiscount.isQualified(STORE_ID, "product1", itemGetter));
    }
    
    @Test
    public void testMultipleProductsInBasket() {
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        orders.put("product2", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        
        // Setup for product1 (qualified)
        Item item1 = mock(Item.class);
        when(itemGetter.apply(STORE_ID, "product1")).thenReturn(item1);
        when(item1.getPrice()).thenReturn(50.0);
        when(qualifier.isQualified(item1)).thenReturn(true);
        
        // Setup for product2 (not qualified) 
        Item item2 = mock(Item.class);
        when(itemGetter.apply(STORE_ID, "product2")).thenReturn(item2);
        when(item2.getPrice()).thenReturn(75.0);
        when(qualifier.isQualified(item2)).thenReturn(false);
        
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        Map<String, ItemPriceBreakdown> result = simpleDiscount.calculatePrice(basket, itemGetter);
        
        assertEquals(2, result.size());
        
        // Product1 should get discount
        ItemPriceBreakdown breakdown1 = result.get("product1");
        assertEquals(0.2, breakdown1.getDiscount(), 0.001);
        
        // Product2 should not get discount
        ItemPriceBreakdown breakdown2 = result.get("product2");
        assertEquals(0.0, breakdown2.getDiscount(), 0.001);
    }
}