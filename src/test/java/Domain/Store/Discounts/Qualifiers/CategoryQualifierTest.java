package Domain.Store.Discounts.Qualifiers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
        Set<Category> itemCategories = new HashSet<>(Arrays.asList(mockCategory, otherCategory));
        when(mockItem.getCategories()).thenReturn(itemCategories);
        
        assertTrue("Item with matching category should be qualified", 
                  categoryQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testIsQualifiedWhenItemDoesNotHaveCategory() {
        Set<Category> itemCategories = new HashSet<>(Arrays.asList(otherCategory));
        when(mockItem.getCategories()).thenReturn(itemCategories);
        
        assertFalse("Item without matching category should not be qualified", 
                   categoryQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testIsQualifiedWithEmptyItemCategories() {
        Set<Category> emptyCategories = new HashSet<>();
        when(mockItem.getCategories()).thenReturn(emptyCategories);
        
        assertFalse("Item with no categories should not be qualified", 
                   categoryQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testIsQualifiedWithOnlyMatchingCategory() {
        Set<Category> itemCategories = new HashSet<>(Arrays.asList(mockCategory));
        when(mockItem.getCategories()).thenReturn(itemCategories);
        
        assertTrue("Item with only matching category should be qualified", 
                  categoryQualifier.isQualified(mockItem));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullCategory() {
        new CategoryQualifier(null);
    }
    
    @Test
    public void testGetCategory() {
        String categoryName = "Electronics";
        when(mockCategory.getName()).thenReturn(categoryName);
        
        assertEquals("getCategory should return category name", 
                    categoryName, categoryQualifier.getCategory());
    }
}