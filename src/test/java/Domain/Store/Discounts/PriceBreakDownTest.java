package Domain.Store.Discounts;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

public class PriceBreakDownTest {
    
    private ItemPriceBreakdown priceBreakDown;
    
    @Before
    public void setUp() {
        priceBreakDown = new ItemPriceBreakdown(100.0, 0.2);
    }
    
    @Test
    public void testConstructorWithTwoParameters() {
        assertEquals(100.0, priceBreakDown.getOriginalPrice(), 0.001);
        assertEquals(0.2, priceBreakDown.getDiscount(), 0.001);
        assertTrue(priceBreakDown.getDescriptions().isEmpty());
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
        priceBreakDown.addDescription("Test discount");
        List<String> descriptions = priceBreakDown.getDescriptions();
        
        assertEquals(1, descriptions.size());
        assertEquals("Test discount", descriptions.get(0));
    }
    
    @Test
    public void testAddMultipleDescriptions() {
        priceBreakDown.addDescription("First discount");
        priceBreakDown.addDescription("Second discount");
        
        List<String> descriptions = priceBreakDown.getDescriptions();
        assertEquals(2, descriptions.size());
        assertEquals("First discount", descriptions.get(0));
        assertEquals("Second discount", descriptions.get(1));
    }
    
    @Test
    public void testGetFinalPrice() {
        assertEquals(80.0, priceBreakDown.getFinalPrice(), 0.001);
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
        priceBreakDown.addDescription("Original description");
        List<String> descriptions = priceBreakDown.getDescriptions();
        
        // Modifying returned list should not affect internal state
        descriptions.add("Modified description");
        
        List<String> internalDescriptions = priceBreakDown.getDescriptions();
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
}