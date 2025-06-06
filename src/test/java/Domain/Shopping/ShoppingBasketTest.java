package Domain.Shopping;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Map;

/**
 * Test class for ShoppingBasket
 */
public class ShoppingBasketTest {
    
    private ShoppingBasket basket;
    private static final String STORE_ID = "store123";
    private static final String CLIENT_ID = "client123";
    private static final String PRODUCT_ID = "product123";
    private static final String PRODUCT_ID2 = "product456";
    
    @Before
    public void setUp() {
        basket = new ShoppingBasket(STORE_ID, CLIENT_ID);
    }
    
    @Test
    public void testConstructor() {
        assertEquals(STORE_ID, basket.getStoreId());
        assertEquals(CLIENT_ID, basket.getClientId());
        assertTrue(basket.getOrders().isEmpty());
    }
    
    @Test
    public void testAddOrder_NewProduct() {
        // Add a new product
        basket.addOrder(PRODUCT_ID, 3);
        
        Map<String, Integer> orders = basket.getOrders();
        assertEquals(1, orders.size());
        assertEquals(Integer.valueOf(3), orders.get(PRODUCT_ID));

        assertTrue(basket.getOrders().containsKey(PRODUCT_ID));
        assertFalse(basket.getOrders().containsKey(PRODUCT_ID2));

    }
    
    @Test
    public void testAddOrder_ExistingProduct() {
        // Add a product
        basket.addOrder(PRODUCT_ID, 3);
        
        // Add more of the same product
        basket.addOrder(PRODUCT_ID, 2);
        
        Map<String, Integer> orders = basket.getOrders();
        assertEquals(1, orders.size());
        assertEquals(Integer.valueOf(5), orders.get(PRODUCT_ID));

        assertTrue(basket.getOrders().containsKey(PRODUCT_ID));
        assertFalse(basket.getOrders().containsKey(PRODUCT_ID2));

    }
    
    @Test
    public void testRemoveItem_QuantityPartial() {
        // Add a product
        basket.addOrder(PRODUCT_ID, 5);
        
        // Remove part of it
        basket.removeItem(PRODUCT_ID, 2);
        
        Map<String, Integer> orders = basket.getOrders();
        assertEquals(1, orders.size());
        assertEquals(Integer.valueOf(3), orders.get(PRODUCT_ID));

        assertTrue(basket.getOrders().containsKey(PRODUCT_ID));
        assertFalse(basket.getOrders().containsKey(PRODUCT_ID2));
    }
    
    @Test
    public void testRemoveItem_QuantityExact() {
        // Add a product
        basket.addOrder(PRODUCT_ID, 3);
        
        // Remove exact amount
        basket.removeItem(PRODUCT_ID, 3);
        
        Map<String, Integer> orders = basket.getOrders();
        assertEquals(0, orders.size());
        assertNull(orders.get(PRODUCT_ID));
        assertFalse(basket.getOrders().containsKey(PRODUCT_ID));
        assertFalse(basket.getOrders().containsKey(PRODUCT_ID2));
    }
    
    @Test
    public void testRemoveItem_QuantityExcess() {
        // Add a product
        basket.addOrder(PRODUCT_ID, 3);
        
        // Remove more than the amount
        basket.removeItem(PRODUCT_ID, 5);
        
        Map<String, Integer> orders = basket.getOrders();
        assertEquals(0, orders.size());
        assertNull(orders.get(PRODUCT_ID));

        assertFalse(basket.getOrders().containsKey(PRODUCT_ID));
        assertFalse(basket.getOrders().containsKey(PRODUCT_ID2));
    }
    
    @Test
    public void testRemoveItem_Complete() {
        // Add products
        basket.addOrder(PRODUCT_ID, 3);
        basket.addOrder(PRODUCT_ID2, 2);
        
        // Remove one completely
        basket.removeItem(PRODUCT_ID);
        
        Map<String, Integer> orders = basket.getOrders();
        assertEquals(1, orders.size());
        assertNull(orders.get(PRODUCT_ID));
        assertEquals(Integer.valueOf(2), orders.get(PRODUCT_ID2));
        assertTrue(basket.getOrders().containsKey(PRODUCT_ID2));
        assertFalse(basket.getOrders().containsKey(PRODUCT_ID));

    }
    
    @Test
    public void testIsEmpty_Empty() {
        assertTrue(basket.isEmpty());
        assertTrue(basket.getOrders().isEmpty());

    }
    
    @Test
    public void testIsEmpty_NotEmpty() {
        basket.addOrder(PRODUCT_ID, 3);
        assertFalse(basket.isEmpty());
        assertFalse(basket.getOrders().isEmpty());

    }
    
    @Test
    public void testClear() {
        // Add products
        basket.addOrder(PRODUCT_ID, 3);
        basket.addOrder(PRODUCT_ID2, 2);
        
        // Clear the basket
        basket.clear();
        
        // Check it's empty
        assertTrue(basket.isEmpty());
        assertTrue(basket.getOrders().isEmpty());
        assertFalse(basket.getOrders().containsKey(PRODUCT_ID));
        assertFalse(basket.getOrders().containsKey(PRODUCT_ID2));

    }
    
    @Test
    public void testGetProduct_Existing() {
        basket.addOrder(PRODUCT_ID, 3);
        assertEquals(3, basket.getProduct(PRODUCT_ID));
        assertEquals(0, basket.getProduct(PRODUCT_ID2));

    }
    
    @Test
    public void testGetProduct_NonExisting() {
        assertEquals(0, basket.getProduct("nonexistent"));
        assertEquals(0, basket.getProduct(PRODUCT_ID));
        assertEquals(0, basket.getProduct(PRODUCT_ID2));

    }
    
    @Test
    public void testGetQuantity_Empty() {
        assertEquals(0, basket.getQuantity());
        assertTrue(basket.getOrders().isEmpty());
    }
    
    @Test
    public void testGetQuantity_MultipleProduts() {
        basket.addOrder(PRODUCT_ID, 3);
        basket.addOrder(PRODUCT_ID2, 2);
        assertEquals(5, basket.getQuantity());
        assertFalse(basket.getOrders().isEmpty());
        
    }
}