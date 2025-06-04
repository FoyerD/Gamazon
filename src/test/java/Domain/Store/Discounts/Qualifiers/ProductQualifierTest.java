package Domain.Store.Discounts.Qualifiers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import Domain.Store.Item;

public class ProductQualifierTest {

    @Mock
    private Item mockItem;
    
    private ProductQualifier productQualifier;
    private final String TARGET_PRODUCT_ID = "product123";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        productQualifier = new ProductQualifier(TARGET_PRODUCT_ID);
    }
    
    @Test
    public void testIsQualifiedWhenProductIdMatches() {
        // Setup: Item has the matching ID
        when(mockItem.getProductId()).thenReturn(TARGET_PRODUCT_ID);
        
        // Test
        assertTrue("Item with matching ID should be qualified", 
                  productQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testIsQualifiedWhenProductIdDoesNotMatch() {
        // Setup: Item has a different ID
        when(mockItem.getProductId()).thenReturn("differentProduct456");
        
        // Test
        assertFalse("Item with different ID should not be qualified", 
                   productQualifier.isQualified(mockItem));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullProductId() {
        // Test that constructor throws exception for null product ID
        new ProductQualifier(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyProductId() {
        // Test that constructor throws exception for empty product ID
        new ProductQualifier("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsQualifiedWithNullProductId() {
        // Setup: Item returns null for ID
        when(mockItem.getProductId()).thenReturn(null);
        
        // Test: Should throw IllegalArgumentException
        productQualifier.isQualified(mockItem);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsQualifiedWithNullItem() {
        // Test: Should throw IllegalArgumentException when item is null
        productQualifier.isQualified(null);
    }
    
    @Test
    public void testCaseSensitiveComparison() {
        // Setup: Item has ID with different case
        when(mockItem.getProductId()).thenReturn("PRODUCT123");
        
        // Test: Should be case-sensitive (assuming standard String.equals behavior)
        assertFalse("Product ID comparison should be case-sensitive", 
                   productQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testWhitespaceInProductId() {
        // Setup: Item has ID with whitespace
        when(mockItem.getProductId()).thenReturn(" product123 ");
        
        // Test: Should not match due to whitespace
        assertFalse("Product ID with whitespace should not match trimmed qualifier", 
                   productQualifier.isQualified(mockItem));
        
        // Test with matching whitespace
        ProductQualifier whitespaceQualifier = new ProductQualifier(" product123 ");
        assertTrue("Product ID with whitespace should match exact qualifier", 
                  whitespaceQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testGetProductId() {
        // Test getter method
        assertEquals("Getter should return the correct product ID", 
                    TARGET_PRODUCT_ID, productQualifier.getProductId());
    }
}