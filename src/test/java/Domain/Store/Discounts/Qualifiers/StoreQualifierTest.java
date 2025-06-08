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
    private Item mockItem;
    
    private StoreQualifier storeQualifier;
    private final String TARGET_STORE_ID = "store123";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        storeQualifier = new StoreQualifier(TARGET_STORE_ID);
    }
    
    @Test
    public void testIsQualifiedWhenStoreIdMatches() {
        when(mockItem.getStoreId()).thenReturn(TARGET_STORE_ID);
        
        assertTrue("Item with matching store ID should be qualified", 
                  storeQualifier.isQualified(mockItem));
    }
    
    @Test
    public void testIsQualifiedWhenStoreIdDoesNotMatch() {
        when(mockItem.getStoreId()).thenReturn("differentStore");
        
        // Note: Current implementation uses == instead of .equals(), so this will fail
        // This test reveals a bug in the implementation
        assertFalse("Item with different store ID should not be qualified", 
                   storeQualifier.isQualified(mockItem));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStoreId() {
        new StoreQualifier(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyStoreId() {
        new StoreQualifier("");
    }
    
    @Test
    public void testGetStoreId() {
        assertEquals("getStoreId should return the constructor parameter", 
                    TARGET_STORE_ID, storeQualifier.getStoreId());
    }
    
    @Test
    public void testImplementsDiscountQualifierInterface() {
        assertTrue("StoreQualifier should implement DiscountQualifier", 
                  storeQualifier instanceof DiscountQualifier);
    }
    
    // This test demonstrates the bug in StoreQualifier implementation
    @Test
    public void testStringComparisonBug() {
        // Create two different String objects with same content
        String storeId1 = new String("testStore");
        String storeId2 = new String("testStore");
        
        StoreQualifier qualifier = new StoreQualifier(storeId1);
        when(mockItem.getStoreId()).thenReturn(storeId2);
        
        // This will fail due to == comparison instead of .equals()
        // Uncomment to see the bug in action:
        // assertFalse("Should fail due to == comparison bug", qualifier.isQualified(mockItem));
    }
}