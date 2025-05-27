package Domain.Store.Discounts.Qualifiers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import Domain.Store.Product;

public class ProductQualifierTest {

    @Mock
    private Product mockProduct;
    
    private ProductQualifier productQualifier;
    private final String TARGET_PRODUCT_ID = "product123";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        productQualifier = new ProductQualifier(TARGET_PRODUCT_ID);
    }
    
    @Test
    public void testIsQualifiedWhenProductIdMatches() {
        // Setup: Product has the matching ID
        when(mockProduct.getProductId()).thenReturn(TARGET_PRODUCT_ID);
        
        // Test
        assertTrue("Product with matching ID should be qualified", 
                  productQualifier.isQualified(mockProduct));
    }
    
    @Test
    public void testIsQualifiedWhenProductIdDoesNotMatch() {
        // Setup: Product has a different ID
        when(mockProduct.getProductId()).thenReturn("differentProduct456");
        
        // Test
        assertFalse("Product with different ID should not be qualified", 
                   productQualifier.isQualified(mockProduct));
    }
    
    @Test
    public void testIsQualifiedWithEmptyProductId() {
        // Setup: Product has empty string ID
        when(mockProduct.getProductId()).thenReturn("");
        
        // Test with empty string qualifier
        ProductQualifier emptyQualifier = new ProductQualifier("");
        assertTrue("Empty product ID should match empty qualifier", 
                  emptyQualifier.isQualified(mockProduct));
        
        // Test with non-empty qualifier
        assertFalse("Empty product ID should not match non-empty qualifier", 
                   productQualifier.isQualified(mockProduct));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsQualifiedWithNullProductId() {
        // Setup: Product returns null for ID
        when(mockProduct.getProductId()).thenReturn(null);
        
        // Test: Should throw IllegalArgumentException
        productQualifier.isQualified(mockProduct);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsQualifiedWithNullProduct() {
        // Test: Should throw IllegalArgumentException when product is null
        productQualifier.isQualified(null);
    }
    
    @Test
    public void testConstructorWithNullProductId() {
        // Test that constructor accepts null product ID
        ProductQualifier nullProductQualifier = new ProductQualifier(null);
        
        when(mockProduct.getProductId()).thenReturn("someProduct");
        
        // Test: Should return false since null != "someProduct"
        assertFalse("Qualifier with null ID should not match non-null product ID", 
                   nullProductQualifier.isQualified(mockProduct));
    }
    
    @Test
    public void testConstructorWithNullProductIdMatchingNullQualifier() {
        // Test that null qualifier ID can match null product ID if both are null
        // But since the implementation throws exception for null product ID, we test valid scenario
        ProductQualifier nullQualifier = new ProductQualifier(null);
        
        when(mockProduct.getProductId()).thenReturn(null);
        
        // This should throw IllegalArgumentException due to null product ID
        try {
            nullQualifier.isQualified(mockProduct);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected behavior
            assertTrue("Exception should be thrown for null product ID", true);
        }
    }
    
    @Test
    public void testCaseSensitiveComparison() {
        // Setup: Product has ID with different case
        when(mockProduct.getProductId()).thenReturn("PRODUCT123");
        
        // Test: Should be case-sensitive (assuming standard String.equals behavior)
        assertFalse("Product ID comparison should be case-sensitive", 
                   productQualifier.isQualified(mockProduct));
    }
    
    @Test
    public void testWhitespaceInProductId() {
        // Setup: Product has ID with whitespace
        when(mockProduct.getProductId()).thenReturn(" product123 ");
        
        // Test: Should not match due to whitespace
        assertFalse("Product ID with whitespace should not match trimmed qualifier", 
                   productQualifier.isQualified(mockProduct));
        
        // Test with matching whitespace
        ProductQualifier whitespaceQualifier = new ProductQualifier(" product123 ");
        assertTrue("Product ID with whitespace should match exact qualifier", 
                  whitespaceQualifier.isQualified(mockProduct));
    }
}