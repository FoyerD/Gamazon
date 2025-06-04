package Domain.Store.Discounts.Qualifiers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import Domain.Store.Item;

public class StoreQualifierTest {

    @Mock
    private Item mockItem1;
    
    @Mock 
    private Item mockItem2;
    
    private StoreQualifier storeQualifier;
    private final String TARGET_STORE_ID = "store123";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        storeQualifier = new StoreQualifier(TARGET_STORE_ID);
    }
    
    @Test
    public void testIsQualifiedWhenStoreIdMatches() {
        // Setup: Item has the matching store ID
        when(mockItem1.getStoreId()).thenReturn(TARGET_STORE_ID);
        
        // Test
        assertTrue("Item with matching store ID should be qualified", 
                  storeQualifier.isQualified(mockItem1));
    }
    
    @Test
    public void testIsQualifiedWhenStoreIdDoesNotMatch() {
        // Setup: Item has different store ID
        when(mockItem1.getStoreId()).thenReturn("differentStore456");
        
        // Test
        assertFalse("Item with different store ID should not be qualified", 
                   storeQualifier.isQualified(mockItem1));
    }
    
    @Test
    public void testIsQualifiedWithNullItem() {
        // Test: Null item should not be qualified
        assertFalse("Null item should not be qualified", 
                   storeQualifier.isQualified(null));
    }
    
    @Test
    public void testIsQualifiedWithNullStoreId() {
        // Setup: Item returns null for store ID
        when(mockItem1.getStoreId()).thenReturn(null);
        
        // Test
        assertFalse("Item with null store ID should not be qualified", 
                   storeQualifier.isQualified(mockItem1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStoreId() {
        // Test that constructor throws exception for null store ID
        new StoreQualifier(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyStoreId() {
        // Test that constructor throws exception for empty store ID
        new StoreQualifier("");
    }
    
    @Test
    public void testIsQualifiedWithDifferentItems() {
        // Setup: Both items have matching store ID
        when(mockItem1.getStoreId()).thenReturn(TARGET_STORE_ID);
        when(mockItem2.getStoreId()).thenReturn(TARGET_STORE_ID);
        
        // Test: Both items should be qualified
        assertTrue("First item should be qualified", 
                  storeQualifier.isQualified(mockItem1));
        assertTrue("Second item should be qualified", 
                  storeQualifier.isQualified(mockItem2));
    }
    
    @Test
    public void testImplementsDiscountQualifierInterface() {
        // Test: Verify that StoreQualifier implements the interface correctly
        assertTrue("StoreQualifier should implement DiscountQualifier", 
                  storeQualifier instanceof DiscountQualifier);
    }
    
    @Test
    public void testGetStoreId() {
        // Test getter method
        assertEquals("Getter should return the correct store ID", 
                    TARGET_STORE_ID, storeQualifier.getStoreId());
    }
    
    @Test
    public void testCaseSensitiveComparison() {
        // Setup: Item has store ID with different case
        when(mockItem1.getStoreId()).thenReturn("STORE123");
        
        // Test: Should be case-sensitive
        assertFalse("Store ID comparison should be case-sensitive", 
                   storeQualifier.isQualified(mockItem1));
    }
}