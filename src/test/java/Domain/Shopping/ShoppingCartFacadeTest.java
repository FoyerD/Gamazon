package Domain.Shopping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import Application.utils.Response;
import Domain.Pair;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.Repos.IProductRepository;
import Domain.Repos.IReceiptRepository;
import Domain.Repos.IShoppingBasketRepository;
import Domain.Repos.IShoppingCartRepository;
import Domain.Repos.IUserRepository;
import Domain.Store.Auction;
import Domain.Store.Category;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.Product;
import Domain.Store.Store;
import Domain.Store.StoreFacade;
import Domain.Store.Discounts.DiscountFacade;
import Domain.management.PolicyFacade;

/**
 * Tests for the ShoppingCartFacade class.
 */
public class ShoppingCartFacadeTest {

    private ShoppingCartFacade facade;
    
    @Mock
    private IShoppingCartRepository mockCartRepo;
    
    @Mock
    private IShoppingBasketRepository mockBasketRepo;
    
    @Mock
    private IExternalPaymentService mockPaymentService;
    
    @Mock
    private ItemFacade mockItemFacade;
    
    @Mock
    private StoreFacade mockStoreFacade;
    
    @Mock
    private IReceiptRepository mockReceiptRepo;
    
    @Mock
    private IProductRepository mockProductRepo;
    
    @Mock
    private IShoppingCart mockCart;
    
    @Mock
    private ShoppingBasket mockBasket;

    @Mock
    private DiscountFacade mockDiscountFacade;
    @Mock
    private IUserRepository mockUserRepository;

    @Mock
    private PolicyFacade mockPolicyFacade;

    @Mock
    private IExternalSupplyService mockSupplyService;
    
    // Test constants
    private static final String CLIENT_ID = "client123";
    private static final String STORE_ID = "store123";
    private static final String PRODUCT_ID = "product123";
    private static final String AUCTION_ID = "auction123";
    private static final int QUANTITY = 2;
    private static final float BID_PRICE = 100.0f;

    @Before
    public void setUp() {
        // Initialize mocks manually since initMocks isn't working
        mockCartRepo = mock(IShoppingCartRepository.class);
        mockBasketRepo = mock(IShoppingBasketRepository.class);
        mockPaymentService = mock(IExternalPaymentService.class);
        mockItemFacade = mock(ItemFacade.class);
        mockStoreFacade = mock(StoreFacade.class);
        mockReceiptRepo = mock(IReceiptRepository.class);
        mockProductRepo = mock(IProductRepository.class);
        mockCart = mock(IShoppingCart.class);
        mockBasket = mock(ShoppingBasket.class);
        mockDiscountFacade = mock(DiscountFacade.class);
        mockPolicyFacade = mock(PolicyFacade.class);
        mockUserRepository = mock(IUserRepository.class);
        mockSupplyService = mock(IExternalSupplyService.class);
        
        // Initialize facade with correct constructor parameters
        facade = new ShoppingCartFacade(
            mockCartRepo, 
            mockBasketRepo, 
            mockPaymentService, 
            mockItemFacade, 
            mockStoreFacade, 
            mockReceiptRepo, 
            mockProductRepo,
            mockDiscountFacade,
            mockPolicyFacade,
            mockUserRepository,
            mockSupplyService,
            mockReceiptRepo
        );

        when(mockSupplyService.supplyOrder(anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(Response.success(20000));
    }

    //
    // ADD PRODUCT TO CART TESTS
    //
    
    @Test
    public void testAddProductToCart_ExistingCart() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        when(mockBasketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(mockBasket);
        when(mockCart.hasStore(STORE_ID)).thenReturn(true);
        when(mockItemFacade.getItem(STORE_ID, PRODUCT_ID)).thenReturn(mock(Item.class));
        when(mockStoreFacade.getStore(STORE_ID)).thenReturn(mock(Store.class));
        
        // Act
        boolean result = facade.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, QUANTITY);
        
        // Assert
        assertTrue("Should return true for successful addition", result);
        verify(mockBasket).addOrder(PRODUCT_ID, QUANTITY);
        verify(mockBasketRepo).update(new Pair<>(CLIENT_ID, STORE_ID), mockBasket);
        // Cart already has store, so no need to add it
        verify(mockCart, never()).addStore(STORE_ID);
    }
    
    @Test
    public void testAddProductToCart_NewCart() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(null);
        when(mockBasketRepo.get(any())).thenReturn(null);
        when(mockItemFacade.getItem(STORE_ID, PRODUCT_ID)).thenReturn(mock(Item.class));
        when(mockStoreFacade.getStore(STORE_ID)).thenReturn(mock(Store.class));

        // Act
        boolean result = facade.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, QUANTITY);
        
