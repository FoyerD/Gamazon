package Domain.Store.Discounts;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import Domain.Store.Item;

public class PriceBreakdownTest {
    
    private ItemPriceBreakdown priceBreakdown;
    
    @Before
    public void setUp() {
        priceBreakdown = new ItemPriceBreakdown(100.0, 0.2);
    }
    
    @Test
    public void testConstructorWithTwoParameters() {
        assertEquals(100.0, priceBreakdown.getOriginalPrice(), 0.001);
        assertEquals(0.2, priceBreakdown.getDiscount(), 0.001);
        assertTrue(priceBreakdown.getDescriptions().isEmpty());
    }
    
    @Test
    public void testConstructorWithItem() {
        Item mockItem = mock(Item.class);
        when(mockItem.getPrice()).thenReturn(150.0);
        
        ItemPriceBreakdown breakdown = new ItemPriceBreakdown(mockItem);
        
        assertEquals(150.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
        assertTrue(breakdown.getDescriptions().isEmpty());
    }
    
    @Test
    public void testConstructorWithDescriptions() {
        List<String> descriptions = Arrays.asList("Holiday discount", "Member discount");
        ItemPriceBreakdown breakdown = new ItemPriceBreakdown(150.0, 0.3, descriptions);
        
        assertEquals(150.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.3, breakdown.getDiscount(), 0.001);
        assertEquals(2, breakdown.getDescriptions().size());
        assertTrue(breakdown.getDescriptions().contains("Holiday discount"));
        assertTrue(breakdown.getDescriptions().contains("Member discount"));
    }
    
    @Test
    public void testConstructorWithNullDescriptions() {
        ItemPriceBreakdown breakdown = new ItemPriceBreakdown(100.0, 0.1, null);
        
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.1, breakdown.getDiscount(), 0.001);
        assertTrue(breakdown.getDescriptions().isEmpty());
    }
    
    @Test
    public void testAddDescription() {
        priceBreakdown.addDescription("Test discount");
        List<String> descriptions = priceBreakdown.getDescriptions();
        
        assertEquals(1, descriptions.size());
        assertEquals("Test discount", descriptions.get(0));
    }
    
    @Test
    public void testAddMultipleDescriptions() {
        priceBreakdown.addDescription("First discount");
        priceBreakdown.addDescription("Second discount");
        
        List<String> descriptions = priceBreakdown.getDescriptions();
        assertEquals(2, descriptions.size());
        assertEquals("First discount", descriptions.get(0));
        assertEquals("Second discount", descriptions.get(1));
    }
    
