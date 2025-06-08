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
        when(mockItem.getProductId()).thenReturn(TARGET_PRODUCT_ID);
        
        assertTrue("Item with matching ID should be qualified", 
                  productQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testIsQualifiedWhenProductIdDoesNotMatch() {
        when(mockItem.getProductId()).thenReturn("differentProduct456");
        
        assertFalse("Item with different ID should not be qualified", 
                   productQualifier.isQualified(mockItem));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullProductId() {
        new ProductQualifier(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyProductId() {
        new ProductQualifier("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsQualifiedWithNullProductId() {
        when(mockItem.getProductId()).thenReturn(null);
        productQualifier.isQualified(mockItem);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsQualifiedWithNullItem() {
        productQualifier.isQualified(null);
    }
    
    @Test
    public void testCaseSensitiveComparison() {
        when(mockItem.getProductId()).thenReturn("PRODUCT123");
        
        assertFalse("Product ID comparison should be case-sensitive", 
                   productQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testWhitespaceInProductId() {
        when(mockItem.getProductId()).thenReturn(" product123 ");
        
        assertFalse("Product ID with whitespace should not match trimmed qualifier", 
                   productQualifier.isQualified(mockItem));
        
        ProductQualifier whitespaceQualifier = new ProductQualifier(" product123 ");
        assertTrue("Product ID with whitespace should match exact qualifier", 
                  whitespaceQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testGetProductId() {
        assertEquals("getProductId should return the constructor parameter", 
                    TARGET_PRODUCT_ID, productQualifier.getProductId());
    }
}