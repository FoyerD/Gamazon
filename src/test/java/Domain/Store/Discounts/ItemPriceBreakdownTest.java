package Domain.Store.Discounts;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Domain.Store.Item;

public class ItemPriceBreakdownTest {
    
    private ItemPriceBreakdown priceBreakdown;
    private Item mockItem;
    
    @Before
    public void setUp() {
        mockItem = mock(Item.class);
        when(mockItem.getPrice()).thenReturn(100.0);
        priceBreakdown = new ItemPriceBreakdown(100.0, 0.2);
    }
    
    @Test
    public void testConstructorWithItem() {
        ItemPriceBreakdown breakdown = new ItemPriceBreakdown(mockItem);
        
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
        assertTrue(breakdown.getDescriptions().isEmpty());
    }
    
    @Test
    public void testConstructorWithTwoParameters() {
        assertEquals(100.0, priceBreakdown.getOriginalPrice(), 0.001);
        assertEquals(0.2, priceBreakdown.getDiscount(), 0.001);
        assertTrue(priceBreakdown.getDescriptions().isEmpty());
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
    public void testCombineMax() {
        ItemPriceBreakdown breakdown1 = new ItemPriceBreakdown(100.0, 0.2);
        ItemPriceBreakdown breakdown2 = new ItemPriceBreakdown(100.0, 0.3);
        
        ItemPriceBreakdown result = ItemPriceBreakdown.combineMax(breakdown1, breakdown2);
        
        assertEquals(breakdown2, result); // Should return the one with higher discount
        assertEquals(0.3, result.getDiscount(), 0.001);
    }
    
    @Test
    public void testCombineMultiplicate() {
        List<String> desc1 = Arrays.asList("First discount");
        List<String> desc2 = Arrays.asList("Second discount");
        
        ItemPriceBreakdown breakdown1 = new ItemPriceBreakdown(100.0, 0.2, desc1);
        ItemPriceBreakdown breakdown2 = new ItemPriceBreakdown(100.0, 0.1, desc2);
        
        ItemPriceBreakdown result = ItemPriceBreakdown.combineMultiplicate(breakdown1, breakdown2);
        
        assertEquals(100.0, result.getOriginalPrice(), 0.001);
        // Combined discount: 1 - (1-0.2)*(1-0.1) = 1 - 0.8*0.9 = 0.28
        assertEquals(0.28, result.getDiscount(), 0.001);
        assertEquals(2, result.getDescriptions().size());
    }
    
    @Test
    public void testCombineMaxMap() {
        Map<String, ItemPriceBreakdown> map1 = new HashMap<>();
        map1.put("product1", new ItemPriceBreakdown(100.0, 0.1));
        map1.put("product2", new ItemPriceBreakdown(200.0, 0.2));
        
        Map<String, ItemPriceBreakdown> map2 = new HashMap<>();
        map2.put("product1", new ItemPriceBreakdown(100.0, 0.15)); // Higher discount
        map2.put("product3", new ItemPriceBreakdown(150.0, 0.1));
        
        List<Map<String, ItemPriceBreakdown>> maps = Arrays.asList(map1, map2);
        Map<String, ItemPriceBreakdown> result = ItemPriceBreakdown.combineMaxMap(maps);
        
        assertEquals(3, result.size());
        assertEquals(0.15, result.get("product1").getDiscount(), 0.001); // Should pick max
        assertEquals(0.2, result.get("product2").getDiscount(), 0.001);
        assertEquals(0.1, result.get("product3").getDiscount(), 0.001);
    }
    
    @Test
    public void testCombineMultiplicateMaps() {
        Map<String, ItemPriceBreakdown> map1 = new HashMap<>();
        map1.put("product1", new ItemPriceBreakdown(100.0, 0.2));
        
        Map<String, ItemPriceBreakdown> map2 = new HashMap<>();
        map2.put("product1", new ItemPriceBreakdown(100.0, 0.1));
        
        List<Map<String, ItemPriceBreakdown>> maps = Arrays.asList(map1, map2);
        Map<String, ItemPriceBreakdown> result = ItemPriceBreakdown.combineMultiplicateMaps(maps);
        
        assertEquals(1, result.size());
        // Combined discount: 1 - (1-0.2)*(1-0.1) = 1 - 0.8*0.9 = 0.28
        assertEquals(0.28, result.get("product1").getDiscount(), 0.001);
    }
    
    @Test
    public void testCalculateFinalPriceFromList() {
        List<ItemPriceBreakdown> breakdowns = Arrays.asList(
            new ItemPriceBreakdown(100.0, 0.1), // final price: 90
            new ItemPriceBreakdown(200.0, 0.2), // final price: 160
            new ItemPriceBreakdown(50.0, 0.0)   // final price: 50
        );
        
        double totalFinalPrice = ItemPriceBreakdown.calculateFinalPrice(breakdowns);
        assertEquals(300.0, totalFinalPrice, 0.001); // 90 + 160 + 50
    }
    
    @Test
    public void testCalculateFinalPriceFromMap() {
        Map<String, ItemPriceBreakdown> breakdowns = new HashMap<>();
        breakdowns.put("product1", new ItemPriceBreakdown(100.0, 0.1));
        breakdowns.put("product2", new ItemPriceBreakdown(200.0, 0.2));
        
        double totalFinalPrice = ItemPriceBreakdown.calculateFinalPrice(breakdowns);
        assertEquals(250.0, totalFinalPrice, 0.001); // 90 + 160
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculateFinalPriceWithNullList() {
        ItemPriceBreakdown.calculateFinalPrice((List<ItemPriceBreakdown>) null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCalculateFinalPriceWithEmptyList() {
        ItemPriceBreakdown.calculateFinalPrice(Arrays.asList());
    }
}