        // Assert
        assertTrue("Should return true for successful addition", result);
        // Updated: Test expects exactly one call to add (in getCart method)
        verify(mockCartRepo, times(1)).add(eq(CLIENT_ID), any(IShoppingCart.class));
    }
    
     @Test
    public void testAddProductToCart_NewStoreInExistingCart() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        when(mockBasketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(mockBasket);
        when(mockCart.hasStore(STORE_ID)).thenReturn(false);
        when(mockItemFacade.getItem(STORE_ID, PRODUCT_ID)).thenReturn(mock(Item.class));
        when(mockStoreFacade.getStore(STORE_ID)).thenReturn(mock(Store.class));
        
        // Act
        boolean result = facade.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, QUANTITY);
        
        // Assert
        assertTrue("Should return true for successful addition", result);
        verify(mockBasket).addOrder(PRODUCT_ID, QUANTITY);
        verify(mockBasketRepo).update(new Pair<>(CLIENT_ID, STORE_ID), mockBasket);
        verify(mockCart).addStore(STORE_ID);
        // Updated: Now we use update instead of add
        verify(mockCartRepo).update(CLIENT_ID, mockCart);
    }
    
    //
    // REMOVE PRODUCT FROM CART TESTS
    //
    
    @Test
    public void testRemoveProductFromCart_WithQuantity_Success() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        when(mockCart.hasStore(STORE_ID)).thenReturn(true);
        when(mockBasketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(mockBasket);
        when(mockBasket.isEmpty()).thenReturn(false);
        
        // Act
        boolean result = facade.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, QUANTITY);
        
        // Assert
        assertTrue("Should return true for successful removal", result);
        verify(mockBasket).removeItem(PRODUCT_ID, QUANTITY);
        verify(mockBasketRepo).add(new Pair<>(CLIENT_ID, STORE_ID), mockBasket);
        verify(mockCart, never()).removeStore(STORE_ID);
    }
    
    @Test
    public void testRemoveProductFromCart_CompleteRemoval_Success() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        when(mockCart.hasStore(STORE_ID)).thenReturn(true);
        when(mockBasketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(mockBasket);
        when(mockBasket.isEmpty()).thenReturn(false);
        
        // Act
        boolean result = facade.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID);
        
        // Assert
        assertTrue("Should return true for successful removal", result);
        verify(mockBasket).removeItem(PRODUCT_ID);
        verify(mockBasketRepo).add(new Pair<>(CLIENT_ID, STORE_ID), mockBasket);
        verify(mockCart, never()).removeStore(STORE_ID);
    }
    
    @Test
    public void testRemoveProductFromCart_LastItemInBasket() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        when(mockCart.hasStore(STORE_ID)).thenReturn(true);
        when(mockBasketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(mockBasket);
        when(mockBasket.isEmpty()).thenReturn(true);
        
        // Act
        boolean result = facade.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, QUANTITY);
        
        // Assert
        assertTrue("Should return true for successful removal", result);
        verify(mockBasket).removeItem(PRODUCT_ID, QUANTITY);
        verify(mockBasketRepo).add(new Pair<>(CLIENT_ID, STORE_ID), mockBasket);
        verify(mockCart).removeStore(STORE_ID);
        verify(mockCartRepo).update(CLIENT_ID, mockCart);
    }
    
    @Test(expected = RuntimeException.class)
    public void testRemoveProductFromCart_StoreNotInCart() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        when(mockCart.hasStore(STORE_ID)).thenReturn(false);
        
        // Act - should throw exception
        facade.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, QUANTITY);
    }
    
    
    
    @Test
    public void testCheckout_EmptyCart() {
        // Arrange
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        // Mock empty cart
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        Set<String> emptyStores = new HashSet<>();
        when(mockCart.getCart()).thenReturn(emptyStores);
        
        // ADD THIS LINE - Mock supply service to return successful response
        when(mockSupplyService.supplyOrder(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Response.success(20000));
        
        // Act
        boolean result = facade.checkout(CLIENT_ID, "123456789", cardNumber, expiryDate, cvv, 
                                        clientName, deliveryAddress, "City", "Country", "12345");
        
        // Assert
        assertTrue("Should return true even for empty cart", result);
        verify(mockPaymentService, never()).processPayment(
            anyString(), anyString(), any(Date.class), anyString(), 
            anyString(), anyDouble()
        );
        verify(mockCart).clear();
        verify(mockBasketRepo, never()).get(any(Pair.class));   
        verify(mockBasketRepo, never()).update(any(Pair.class), any(ShoppingBasket.class));
        verify(mockReceiptRepo, never()).savePurchase(
            anyString(), anyString(), anyMap(), anyDouble(), anyString(), anyString()
        );
    }
    
    
    // Handle checkout with no cart more elegantly - a new cart will be created
    @Test
    public void testCheckout_CartNotFound() {
        // Arrange
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        // Mock null cart, which will cause a new empty cart to be created
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(null);
        Set<String> emptyStores = new HashSet<>();
        when(mockCart.getCart()).thenReturn(emptyStores);
        
        // ADD THIS LINE - Mock supply service to return successful response
        when(mockSupplyService.supplyOrder(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Response.success(20000));
        
        // Act
        boolean result = facade.checkout(CLIENT_ID, "123456789", cardNumber, expiryDate, cvv, 
                                        clientName, deliveryAddress, "City", "Country", "12345");
        
        // Assert
        assertTrue("Should return true even for null cart (new one is created)", result);
    }

    
    @Test(expected = RuntimeException.class)
    public void testCheckout_PaymentFailure() {
        // Arrange
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        // Mock cart and basket with items
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        Set<String> stores = new HashSet<>();
        stores.add(STORE_ID);
        when(mockCart.getCart()).thenReturn(stores);
        
        when(mockBasketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(mockBasket);
        when(mockBasket.isEmpty()).thenReturn(false);
        
        // Mock basket contents
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, QUANTITY);
        when(mockBasket.getOrders()).thenReturn(orders);
        
        // Real item and product info
        LinkedHashSet<Category> categories = new LinkedHashSet<>();
        Item mockItem = new Item(STORE_ID, PRODUCT_ID, 10.0, 20, "description", null, categories);
        when(mockItemFacade.getItem(STORE_ID, PRODUCT_ID)).thenReturn(mockItem);
        
        Product mockProduct = new Product(PRODUCT_ID, "a", new LinkedHashSet<>());
        when(mockProductRepo.get(PRODUCT_ID)).thenReturn(mockProduct);
        
        // Mock payment failure
        when(mockPaymentService.processPayment(
            anyString(), anyString(), any(Date.class), anyString(), 
            anyString(), anyDouble()
        )).thenThrow(new RuntimeException("Payment failed"));
        
        // Mock supply service to return successful response (payment fails first)
        // TODO! Amit
        // when(mockSupplyService.supply(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(Response.success(20000));
        
        // Act - should throw exception
        facade.checkout(CLIENT_ID, "123456789", cardNumber, expiryDate, cvv, 
                      clientName, deliveryAddress, "City", "Country", "12345");
    }
    
    //
    // CLEAR CART/BASKET TESTS
    //
    
    @Test
    public void testClearCart_Success() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        Set<String> stores = new HashSet<>();
        stores.add(STORE_ID);
        when(mockCart.getCart()).thenReturn(stores);
        when(mockBasketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(mockBasket);
        
        // Act
        boolean result = facade.clearCart(CLIENT_ID);
        
        // Assert
        assertTrue("Should return true for successful cart clearing", result);
        verify(mockBasket).clear();
        verify(mockBasketRepo).update(new Pair<>(CLIENT_ID, STORE_ID), mockBasket);
        verify(mockCart).clear();
        verify(mockCartRepo).update(CLIENT_ID, mockCart);
    }
    
    @Test
    public void testClearCart_CartNotFound() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(null);
        // A new cart will be created in getCart
        
        // Act
        boolean result = facade.clearCart(CLIENT_ID);
        
        // Assert
        // Since an empty cart is created, then cleared, should still return true
        assertTrue("Should return true when clearing non-existent cart", result);
    }
    
    @Test
    public void testClearBasket_Success() {
        // Arrange
        when(mockBasketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(mockBasket);
        
        // Act
        boolean result = facade.clearBasket(CLIENT_ID, STORE_ID);
        
        // Assert
        assertTrue("Should return true for successful basket clearing", result);
        verify(mockBasket).clear();
        verify(mockBasketRepo).update(new Pair<>(CLIENT_ID, STORE_ID), mockBasket);
    }
    
    @Test
    public void testClearBasket_BasketNotFound() {
        // Arrange
        when(mockBasketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(null);
        // A new basket will be created in getBasket
        
        // Act
        boolean result = facade.clearBasket(CLIENT_ID, STORE_ID);
        
        // Assert
        // Since an empty basket is created, then cleared, should still return true
        assertTrue("Should return true when clearing non-existent basket", result);
    }
    
    //
    // VIEW CART TEST
    //
    
    @Test
    public void testViewCart_Success() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        Set<String> stores = new HashSet<>();
        stores.add(STORE_ID);
        when(mockCart.getCart()).thenReturn(stores);
        
        when(mockBasketRepo.get(new Pair<>(CLIENT_ID, STORE_ID))).thenReturn(mockBasket);
        Map<String, Integer> orders = new HashMap<>();
        orders.put(PRODUCT_ID, QUANTITY);
        when(mockBasket.getOrders()).thenReturn(orders);
        
        Item mockItem = mock(Item.class);
        when(mockItemFacade.getItem(STORE_ID, PRODUCT_ID)).thenReturn(mockItem);
        
        // Act
        Set<Pair<Item, Integer>> result = facade.viewCart(CLIENT_ID);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Result should contain one item", 1, result.size());
        
        // Verify the item in the result
        Pair<Item, Integer> itemPair = result.iterator().next();
        assertEquals("Should have the correct item", mockItem, itemPair.getFirst());
        assertEquals("Should have the correct quantity", QUANTITY, itemPair.getSecond().intValue());
    }
    
    @Test
    public void testViewCart_EmptyCart() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(mockCart);
        Set<String> emptyStores = new HashSet<>();
        when(mockCart.getCart()).thenReturn(emptyStores);
        
        // Act
        Set<Pair<Item, Integer>> result = facade.viewCart(CLIENT_ID);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }
    
    @Test
    public void testViewCart_CartNotFound() {
        // Arrange
        when(mockCartRepo.get(CLIENT_ID)).thenReturn(null);
        // getCart will create a new empty cart
        
        // Act
        Set<Pair<Item, Integer>> result = facade.viewCart(CLIENT_ID);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }
    
    //
    // MAKE BID TEST
    //
    
    @Test
    public void testMakeBid_Success() {
        // Arrange
        Auction mockAuction = mock(Auction.class);
        when(mockStoreFacade.addBid(eq(AUCTION_ID), eq(CLIENT_ID), eq(BID_PRICE), anyString(), any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockAuction);

        // Act
        boolean result = facade.makeBid(AUCTION_ID, CLIENT_ID, BID_PRICE,
            "1234567890123456", new Date(), "123", 12345L, "John Doe", "123 Main St", "City", "Country", "12345");

        // Assert
        assertTrue("Should return true for successful bid", result);
        verify(mockStoreFacade).addBid(eq(AUCTION_ID), eq(CLIENT_ID), eq(BID_PRICE), anyString(), any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
    
    //
    // PURCHASE HISTORY TESTS
    //
    
    @Test
    public void testGetClientPurchaseHistory() {
        // Arrange
        List<Receipt> receipts = new ArrayList<>();
        receipts.add(mock(Receipt.class));
        when(mockReceiptRepo.getClientReceipts(CLIENT_ID)).thenReturn(receipts);
        
        // Act
        List<Receipt> result = facade.getClientPurchaseHistory(CLIENT_ID);
        
        // Assert
        assertEquals("Should return correct receipt list", receipts, result);
        verify(mockReceiptRepo).getClientReceipts(CLIENT_ID);
    }
    
    @Test
    public void testGetStorePurchaseHistory() {
        // Arrange
        List<Receipt> receipts = new ArrayList<>();
        receipts.add(mock(Receipt.class));
        when(mockReceiptRepo.getStoreReceipts(STORE_ID)).thenReturn(receipts);
        
        // Act
        List<Receipt> result = facade.getStorePurchaseHistory(STORE_ID);
        
        // Assert
        assertEquals("Should return correct receipt list", receipts, result);
        verify(mockReceiptRepo).getStoreReceipts(STORE_ID);
    }
}