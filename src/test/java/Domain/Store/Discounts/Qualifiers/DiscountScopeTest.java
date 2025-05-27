package Domain.Store.Discounts.Qualifiers;

import static org.junit.Assert.*;

import org.junit.Test;

public class DiscountScopeTest {
    
    @Test
    public void testEnumValues() {
        // Test that all expected enum values exist
        DiscountScope[] values = DiscountScope.values();
        
        assertEquals("Should have exactly 3 enum values", 3, values.length);
        
        // Verify specific values exist
        assertTrue("PRODUCT should exist", containsValue(values, DiscountScope.PRODUCT));
        assertTrue("CATEGORY should exist", containsValue(values, DiscountScope.CATEGORY));
        assertTrue("STORE should exist", containsValue(values, DiscountScope.STORE));
    }
    
    @Test
    public void testValueOfProduct() {
        // Test valueOf with PRODUCT
        DiscountScope scope = DiscountScope.valueOf("PRODUCT");
        assertEquals("valueOf should return PRODUCT", DiscountScope.PRODUCT, scope);
    }
    
    @Test
    public void testValueOfCategory() {
        // Test valueOf with CATEGORY
        DiscountScope scope = DiscountScope.valueOf("CATEGORY");
        assertEquals("valueOf should return CATEGORY", DiscountScope.CATEGORY, scope);
    }
    
    @Test
    public void testValueOfStore() {
        // Test valueOf with STORE
        DiscountScope scope = DiscountScope.valueOf("STORE");
        assertEquals("valueOf should return STORE", DiscountScope.STORE, scope);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalidValue() {
        // Test valueOf with invalid value should throw IllegalArgumentException
        DiscountScope.valueOf("INVALID_SCOPE");
    }
    
    @Test(expected = NullPointerException.class)
    public void testValueOfNull() {
        // Test valueOf with null should throw NullPointerException
        DiscountScope.valueOf(null);
    }
    
    @Test
    public void testEnumEquality() {
        // Test enum equality
        assertEquals("PRODUCT should equal PRODUCT", 
                    DiscountScope.PRODUCT, DiscountScope.PRODUCT);
        assertEquals("CATEGORY should equal CATEGORY", 
                    DiscountScope.CATEGORY, DiscountScope.CATEGORY);
        assertEquals("STORE should equal STORE", 
                    DiscountScope.STORE, DiscountScope.STORE);
        
        assertNotEquals("PRODUCT should not equal CATEGORY", 
                       DiscountScope.PRODUCT, DiscountScope.CATEGORY);
        assertNotEquals("PRODUCT should not equal STORE", 
                       DiscountScope.PRODUCT, DiscountScope.STORE);
        assertNotEquals("CATEGORY should not equal STORE", 
                       DiscountScope.CATEGORY, DiscountScope.STORE);
    }
    
    @Test
    public void testEnumToString() {
        // Test toString method
        assertEquals("PRODUCT toString should return 'PRODUCT'", 
                    "PRODUCT", DiscountScope.PRODUCT.toString());
        assertEquals("CATEGORY toString should return 'CATEGORY'", 
                    "CATEGORY", DiscountScope.CATEGORY.toString());
        assertEquals("STORE toString should return 'STORE'", 
                    "STORE", DiscountScope.STORE.toString());
    }
    
    @Test
    public void testEnumName() {
        // Test name method
        assertEquals("PRODUCT name should return 'PRODUCT'", 
                    "PRODUCT", DiscountScope.PRODUCT.name());
        assertEquals("CATEGORY name should return 'CATEGORY'", 
                    "CATEGORY", DiscountScope.CATEGORY.name());
        assertEquals("STORE name should return 'STORE'", 
                    "STORE", DiscountScope.STORE.name());
    }
    
    @Test
    public void testEnumOrdinal() {
        // Test ordinal values (order in which they're declared)
        assertEquals("PRODUCT should have ordinal 0", 0, DiscountScope.PRODUCT.ordinal());
        assertEquals("CATEGORY should have ordinal 1", 1, DiscountScope.CATEGORY.ordinal());
        assertEquals("STORE should have ordinal 2", 2, DiscountScope.STORE.ordinal());
    }
    
    @Test
    public void testEnumInSwitchStatement() {
        // Test that enum can be used in switch statements
        for (DiscountScope scope : DiscountScope.values()) {
            String result = getScopeDescription(scope);
            assertNotNull("Switch statement should handle all enum values", result);
            assertFalse("Description should not be empty", result.isEmpty());
        }
    }
    
    // Helper method for testing enum values
    private boolean containsValue(DiscountScope[] values, DiscountScope target) {
        for (DiscountScope value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
    
    // Helper method for switch statement test
    private String getScopeDescription(DiscountScope scope) {
        switch (scope) {
            case PRODUCT:
                return "Applies to specific products";
            case CATEGORY:
                return "Applies to product categories";
            case STORE:
                return "Applies to entire store";
            default:
                return "Unknown scope";
        }
    }
}