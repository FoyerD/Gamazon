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

public class XorDiscountTest {
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Discount discount1;
    
    @Mock
    private Discount discount2;
    
    @Mock
    private Condition condition;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    private XorDiscount xorDiscount;
    private final String DISCOUNT_ID = "xor-discount-1";
    private final String STORE_ID = "store123";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        xorDiscount = new XorDiscount(DISCOUNT_ID, STORE_ID, discount1, discount2, condition, Discount.MergeType.MAX);

        // Ensure basket always returns valid store ID and empty orders by default
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getOrders()).thenReturn(new HashMap<>());
    }

    
    @Test
    public void testConstructorValid() {
        assertNotNull(xorDiscount);
        assertEquals(DISCOUNT_ID, xorDiscount.getId());
        assertEquals(STORE_ID, xorDiscount.getStoreId());
        assertEquals(condition, xorDiscount.getCondition());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullId() {
        new XorDiscount(null, STORE_ID, discount1, discount2, condition, Discount.MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyId() {
        new XorDiscount("", STORE_ID, discount1, discount2, condition, Discount.MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStoreId() {
        new XorDiscount(DISCOUNT_ID, null, discount1, discount2, condition, Discount.MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyStoreId() {
        new XorDiscount(DISCOUNT_ID, "", discount1, discount2, condition, Discount.MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullCondition() {
        new XorDiscount(DISCOUNT_ID, STORE_ID, discount1, discount2, null, Discount.MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullMergeType() {
        new XorDiscount(DISCOUNT_ID, STORE_ID, discount1, discount2, condition, null);
    }
    
    @Test
    public void testConstructorWithNullDiscount1() {
        try {
            new XorDiscount(DISCOUNT_ID, STORE_ID, null, discount2, condition, Discount.MergeType.MAX);
            fail("Expected IllegalArgumentException or NullPointerException");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Test passes — expected exception type
        }
    }

    
    @Test
    public void testConstructorWithNullDiscount2() {
        try {
            new XorDiscount(DISCOUNT_ID, STORE_ID, discount1, null, condition, Discount.MergeType.MAX);
            fail("Expected IllegalArgumentException or NullPointerException");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Expected exception
        }
    }

    
    @Test
    public void testCalculatePriceWhenConditionNotSatisfied() {
        // Setup
        Map<String, ItemPriceBreakdown> discount1Result = new HashMap<>();
        discount1Result.put("product1", new ItemPriceBreakdown(100.0, 0.1));
        
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(discount1Result);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should apply first discount when condition not satisfied
        assertEquals(discount1Result, result);
        verify(discount1).calculatePrice(basket, itemGetter);
        verify(discount2, never()).calculatePrice(basket, itemGetter);
    }
    
    @Test
    public void testCalculatePriceWhenConditionSatisfied() {
        // Setup
        Map<String, ItemPriceBreakdown> discount2Result = new HashMap<>();
        discount2Result.put("product1", new ItemPriceBreakdown(100.0, 0.2));
        
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(discount2.calculatePrice(basket, itemGetter)).thenReturn(discount2Result);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should apply second discount when condition satisfied
        assertEquals(discount2Result, result);
        verify(discount2).calculatePrice(basket, itemGetter);
        verify(discount1, never()).calculatePrice(basket, itemGetter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithNullBasket() {
        xorDiscount.calculatePrice(null, itemGetter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithNullStoreIdInBasket() {
        when(basket.getStoreId()).thenReturn(null);
        xorDiscount.calculatePrice(basket, itemGetter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithNullOrdersInBasket() {
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getOrders()).thenReturn(null);
        xorDiscount.calculatePrice(basket, itemGetter);
    }
    
    @Test
    public void testCalculatePriceWithValidBasketButEmptyOrders() {
        // Setup empty but valid basket
        Map<String, Integer> emptyOrders = new HashMap<>();
        Map<String, ItemPriceBreakdown> discount1Result = new HashMap<>();
        
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getOrders()).thenReturn(emptyOrders);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(discount1Result);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = xorDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should handle empty orders gracefully
        assertEquals(discount1Result, result);
    }
    
    @Test
    public void testGetDiscounts() {
        assertEquals("Should have exactly 2 discounts", 2, xorDiscount.getDiscounts().size());
        assertTrue("Should contain first discount", xorDiscount.getDiscounts().contains(discount1));
        assertTrue("Should contain second discount", xorDiscount.getDiscounts().contains(discount2));
    }
    
    @Test
    public void testGetMergeType() {
        assertEquals("Should return correct merge type", Discount.MergeType.MAX, xorDiscount.getMergeType());
    }
    
    @Test
    public void testXorBehaviorWithDifferentConditionStates() {
        // Test multiple calls with different condition states
        Map<String, ItemPriceBreakdown> result1 = new HashMap<>();
        result1.put("product1", new ItemPriceBreakdown(100.0, 0.1));
        
        Map<String, ItemPriceBreakdown> result2 = new HashMap<>();
        result2.put("product1", new ItemPriceBreakdown(100.0, 0.2));
        
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(result1);
        when(discount2.calculatePrice(basket, itemGetter)).thenReturn(result2);
        
        // First call - condition false, should use discount1
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        Map<String, ItemPriceBreakdown> firstResult = xorDiscount.calculatePrice(basket, itemGetter);
        assertEquals(result1, firstResult);
        
        // Second call - condition true, should use discount2
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        Map<String, ItemPriceBreakdown> secondResult = xorDiscount.calculatePrice(basket, itemGetter);
        assertEquals(result2, secondResult);
    }
    
    @Test
    public void testInheritanceFromCompositeDiscount() {
        assertTrue("XorDiscount should be instance of CompositeDiscount", 
                  xorDiscount instanceof CompositeDiscount);
        assertTrue("XorDiscount should be instance of Discount", 
                  xorDiscount instanceof Discount);
    }
    
    @Test
    public void testEqualsAndHashCode() {
        XorDiscount anotherXorDiscount = new XorDiscount(
            DISCOUNT_ID, STORE_ID, discount1, discount2, condition, Discount.MergeType.MAX);
        
        // Same ID should be equal (assuming equals is based on ID)
        assertEquals("Discounts with same ID should be equal", xorDiscount, anotherXorDiscount);
        assertEquals("Discounts with same ID should have same hash code", 
                    xorDiscount.hashCode(), anotherXorDiscount.hashCode());
        
        // Different ID should not be equal
        XorDiscount differentIdDiscount = new XorDiscount(
            "different-id", STORE_ID, discount1, discount2, condition, Discount.MergeType.MAX);
        assertNotEquals("Discounts with different IDs should not be equal", 
                       xorDiscount, differentIdDiscount);
    }
    
    @Test
    public void testToString() {
        String toString = xorDiscount.toString();
        assertTrue("toString should contain class name", toString.contains("XorDiscount"));
        assertTrue("toString should contain ID", toString.contains(DISCOUNT_ID));
    }
    
    @Test
    public void testWithDifferentMergeTypes() {
        // Test with MUL merge type
        XorDiscount mulXorDiscount = new XorDiscount(
            "xor-mul-id", STORE_ID, discount1, discount2, condition, Discount.MergeType.MUL);
        
        assertEquals("Should have MUL merge type", Discount.MergeType.MUL, mulXorDiscount.getMergeType());
        assertNotNull("Should be created successfully", mulXorDiscount);
    }
    
    @Test
    public void testConditionEvaluationIsolation() {
        // Verify that condition evaluation doesn't affect the discounts themselves
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        Map<String, ItemPriceBreakdown> result = new HashMap<>();
        result.put("product1", new ItemPriceBreakdown(100.0, 0.15));
        when(discount2.calculatePrice(basket, itemGetter)).thenReturn(result);
        
        xorDiscount.calculatePrice(basket, itemGetter);
        
        // Verify that only the condition and appropriate discount were called
        verify(condition, times(1)).isSatisfied(basket, itemGetter);
        verify(discount2, times(1)).calculatePrice(basket, itemGetter);
        verify(discount1, never()).calculatePrice(basket, itemGetter);
    }
}