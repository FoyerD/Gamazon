package Domain.Store.Discounts;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Product;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class SimpleDiscountTest {
    
    @Mock
    private ItemFacade itemFacade;
    
    @Mock
    private DiscountQualifier qualifier;
    
    @Mock
    private Condition condition;
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Product product;
    
    @Mock
    private Item item;
    
    private SimpleDiscount simpleDiscount;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        simpleDiscount = new SimpleDiscount(itemFacade, 0.2f, qualifier, condition);
    }
    
    @Test
    public void testConstructorWithCondition() {
        assertNotNull(simpleDiscount);
        assertEquals(condition, simpleDiscount.getCondition());
        assertNotNull(simpleDiscount.getId());
        assertEquals(0.2, simpleDiscount.getDiscountPercentage(), 0.001);
        assertEquals(qualifier, simpleDiscount.getQualifier());
    }
    
    @Test
    public void testConstructorWithoutCondition() {
        SimpleDiscount discount = new SimpleDiscount(itemFacade, 0.15f, qualifier);
        assertNotNull(discount.getCondition());
        assertEquals(0.15, discount.getDiscountPercentage(), 0.001);
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        SimpleDiscount discount = new SimpleDiscount(existingId, itemFacade, 0.25f, qualifier, condition);
        
        assertEquals(existingId, discount.getId());
        assertEquals(0.25, discount.getDiscountPercentage(), 0.001);
    }
    
    @Test
    public void testCalculatePriceQualifiedWithCondition() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(itemFacade.getProduct("product1")).thenReturn(product);
        when(item.getPrice()).thenReturn(100.0);
        when(qualifier.isQualified(product)).thenReturn(true);
        when(condition.isSatisfied(basket)).thenReturn(true);
        
        // Execute
        Map<String, PriceBreakDown> result = simpleDiscount.calculatePrice(basket);
        
        // Verify
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.2, breakdown.getDiscount(), 0.001);
        assertEquals(80.0, breakdown.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testCalculatePriceNotQualified() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(itemFacade.getProduct("product1")).thenReturn(product);
        when(item.getPrice()).thenReturn(100.0);
        when(qualifier.isQualified(product)).thenReturn(false);
        
        // Execute
        Map<String, PriceBreakDown> result = simpleDiscount.calculatePrice(basket);
        
        // Verify
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
        assertEquals(100.0, breakdown.getFinalPrice(), 0.001);
    }
    
    @Test
    public void testCalculatePriceConditionNotSatisfied() {
        // Setup
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(itemFacade.getProduct("product1")).thenReturn(product);
        when(item.getPrice()).thenReturn(100.0);
        when(qualifier.isQualified(product)).thenReturn(true);
        when(condition.isSatisfied(basket)).thenReturn(false);
        
        // Execute
        Map<String, PriceBreakDown> result = simpleDiscount.calculatePrice(basket);
        
        // Verify
        assertEquals(1, result.size());
        PriceBreakDown breakdown = result.get("product1");
        assertEquals(100.0, breakdown.getOriginalPrice(), 0.001);
        assertEquals(0.0, breakdown.getDiscount(), 0.001);
    }
    
    @Test
    public void testIsQualified() {
        when(itemFacade.getProduct("product1")).thenReturn(product);
        when(qualifier.isQualified(product)).thenReturn(true);
        
        assertTrue(simpleDiscount.isQualified("product1"));
    }
    
    @Test
    public void testIsNotQualified() {
        when(itemFacade.getProduct("product1")).thenReturn(product);
        when(qualifier.isQualified(product)).thenReturn(false);
        
        assertFalse(simpleDiscount.isQualified("product1"));
    }
    
    @Test
    public void testMultipleProductsInBasket() {
        Map<String, Integer> orders = new HashMap<>();
        orders.put("product1", 1);
        orders.put("product2", 2);
        
        when(basket.getOrders()).thenReturn(orders);
        when(basket.getStoreId()).thenReturn("store1");
        
        // Setup for product1 (qualified)
        when(itemFacade.getItem("store1", "product1")).thenReturn(item);
        when(itemFacade.getProduct("product1")).thenReturn(product);
        when(item.getPrice()).thenReturn(50.0);
        when(qualifier.isQualified(product)).thenReturn(true);
        
        // Setup for product2 (not qualified) 
        Item item2 = mock(Item.class);
        Product product2 = mock(Product.class);
        when(itemFacade.getItem("store1", "product2")).thenReturn(item2);
        when(itemFacade.getProduct("product2")).thenReturn(product2);
        when(item2.getPrice()).thenReturn(75.0);
        when(qualifier.isQualified(product2)).thenReturn(false);
        
        when(condition.isSatisfied(basket)).thenReturn(true);
        
        Map<String, PriceBreakDown> result = simpleDiscount.calculatePrice(basket);
        
        assertEquals(2, result.size());
        
        // Product1 should get discount
        PriceBreakDown breakdown1 = result.get("product1");
        assertEquals(0.2, breakdown1.getDiscount(), 0.001);
        
        // Product2 should not get discount
        PriceBreakDown breakdown2 = result.get("product2");
        assertEquals(0.0, breakdown2.getDiscount(), 0.001);
    }
    
    @Test
    public void testHasUniqueId() {
        SimpleDiscount another = new SimpleDiscount(itemFacade, 0.2f, qualifier, condition);
        assertNotEquals(simpleDiscount.getId(), another.getId());
    }
}