    @Test
    public void testGetFinalPrice() {
        assertEquals(80.0, priceBreakdown.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testGetFinalPriceNoDiscount() {
        ItemPriceBreakdown noDiscount = new ItemPriceBreakdown(100.0, 0.0);
        assertEquals(100.0, noDiscount.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testGetFinalPriceFullDiscount() {
        ItemPriceBreakdown fullDiscount = new ItemPriceBreakdown(100.0, 1.0);
        assertEquals(0.0, fullDiscount.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testGetFinalPriceWithHalfDiscount() {
        ItemPriceBreakdown halfDiscount = new ItemPriceBreakdown(200.0, 0.5);
        assertEquals(100.0, halfDiscount.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testGetDescriptionsReturnsDefensiveCopy() {
        priceBreakdown.addDescription("Original description");
        List<String> descriptions = priceBreakdown.getDescriptions();
        
        // Modifying returned list should not affect internal state
        descriptions.add("Modified description");
        
        List<String> internalDescriptions = priceBreakdown.getDescriptions();
        assertEquals(1, internalDescriptions.size());
        assertEquals("Original description", internalDescriptions.get(0));
    }
    
    @Test
    public void testZeroPrice() {
        ItemPriceBreakdown zeroPrice = new ItemPriceBreakdown(0.0, 0.1);
        assertEquals(0.0, zeroPrice.getOriginalPrice(), 0.001);
        assertEquals(0.1, zeroPrice.getDiscount(), 0.001);
        assertEquals(0.0, zeroPrice.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testNegativePrice() {
        ItemPriceBreakdown negativePrice = new ItemPriceBreakdown(-50.0, 0.2);
        assertEquals(-50.0, negativePrice.getOriginalPrice(), 0.001);
        assertEquals(0.2, negativePrice.getDiscount(), 0.001);
        assertEquals(-40.0, negativePrice.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testCombineMaxMap() {
        // Setup multiple breakdowns
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put("item1", new ItemPriceBreakdown(100.0, 0.1));
        breakdown1.put("item2", new ItemPriceBreakdown(200.0, 0.2));
        
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put("item1", new ItemPriceBreakdown(100.0, 0.3)); // Higher discount
        breakdown2.put("item2", new ItemPriceBreakdown(200.0, 0.1)); // Lower discount
        
        List<Map<String, ItemPriceBreakdown>> breakdowns = List.of(breakdown1, breakdown2);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = ItemPriceBreakdown.combineMaxMap(breakdowns);
        
        // Verify - should pick maximum discount for each item
        assertEquals(0.3, result.get("item1").getDiscount(), 0.001);
        assertEquals(0.2, result.get("item2").getDiscount(), 0.001);
    }
    
    @Test
    public void testCombineMultiplicateMaps() {
        // Setup multiple breakdowns
        Map<String, ItemPriceBreakdown> breakdown1 = new HashMap<>();
        breakdown1.put("item1", new ItemPriceBreakdown(100.0, 0.2));
        
        Map<String, ItemPriceBreakdown> breakdown2 = new HashMap<>();
        breakdown2.put("item1", new ItemPriceBreakdown(100.0, 0.1));
        
        List<Map<String, ItemPriceBreakdown>> breakdowns = List.of(breakdown1, breakdown2);
        
        // Execute
        Map<String, ItemPriceBreakdown> result = ItemPriceBreakdown.combineMultiplicateMaps(breakdowns);
        
        // Verify - should multiply discounts: 1 - (1-0.2)*(1-0.1) = 1 - 0.8*0.9 = 0.28
        assertEquals(0.28, result.get("item1").getDiscount(), 0.001);
    }
    
    @Test
    public void testCombineMax() {
        ItemPriceBreakdown breakdown1 = new ItemPriceBreakdown(100.0, 0.15);
        ItemPriceBreakdown breakdown2 = new ItemPriceBreakdown(100.0, 0.25);
        
        ItemPriceBreakdown result = ItemPriceBreakdown.combineMax(breakdown1, breakdown2);
        
        assertEquals(breakdown2, result); // Should return the one with higher discount
        assertEquals(0.25, result.getDiscount(), 0.001);
    }
    
    @Test
    public void testCombineMultiplicate() {
        List<String> descriptions1 = List.of("Discount 1");
        List<String> descriptions2 = List.of("Discount 2");
        
        ItemPriceBreakdown breakdown1 = new ItemPriceBreakdown(100.0, 0.2, descriptions1);
        ItemPriceBreakdown breakdown2 = new ItemPriceBreakdown(100.0, 0.1, descriptions2);
        
        ItemPriceBreakdown result = ItemPriceBreakdown.combineMultiplicate(breakdown1, breakdown2);
        
        assertEquals(100.0, result.getOriginalPrice(), 0.001);
        assertEquals(0.28, result.getDiscount(), 0.001); // 1 - (1-0.2)*(1-0.1) = 0.28
        assertEquals(2, result.getDescriptions().size());
        assertTrue(result.getDescriptions().contains("Discount 1"));
        assertTrue(result.getDescriptions().contains("Discount 2"));
    }
    
    @Test(expected = AssertionError.class)
    public void testCombineMaxWithDifferentPrices() {
        ItemPriceBreakdown breakdown1 = new ItemPriceBreakdown(100.0, 0.15);
        ItemPriceBreakdown breakdown2 = new ItemPriceBreakdown(200.0, 0.25);
        
        // Should throw AssertionError due to different original prices
        ItemPriceBreakdown.combineMax(breakdown1, breakdown2);
    }
    
    @Test(expected = AssertionError.class)
    public void testCombineMultiplicateWithDifferentPrices() {
        ItemPriceBreakdown breakdown1 = new ItemPriceBreakdown(100.0, 0.2);
        ItemPriceBreakdown breakdown2 = new ItemPriceBreakdown(150.0, 0.1);
        
        // Should throw AssertionError due to different original prices
        ItemPriceBreakdown.combineMultiplicate(breakdown1, breakdown2);
    }
}