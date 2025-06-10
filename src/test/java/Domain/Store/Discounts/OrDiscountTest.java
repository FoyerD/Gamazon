package Domain.Store.Discounts;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;

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
    private Condition subCondition1;
    
    @Mock
    private Condition subCondition2;
    
    @Mock
    private BiFunction<String, String, Item> itemGetter;
    
    private OrDiscount orDiscount;
    private final String DISCOUNT_ID = "or-discount-1";
    private final String STORE_ID = "store123";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Mock basic basket methods that are checked in calculatePrice
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getOrders()).thenReturn(new HashMap<>()); // Non-null map
        
        when(discount1.getCondition()).thenReturn(subCondition1);
        when(discount2.getCondition()).thenReturn(subCondition2);
        
        List<Discount> discounts = Arrays.asList(discount1, discount2);
        orDiscount = new OrDiscount(DISCOUNT_ID, STORE_ID, discounts, condition, Discount.MergeType.MAX);
    }
    
    @Test
    public void testConstructorValid() {
        assertNotNull(orDiscount);
        assertEquals(DISCOUNT_ID, orDiscount.getId());
        assertEquals(STORE_ID, orDiscount.getStoreId());
        assertEquals(condition, orDiscount.getCondition());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullId() {
        List<Discount> discounts = Arrays.asList(discount1, discount2);
        new OrDiscount(null, STORE_ID, discounts, condition, Discount.MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDiscounts() {
        new OrDiscount(DISCOUNT_ID, STORE_ID, null, condition, Discount.MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyDiscounts() {
        new OrDiscount(DISCOUNT_ID, STORE_ID, Arrays.asList(), condition, Discount.MergeType.MAX);
    }
    
    @Test
    public void testCalculatePriceWhenConditionNotSatisfied() {
        // Setup
        Map<String, ItemPriceBreakdown> originalPrices = new HashMap<>();
        originalPrices.put("product1", new ItemPriceBreakdown(100.0, 0.0));
        
        when(basket.getBestPrice(itemGetter)).thenReturn(originalPrices);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = orDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should return original prices when main condition not satisfied
        assertEquals(originalPrices, result);
    }

    
    @Test
    public void testCalculatePriceWhenAtLeastOneSubConditionSatisfied() {
        // Setup
        Map<String, ItemPriceBreakdown> originalPrices = new HashMap<>();
        originalPrices.put("product1", new ItemPriceBreakdown(100.0, 0.0));
        
        when(basket.getBestPrice(itemGetter)).thenReturn(originalPrices);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(subCondition1.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(subCondition2.isSatisfied(basket, itemGetter)).thenReturn(true); // This satisfies the OR
        
        // Mock sub-discount calculations
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new ItemPriceBreakdown(100.0, 0.1));
        
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put("product1", new ItemPriceBreakdown(100.0, 0.2));
        
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket, itemGetter)).thenReturn(breakdown2);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = orDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should apply discounts when OR condition satisfied
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.2, breakdown.getDiscount(), 0.001); // Should pick max discount
    }
    
    @Test
    public void testCalculatePriceWhenAllSubConditionsFalse() {
        // Setup
        Map<String, ItemPriceBreakdown> originalPrices = new HashMap<>();
        originalPrices.put("product1", new ItemPriceBreakdown(100.0, 0.0));
        
        when(basket.getBestPrice(itemGetter)).thenReturn(originalPrices);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(subCondition1.isSatisfied(basket, itemGetter)).thenReturn(false);
        when(subCondition2.isSatisfied(basket, itemGetter)).thenReturn(false);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = orDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should return original prices when no sub-conditions satisfied
        assertEquals(originalPrices, result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePriceWithNullBasket() {
        orDiscount.calculatePrice(null, itemGetter);
    }
    
    @Test
    public void testCalculatePriceWithMulMergeType() {
        // Setup with MUL merge type
        List<Discount> discounts = Arrays.asList(discount1, discount2);
        OrDiscount mulOrDiscount = new OrDiscount(DISCOUNT_ID, STORE_ID, discounts, condition, Discount.MergeType.MUL);
        
        Map<String, ItemPriceBreakdown> originalPrices = new HashMap<>();
        originalPrices.put("product1", new ItemPriceBreakdown(100.0, 0.0));
        
        when(basket.getBestPrice(itemGetter)).thenReturn(originalPrices);
        when(condition.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(subCondition1.isSatisfied(basket, itemGetter)).thenReturn(true);
        when(subCondition2.isSatisfied(basket, itemGetter)).thenReturn(true);
        
        // Mock sub-discount calculations
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put("product1", new ItemPriceBreakdown(100.0, 0.1));
        
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put("product1", new ItemPriceBreakdown(100.0, 0.2));
        
        when(discount1.calculatePrice(basket, itemGetter)).thenReturn(breakdown1);
        when(discount2.calculatePrice(basket, itemGetter)).thenReturn(breakdown2);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = mulOrDiscount.calculatePrice(basket, itemGetter);
        
        // Verify - should apply multiplicative discount
        assertEquals(1, result.size());
        ItemPriceBreakdown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        // Combined discount: 1 - (1-0.1)*(1-0.2) = 1 - 0.9*0.8 = 0.28
        assertEquals(0.28, breakdown.getDiscount(), 0.001);
    }

}