package Domain.Shopping;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import Application.DTOs.OrderDTO;
import Application.ShoppingService;
import Application.utils.Response;
import Domain.Pair;
import Domain.TokenService;
import Domain.ExternalServices.IPaymentService;
import Domain.Store.Auction;
import Domain.Store.Category;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.StoreFacade;
import Domain.Store.IProductRepository;

/**
 * Test class for ShoppingService
 * 
 * Tests the main use cases:
 * - 2.3: Saving Products to the Shopping Cart
 * - 2.4: Viewing the Shopping Cart
 * - 2.5: Immediate Purchase of the Shopping Cart (All-or-Nothing)
 * - 3.9: Submitting a Purchase Bid for a Single Product
 */
public class ShoppingServiceTest {

    private ShoppingService shoppingService;
    
    @Mock
    private TokenService mockTokenService;
    
    @Mock
    private IShoppingCartRepository mockCartRepository;
    
    @Mock
    private IShoppingBasketRepository mockBasketRepository;
    
    @Mock
    private ItemFacade mockItemFacade;
    
    @Mock
    private StoreFacade mockStoreFacade;
    
    @Mock
    private IReceiptRepository mockReceiptRepository;
    
    @Mock
    private IProductRepository mockProductRepository;
    
    @Mock
    private IShoppingCartFacade mockCartFacade;
    
    // Common test constants
    private static final String CLIENT_ID = "client123";
    private static final String STORE_ID = "store123";
    private static final String PRODUCT_ID = "product123";
    private static final String AUCTION_ID = "auction123";

    // Bid-specific constants
    private static final float VALID_BID_PRICE = 100.0f;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Create real shopping service with mocked dependencies
        shoppingService = new ShoppingService(
            mockCartRepository, 
            mockBasketRepository, 
            mockItemFacade, 
            mockStoreFacade,
            mockReceiptRepository,
            mockProductRepository,
            mockTokenService
        );
        
