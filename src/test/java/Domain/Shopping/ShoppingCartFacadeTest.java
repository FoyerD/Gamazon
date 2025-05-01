package Domain.Shopping;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import Domain.Pair;
import Domain.ExternalServices.IPaymentService;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.Product;
import Domain.Store.IProductRepository;
import Domain.Store.StoreFacade;

/**
 * Test class for ShoppingCartFacade with updated implementation
 */
public class ShoppingCartFacadeTest {
    
    private ShoppingCartFacade facade;
    
    @Mock
    private IShoppingCartRepository cartRepo;
    
    @Mock
    private IShoppingBasketRepository basketRepo;
    
    @Mock
    private IReceiptRepository receiptRepo;
    
    @Mock
    private IPaymentService paymentService;
    
    @Mock
    private ItemFacade itemFacade;
    
    @Mock
    private StoreFacade storeFacade;
    
    @Mock
    private IProductRepository productRepo;
    
    @Mock
    private IShoppingCart cart;
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Item item;
    
    @Mock
    private Product product;
    
    private static final String CLIENT_ID = "client123";
    private static final String STORE_ID = "store123";
    private static final String PRODUCT_ID = "product123";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        facade = new ShoppingCartFacade(cartRepo, basketRepo, paymentService, itemFacade, storeFacade, receiptRepo, productRepo);
        
        // Setup default behavior
        when(cartRepo.get(CLIENT_ID)).thenReturn(cart);
        when(cart.getClientId()).thenReturn(CLIENT_ID);
        
        when(basketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(basket);
        when(basket.getStoreId()).thenReturn(STORE_ID);
        when(basket.getClientId()).thenReturn(CLIENT_ID);
        
        when(itemFacade.getItem(STORE_ID, PRODUCT_ID)).thenReturn(item);
        when(item.getPrice()).thenReturn(10.0);
        
        when(productRepo.get(PRODUCT_ID)).thenReturn(product);
        when(product.getProductId()).thenReturn(PRODUCT_ID);
    }
    
    @Test
    public void testAddProductToCart_ExistingCartAndBasket() {
        when(cart.hasStore(STORE_ID)).thenReturn(true);
        
        boolean result = facade.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 3);
        
