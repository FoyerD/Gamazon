package Domain.Store.Discounts.Qualifiers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import Domain.Store.Category;
import Domain.Store.Product;

public class CategoryQualifierTest {

    @Mock
    private Category mockCategory;
    
    @Mock
    private Category otherCategory;
    
    @Mock
    private Product mockProduct;
    
    private CategoryQualifier categoryQualifier;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        categoryQualifier = new CategoryQualifier(mockCategory);
    }
    
    @Test
    public void testIsQualifiedWhenProductHasCategory() {
        // Setup: Product contains the qualifier's category
        Set<Category> productCategories = new HashSet<>();
        productCategories.add(mockCategory);
        productCategories.add(otherCategory);
        
        when(mockProduct.getCategories()).thenReturn(productCategories);
        
        // Test
        assertTrue("Product with matching category should be qualified", 
                  categoryQualifier.isQualified(mockProduct));
    }
    
    @Test
    public void testIsQualifiedWhenProductDoesNotHaveCategory() {
        // Setup: Product does not contain the qualifier's category
        Set<Category> productCategories = new HashSet<>();
        productCategories.add(otherCategory); // Only has other category
        
        when(mockProduct.getCategories()).thenReturn(productCategories);
        
        // Test
        assertFalse("Product without matching category should not be qualified", 
                   categoryQualifier.isQualified(mockProduct));
    }
    
    @Test
    public void testIsQualifiedWithEmptyProductCategories() {
        // Setup: Product has no categories
        Set<Category> emptyCategories = new HashSet<>();
        
        when(mockProduct.getCategories()).thenReturn(emptyCategories);
        
        // Test
        assertFalse("Product with no categories should not be qualified", 
                   categoryQualifier.isQualified(mockProduct));
    }
    
    @Test
    public void testIsQualifiedWithOnlyMatchingCategory() {
        // Setup: Product has only the matching category
        Set<Category> productCategories = new HashSet<>();
        productCategories.add(mockCategory);
        
        when(mockProduct.getCategories()).thenReturn(productCategories);
        
        // Test
        assertTrue("Product with only matching category should be qualified", 
                  categoryQualifier.isQualified(mockProduct));
    }
    
    @Test(expected = NullPointerException.class)
    public void testIsQualifiedWithNullProductCategories() {
        // Setup: Product returns null for categories
        when(mockProduct.getCategories()).thenReturn(null);
        
        // Test: Should throw NullPointerException
        categoryQualifier.isQualified(mockProduct);
    }
    
    @Test
    public void testConstructorWithNullCategory() {
        // Test that constructor accepts null category
        CategoryQualifier nullCategoryQualifier = new CategoryQualifier(null);
        
        Set<Category> productCategories = new HashSet<>();
        productCategories.add(mockCategory);
        
        when(mockProduct.getCategories()).thenReturn(productCategories);
        
        // Test: Should return false since null is not in the set
        assertFalse("Qualifier with null category should not match any product", 
                   nullCategoryQualifier.isQualified(mockProduct));
    }
}