        // Inject mocked cart facade using reflection to avoid the real implementation being used
        try {
            java.lang.reflect.Field cartFacadeField = ShoppingService.class.getDeclaredField("cartFacade");
            cartFacadeField.setAccessible(true);
            cartFacadeField.set(shoppingService, mockCartFacade);
        } catch (Exception e) {
            fail("Failed to inject mocked cart facade: " + e.getMessage());
        }
    }

    //
    // USE CASE 2.3: SAVING PRODUCTS TO THE SHOPPING CART
    //

    @Test
    public void testAddProductToCart_Success() {
        // Arrange
        int quantity = 2;
        when(mockCartFacade.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity)).thenReturn(true);
        
        // Act
        Response<Boolean> response = shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        verify(mockCartFacade).addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity);
    }
    
    @Test
    public void testAddProductToCart_ServiceFailure() {
        // Arrange
        int quantity = 2;
        String errorMessage = "Product is out of stock";
        when(mockCartFacade.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity))
            .thenThrow(new RuntimeException(errorMessage));
        
        // Act
        Response<Boolean> response = shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should match exception", errorMessage, response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
    }

    //
    // USE CASE 2.4: VIEWING THE SHOPPING CART
    //

    @Test
    public void testViewCart_Success() {
        // Arrange
        Set<Pair<Item, Integer>> cartContents = new HashSet<>();
        Item mockItem = mock(Item.class);
        when(mockItem.getStoreId()).thenReturn(STORE_ID);
        when(mockItem.getProductId()).thenReturn(PRODUCT_ID);
        when(mockItem.getProductName()).thenReturn("Test Product");
        when(mockItem.getDescription()).thenReturn("Test Description");
        when(mockItem.getCategories()).thenReturn(new HashSet<>());
        
        cartContents.add(new Pair<>(mockItem, 2));
        
        // Mock the behavior to return cart contents
        when(mockCartFacade.viewCart(CLIENT_ID)).thenReturn(cartContents);
        
        // Act
        Response<Set<OrderDTO>> response = shoppingService.viewCart(CLIENT_ID);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertNotNull("Response value should not be null", response.getValue());
        assertEquals("Cart should have one item", 1, response.getValue().size());
        
        // Verify the item details in OrderDTO
        OrderDTO orderDTO = response.getValue().iterator().next();
        assertEquals(STORE_ID, orderDTO.getStoreId());
        assertEquals(PRODUCT_ID, orderDTO.getProductId());
        assertEquals(2, orderDTO.getQuantity());
        
        verify(mockCartFacade).viewCart(CLIENT_ID);
    }

    @Test
    public void testViewCart_EmptyCart() {
        // Arrange
        Set<Pair<Item, Integer>> emptyCart = new HashSet<>();
        when(mockCartFacade.viewCart(CLIENT_ID)).thenReturn(emptyCart);
        
        // Act
        Response<Set<OrderDTO>> response = shoppingService.viewCart(CLIENT_ID);
        
        // Assert
        assertFalse("Should not have error even for empty cart", response.errorOccurred());
        assertTrue("Cart should be empty", response.getValue().isEmpty());
        verify(mockCartFacade).viewCart(CLIENT_ID);
    }
    
    @Test
    public void testViewCart_ServiceFailure() {
        // Arrange
        String errorMessage = "Failed to access cart data";
        when(mockCartFacade.viewCart(CLIENT_ID)).thenThrow(new RuntimeException(errorMessage));
        
        // Act
        Response<Set<OrderDTO>> response = shoppingService.viewCart(CLIENT_ID);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should match exception", errorMessage, response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
    }

    //
    // USE CASE 2.5: IMMEDIATE PURCHASE (CHECKOUT)
    //

    @Test
    public void testCheckout_Success() {
        // Arrange
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        when(mockCartFacade.checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress))
            .thenReturn(true);
        
        // Act
        Response<Boolean> response = shoppingService.checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        verify(mockCartFacade).checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
    }

    @Test
    public void testCheckout_PaymentError() {
        // Arrange
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        String errorMessage = "Payment service rejected the transaction";
        
        when(mockCartFacade.checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress))
            .thenThrow(new RuntimeException(errorMessage));
        
        // Act
        Response<Boolean> response = shoppingService.checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should match payment error", errorMessage, response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockCartFacade).checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
    }
    
    //
    // CART MANAGEMENT - REMOVE PRODUCT
    //
    
    @Test
    public void testRemoveProductFromCart_WithQuantity_Success() {
        // Arrange
        int quantity = 2;
        when(mockCartFacade.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity)).thenReturn(true);
        
        // Act
        Response<Boolean> response = shoppingService.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        verify(mockCartFacade).removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity);
    }
    
    @Test
    public void testRemoveProductFromCart_CompleteRemoval_Success() {
        // Arrange
        when(mockCartFacade.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID)).thenReturn(true);
        
        // Act
        Response<Boolean> response = shoppingService.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        verify(mockCartFacade).removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID);
    }
    
    //
    // CART MANAGEMENT - CLEAR OPERATIONS
    //
    
    @Test
    public void testClearCart_Success() {
        // Arrange
        when(mockCartFacade.clearCart(CLIENT_ID)).thenReturn(true);
        
        // Act
        Response<Boolean> response = shoppingService.clearCart(CLIENT_ID);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        verify(mockCartFacade).clearCart(CLIENT_ID);
    }
    
    @Test
    public void testClearBasket_Success() {
        // Arrange
        when(mockCartFacade.clearBasket(CLIENT_ID, STORE_ID)).thenReturn(true);
        
        // Act
        Response<Boolean> response = shoppingService.clearBasket(CLIENT_ID, STORE_ID);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        verify(mockCartFacade).clearBasket(CLIENT_ID, STORE_ID);
    }
    
    //
    // USE CASE 3.9: SUBMITTING A PURCHASE BID
    //
    
    @Test
    public void testMakeBid_Success() {
        // Arrange
        when(mockCartFacade.makeBid(AUCTION_ID, CLIENT_ID, VALID_BID_PRICE)).thenReturn(true);
        
        // Act
        Response<Boolean> response = shoppingService.makeBid(AUCTION_ID, CLIENT_ID, VALID_BID_PRICE);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        verify(mockCartFacade).makeBid(AUCTION_ID, CLIENT_ID, VALID_BID_PRICE);
    }
    
    @Test
    public void testMakeBid_BidRejected() {
        // Arrange
        String errorMessage = "Bid price too low";
        when(mockCartFacade.makeBid(AUCTION_ID, CLIENT_ID, VALID_BID_PRICE))
            .thenThrow(new RuntimeException(errorMessage));
        
        // Act
        Response<Boolean> response = shoppingService.makeBid(AUCTION_ID, CLIENT_ID, VALID_BID_PRICE);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should match bid rejection reason", errorMessage, response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockCartFacade).makeBid(AUCTION_ID, CLIENT_ID, VALID_BID_PRICE);
    }
}