        assertTrue(result);
        verify(basket).addOrder(PRODUCT_ID, 3);
        verify(basketRepo).update(new Pair<>(CLIENT_ID, STORE_ID), basket);
        // No need to update cart as it already has the store
        verify(cartRepo, never()).add(CLIENT_ID, cart);
    }
    
    @Test
    public void testAddProductToCart_ExistingCartNewStore() {
        when(cart.hasStore(STORE_ID)).thenReturn(false);
        
        boolean result = facade.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 3);
        
        assertTrue(result);
        verify(basket).addOrder(PRODUCT_ID, 3);
        verify(basketRepo).update(new Pair<>(CLIENT_ID, STORE_ID), basket);
        verify(cart).addStore(STORE_ID);
        verify(cartRepo).add(CLIENT_ID, cart);
    }
    
    @Test
    public void testAddProductToCart_NewCart() {
        when(cartRepo.get(CLIENT_ID)).thenReturn(null);
        when(basketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(null);
        
        boolean result = facade.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 3);
        
        assertTrue(result);
        // Verify a new cart and basket were created
        verify(cartRepo).add(eq(CLIENT_ID), any(IShoppingCart.class));
        verify(basketRepo).add(eq(new Pair<>(CLIENT_ID, STORE_ID)), any(ShoppingBasket.class));
    }
    
    @Test
    public void testRemoveProductFromCart_QuantityPartial() {
        when(cart.hasStore(STORE_ID)).thenReturn(true);
        when(basket.isEmpty()).thenReturn(false);
        
        boolean result = facade.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        
        assertTrue(result);
        verify(basket).removeItem(PRODUCT_ID, 2);
        verify(basketRepo).add(new Pair<>(CLIENT_ID, STORE_ID), basket);
        // Basket not empty, no need to update cart
        verify(cart, never()).removeStore(STORE_ID);
        verify(cartRepo, never()).update(CLIENT_ID, cart);
    }
    
    @Test
    public void testRemoveProductFromCart_BasketEmpty() {
        when(cart.hasStore(STORE_ID)).thenReturn(true);
        when(basket.isEmpty()).thenReturn(true);
        
        boolean result = facade.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 3);
        
        assertTrue(result);
        verify(basket).removeItem(PRODUCT_ID, 3);
        verify(basketRepo).add(new Pair<>(CLIENT_ID, STORE_ID), basket);
        // Basket empty, remove store from cart
        verify(cart).removeStore(STORE_ID);
        verify(cartRepo).update(CLIENT_ID, cart);
    }
    
    @Test(expected = RuntimeException.class)
    public void testRemoveProductFromCart_StoreNotInCart() {
        when(cart.hasStore(STORE_ID)).thenReturn(false);
        
        facade.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 3);
    }
    
    @Test
    public void testRemoveProductFromCart_CompleteRemoval() {
        when(cart.hasStore(STORE_ID)).thenReturn(true);
        when(basket.isEmpty()).thenReturn(false);
        
        boolean result = facade.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID);
        
        assertTrue(result);
        verify(basket).removeItem(PRODUCT_ID);
        verify(basketRepo).add(new Pair<>(CLIENT_ID, STORE_ID), basket);
    }
    
    @Test
    public void testMakeBid() {
        boolean result = facade.makeBid("auction123", CLIENT_ID, 100.0f);
        
        assertTrue(result);
        verify(storeFacade).addBid("auction123", CLIENT_ID, 100.0f);
    }
    
    @Test
    public void testCheckout_Success() {
        // Setup cart with one store
        Set<String> stores = new HashSet<>();
        stores.add(STORE_ID);
        when(cart.getCart()).thenReturn(stores);
        
        // Setup basket with one product
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 3);
        when(basket.getOrders()).thenReturn(orders);
        when(basket.isEmpty()).thenReturn(false);
        
        Date expiryDate = new Date();
        boolean result = facade.checkout(CLIENT_ID, "1234567890", expiryDate, "123", 1L, "John Doe", "123 Main St");
        
        assertTrue(result);
        // Verify items were decreased
        verify(itemFacade).decreaseAmount(new Pair<>(STORE_ID, PRODUCT_ID), 3);
        // Verify payment was processed (total price should be 30.0)
        verify(paymentService).processPayment(eq("John Doe"), eq("1234567890"), eq(expiryDate), eq("123"), eq(30.0), eq(1L), eq("John Doe"), eq("123 Main St"));
        // Verify baskets were cleared
        verify(basket).clear();
        verify(basketRepo).update(new Pair<>(CLIENT_ID, STORE_ID), basket);
        // Verify cart was cleared
        verify(cart).clear();
        verify(cartRepo).update(CLIENT_ID, cart);
        // Verify receipt was created
        verify(receiptRepo).savePurchase(eq(CLIENT_ID), eq(STORE_ID), any(Map.class), eq(30.0), anyString());
    }
    
    @Test(expected = RuntimeException.class)
    public void testCheckout_CartNotFound() {
        when(cartRepo.get(CLIENT_ID)).thenReturn(null);
        
        Date expiryDate = new Date();
        facade.checkout(CLIENT_ID, "1234567890", expiryDate, "123", 1L, "John Doe", "123 Main St");
    }
    
    @Test
    public void testGetTotalItems() {
        // Setup cart with one store
        Set<String> stores = new HashSet<>();
        stores.add(STORE_ID);
        when(cart.getCart()).thenReturn(stores);
        when(basket.getQuantity()).thenReturn(3);
        
        int result = facade.getTotalItems(CLIENT_ID);
        
        assertEquals(3, result);
    }
    
    @Test
    public void testIsEmpty_EmptyCart() {
        when(cart.isEmpty()).thenReturn(true);
        
        boolean result = facade.isEmpty(CLIENT_ID);
        
        assertTrue(result);
        // getTotalItems should not be called if cart is empty
        verify(basket, never()).getQuantity();
    }
    
    @Test
    public void testIsEmpty_CartWithItems() {
        when(cart.isEmpty()).thenReturn(false);
        Set<String> stores = new HashSet<>();
        stores.add(STORE_ID);
        when(cart.getCart()).thenReturn(stores);
        when(basket.getQuantity()).thenReturn(3);
        
        boolean result = facade.isEmpty(CLIENT_ID);
        
        assertFalse(result);
    }
    
    @Test
    public void testIsEmpty_CartWithoutItems() {
        when(cart.isEmpty()).thenReturn(false);
        Set<String> stores = new HashSet<>();
        stores.add(STORE_ID);
        when(cart.getCart()).thenReturn(stores);
        when(basket.getQuantity()).thenReturn(0);
        
        boolean result = facade.isEmpty(CLIENT_ID);
        
        assertTrue(result);
    }
    
    @Test
    public void testClearCart_Success() {
        Set<String> stores = new HashSet<>();
        stores.add(STORE_ID);
        when(cart.getCart()).thenReturn(stores);
        
        boolean result = facade.clearCart(CLIENT_ID);
        
        assertTrue(result);
        verify(basket).clear();
        verify(basketRepo).update(new Pair<>(CLIENT_ID, STORE_ID), basket);
        verify(cart).clear();
        verify(cartRepo).update(CLIENT_ID, cart);
    }
    
    @Test
    public void testClearCart_CartNotFound() {
        when(cartRepo.get(CLIENT_ID)).thenReturn(null);
        
        boolean result = facade.clearCart(CLIENT_ID);
        
        assertFalse(result);
    }
    
    @Test
    public void testClearBasket_Success() {
        boolean result = facade.clearBasket(CLIENT_ID, STORE_ID);
        
        assertTrue(result);
        verify(basket).clear();
        verify(basketRepo).update(new Pair<>(CLIENT_ID, STORE_ID), basket);
    }
    
    @Test
    public void testViewCart() {
        // Setup cart with one store
        Set<String> stores = new HashSet<>();
        stores.add(STORE_ID);
        when(cart.getCart()).thenReturn(stores);
        
        // Setup basket with one product
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, 3);
        when(basket.getOrders()).thenReturn(orders);
        
        // Mock the item returned by the facade
        when(itemFacade.getItem(eq(STORE_ID), eq(PRODUCT_ID))).thenReturn(item);
        
        Set<Pair<Item, Integer>> result = facade.viewCart(CLIENT_ID);
        
        assertEquals(1, result.size());
        boolean foundItem = false;
        for (Pair<Item, Integer> pair : result) {
            if (pair.getFirst() == item && pair.getSecond() == 3) {
                foundItem = true;
                break;
            }
        }
        assertTrue("Expected item not found in cart view", foundItem);
    }
    
    @Test
    public void testViewCart_EmptyCart() {
        when(cart.getCart()).thenReturn(new HashSet<>());
        
        Set<Pair<Item, Integer>> result = facade.viewCart(CLIENT_ID);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testViewCart_CartNotFound() {
        when(cartRepo.get(CLIENT_ID)).thenReturn(null);
        
        Set<Pair<Item, Integer>> result = facade.viewCart(CLIENT_ID);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testGetClientPurchaseHistory() {
        List<Receipt> expectedReceipts = new ArrayList<>();
        expectedReceipts.add(mock(Receipt.class));
        when(receiptRepo.getClientReceipts(CLIENT_ID)).thenReturn(expectedReceipts);
        
        List<Receipt> result = facade.getClientPurchaseHistory(CLIENT_ID);
        
        assertEquals(expectedReceipts, result);
        verify(receiptRepo).getClientReceipts(CLIENT_ID);
    }
    
    @Test
    public void testGetStorePurchaseHistory() {
        List<Receipt> expectedReceipts = new ArrayList<>();
        expectedReceipts.add(mock(Receipt.class));
        when(receiptRepo.getStoreReceipts(STORE_ID)).thenReturn(expectedReceipts);
        
        List<Receipt> result = facade.getStorePurchaseHistory(STORE_ID);
        
        assertEquals(expectedReceipts, result);
        verify(receiptRepo).getStoreReceipts(STORE_ID);
    }
}