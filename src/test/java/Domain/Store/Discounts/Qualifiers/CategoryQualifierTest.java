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
import Domain.Store.Item;

public class CategoryQualifierTest {

    @Mock
    private Category mockCategory;
    
    @Mock
    private Category otherCategory;
    
    @Mock
    private Item mockItem;
    
    private CategoryQualifier categoryQualifier;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        categoryQualifier = new CategoryQualifier(mockCategory);
    }
    
    @Test
    public void testIsQualifiedWhenItemHasCategory() {
        // Setup: Item contains the qualifier's category
        Set<Category> itemCategories = new HashSet<>();
        itemCategories.add(mockCategory);
        itemCategories.add(otherCategory);
        
        when(mockItem.getCategories()).thenReturn(itemCategories);
        
        // Test
        assertTrue("Item with matching category should be qualified", 
                  categoryQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testIsQualifiedWhenItemDoesNotHaveCategory() {
        // Setup: Item does not contain the qualifier's category
        Set<Category> itemCategories = new HashSet<>();
        itemCategories.add(otherCategory); // Only has other category
        
        when(mockItem.getCategories()).thenReturn(itemCategories);
        
        // Test
        assertFalse("Item without matching category should not be qualified", 
                   categoryQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testIsQualifiedWithEmptyItemCategories() {
        // Setup: Item has no categories
        Set<Category> emptyCategories = new HashSet<>();
        
        when(mockItem.getCategories()).thenReturn(emptyCategories);
        
        // Test
        assertFalse("Item with no categories should not be qualified", 
                   categoryQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testIsQualifiedWithOnlyMatchingCategory() {
        // Setup: Item has only the matching category
        Set<Category> itemCategories = new HashSet<>();
        itemCategories.add(mockCategory);
        
        when(mockItem.getCategories()).thenReturn(itemCategories);
        
        // Test
        assertTrue("Item with only matching category should be qualified", 
                  categoryQualifier.isQualified(mockItem));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullCategory() {
        // Test that constructor throws exception for null category
        new CategoryQualifier(null);
    }
    
    @Test
    public void testGetCategory() {
        // Setup: Mock category name
        when(mockCategory.getName()).thenReturn("Electronics");
        
        // Test getter method
        assertEquals("Getter should return the correct category name", 
                    "Electronics", categoryQualifier.getCategory());
    }
}