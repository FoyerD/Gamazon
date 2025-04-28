//package Application;
package Domain.Shopping;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import Application.Response;
import Application.ShoppingService;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Store.ItemFacade;
import Domain.Store.StoreFacade;
import Domain.Store.Auction;
import Domain.TokenService;
import Domain.Pair;

/**
 * Test class for ShoppingService and bidding functionality
 * 
 * Tests the main use cases:
 * - 2.3: Saving Products to the Shopping Cart
 * - 2.4: Viewing the Shopping Cart
 * - 2.5: Immediate Purchase of the Shopping Cart (All-or-Nothing)
 * - 3.9: Submitting a Purchase Bid for a Single Product
 */
public class ShoppingServiceTest {

    // Shopping Service Tests
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
    private IShoppingCartFacade mockCartFacade;
    
    // Bid Service Tests
    private BidService bidService;
    
    @Mock
    private StoreFacade mockStoreFacade;
    
    // Common test constants
    private static final String VALID_SESSION_TOKEN = "valid-token";
    private static final String INVALID_SESSION_TOKEN = "invalid-token";
    private static final String CLIENT_ID = "client123";
    private static final String USER_ID = "client123"; // Same as CLIENT_ID for simplicity
    private static final String STORE_ID = "store123";
    private static final String PRODUCT_ID = "product123";
    private static final String AUCTION_ID = "auction123";

    // Bid-specific constants
    private static final float VALID_BID_PRICE = 100.0f;
    private static final float INVALID_BID_PRICE = 10.0f;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Set up token service mock
        when(mockTokenService.validateToken(VALID_SESSION_TOKEN)).thenReturn(true);
        when(mockTokenService.validateToken(INVALID_SESSION_TOKEN)).thenReturn(false);
        when(mockTokenService.extractId(VALID_SESSION_TOKEN)).thenReturn(CLIENT_ID);
        
        // Create real shopping service with mocked dependencies
        shoppingService = new ShoppingService(mockTokenService, mockCartRepository, mockBasketRepository, mockItemFacade);
        
        // Inject mocked cart facade using reflection
        try {
            java.lang.reflect.Field cartFacadeField = ShoppingService.class.getDeclaredField("cartFacade");
            cartFacadeField.setAccessible(true);
            cartFacadeField.set(shoppingService, mockCartFacade);
        } catch (Exception e) {
            fail("Failed to inject mocked cart facade: " + e.getMessage());
        }
        
        // Create bid service
        bidService = new BidService(mockTokenService, mockStoreFacade);
    }

    //
    // USE CASE 2.3: SAVING PRODUCTS TO THE SHOPPING CART
    //

    @Test
    public void testAddProductToCart_Success() {
        // Arrange
        int quantity = 2;
        doNothing().when(mockCartFacade).addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity);
        
        // Act
        Response<Boolean> response = shoppingService.addProductToCart(VALID_SESSION_TOKEN, STORE_ID, PRODUCT_ID, quantity);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        verify(mockCartFacade, times(1)).addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity);
    }

    @Test
    public void testAddProductToCart_InvalidToken() {
        // Arrange
        int quantity = 2;
        
        // Act
        Response<Boolean> response = shoppingService.addProductToCart(INVALID_SESSION_TOKEN, STORE_ID, PRODUCT_ID, quantity);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should mention invalid token", "Invalid session token", response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockCartFacade, never()).addProductToCart(anyString(), anyString(), anyString(), anyInt());
    }
    
    @Test
    public void testAddProductToCart_ProductUnavailable() {
        // Arrange
        int quantity = 2;
        String errorMessage = "Product is out of stock";
        doThrow(new RuntimeException(errorMessage)).when(mockCartFacade).addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity);
        
        // Act
        Response<Boolean> response = shoppingService.addProductToCart(VALID_SESSION_TOKEN, STORE_ID, PRODUCT_ID, quantity);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should mention stock availability", errorMessage, response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockCartFacade, times(1)).addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, quantity);
    }

    //
    // USE CASE 2.4: VIEWING THE SHOPPING CART
    //

    @Test
    public void testViewCart_Success() {
        // Arrange
        Map<String, Map<String, Integer>> cartContents = new HashMap<>();
        Map<String, Integer> storeProducts = new HashMap<>();
        storeProducts.put(PRODUCT_ID, 2);
        cartContents.put(STORE_ID, storeProducts);
        
        when(mockCartFacade.viewCart(CLIENT_ID)).thenReturn(cartContents);
        
        // Act
        Response<Map<String, Map<String, Integer>>> response = shoppingService.viewCart(VALID_SESSION_TOKEN);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return correct cart contents", cartContents, response.getValue());
        verify(mockCartFacade, times(1)).viewCart(CLIENT_ID);
    }

    @Test
    public void testViewCart_EmptyCart() {
        // Arrange
        Map<String, Map<String, Integer>> emptyCart = new HashMap<>();
        when(mockCartFacade.viewCart(CLIENT_ID)).thenReturn(emptyCart);
        
        // Act
        Response<Map<String, Map<String, Integer>>> response = shoppingService.viewCart(VALID_SESSION_TOKEN);
        
        // Assert
        assertFalse("Should not have error even for empty cart", response.errorOccurred());
        assertTrue("Should return empty map for empty cart", response.getValue().isEmpty());
        verify(mockCartFacade, times(1)).viewCart(CLIENT_ID);
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
        
        doNothing().when(mockCartFacade).checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Act
        Response<Boolean> response = shoppingService.checkout(VALID_SESSION_TOKEN, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        verify(mockCartFacade, times(1)).checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
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
        
        doThrow(new RuntimeException(errorMessage)).when(mockCartFacade).checkout(
            CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress
        );
        
        // Act
        Response<Boolean> response = shoppingService.checkout(VALID_SESSION_TOKEN, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should match payment error", errorMessage, response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockCartFacade, times(1)).checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
    }

    @Test
    public void testCheckout_SupplyServiceError() {
        // Arrange
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        String errorMessage = "Supply service rejected the order";
        
        doThrow(new RuntimeException(errorMessage)).when(mockCartFacade).checkout(
            CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress
        );
        
        // Act
        Response<Boolean> response = shoppingService.checkout(VALID_SESSION_TOKEN, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should match supply error", errorMessage, response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockCartFacade, times(1)).checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
    }
    
    @Test
    public void testCheckout_IntegrityRuleViolation() {
        // Arrange
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        String errorMessage = "Cannot complete purchase due to integrity rule violation";
        
        doThrow(new RuntimeException(errorMessage)).when(mockCartFacade).checkout(
            CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress
        );
        
        // Act
        Response<Boolean> response = shoppingService.checkout(VALID_SESSION_TOKEN, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should mention integrity rule", errorMessage, response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockCartFacade, times(1)).checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
    }
    
    @Test
    public void testGetClientPurchaseHistory_Success() {
        // Arrange
        Map<String, Map<String, Object>> purchaseHistory = new HashMap<>();
        Map<String, Object> receipt = new HashMap<>();
        receipt.put("totalPrice", 100.0);
        receipt.put("timestamp", new Date());
        Map<String, Integer> products = new HashMap<>();
        products.put(PRODUCT_ID, 2);
        receipt.put("products", products);
        purchaseHistory.put("receipt123", receipt);
        
        when(mockCartFacade.getClientPurchaseHistory(CLIENT_ID)).thenReturn(purchaseHistory);
        
        // Act
        Response<Map<String, Map<String, Object>>> response = shoppingService.getClientPurchaseHistory(VALID_SESSION_TOKEN);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return correct purchase history", purchaseHistory, response.getValue());
        verify(mockCartFacade, times(1)).getClientPurchaseHistory(CLIENT_ID);
    }

    //
    // USE CASE 3.9: SUBMITTING A PURCHASE BID
    //

    @Test
    public void testSubmitBid_Success() {
        // Arrange
        Auction mockAuction = mock(Auction.class);
        when(mockStoreFacade.isProductBiddable(STORE_ID, PRODUCT_ID)).thenReturn(true);
        when(mockStoreFacade.getAuctionForProduct(STORE_ID, PRODUCT_ID)).thenReturn(AUCTION_ID);
        when(mockStoreFacade.isStockAvailable(STORE_ID, PRODUCT_ID, 1)).thenReturn(true);
        when(mockStoreFacade.isValidBidAmount(AUCTION_ID, VALID_BID_PRICE)).thenReturn(true);
        when(mockStoreFacade.addBid(AUCTION_ID, USER_ID, VALID_BID_PRICE)).thenReturn(mockAuction);
        
        // Act
        Response<Auction> response = bidService.submitBid(VALID_SESSION_TOKEN, STORE_ID, PRODUCT_ID, VALID_BID_PRICE);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertNotNull("Should return auction object", response.getValue());
        verify(mockStoreFacade, times(1)).addBid(AUCTION_ID, USER_ID, VALID_BID_PRICE);
    }

    @Test
    public void testSubmitBid_InvalidToken() {
        // Act
        Response<Auction> response = bidService.submitBid(INVALID_SESSION_TOKEN, STORE_ID, PRODUCT_ID, VALID_BID_PRICE);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should mention invalid token", "Invalid session token", response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockStoreFacade, never()).addBid(anyString(), anyString(), anyFloat());
    }
    
    @Test
    public void testSubmitBid_BiddingNotSupported() {
        // Arrange
        when(mockStoreFacade.isProductBiddable(STORE_ID, PRODUCT_ID)).thenReturn(false);
        
        // Act
        Response<Auction> response = bidService.submitBid(VALID_SESSION_TOKEN, STORE_ID, PRODUCT_ID, VALID_BID_PRICE);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should mention bidding not supported", 
                     "This product does not support bidding", 
                     response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockStoreFacade, never()).addBid(anyString(), anyString(), anyFloat());
    }
    
    @Test
    public void testSubmitBid_QuantityNotAvailable() {
        // Arrange
        when(mockStoreFacade.isProductBiddable(STORE_ID, PRODUCT_ID)).thenReturn(true);
        when(mockStoreFacade.isStockAvailable(STORE_ID, PRODUCT_ID, 1)).thenReturn(false);
        
        // Act
        Response<Auction> response = bidService.submitBid(VALID_SESSION_TOKEN, STORE_ID, PRODUCT_ID, VALID_BID_PRICE);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should mention quantity not available", 
                     "Product is not available in stock", 
                     response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockStoreFacade, never()).addBid(anyString(), anyString(), anyFloat());
    }
    
    @Test
    public void testSubmitBid_BidPriceTooLow() {
        // Arrange
        when(mockStoreFacade.isProductBiddable(STORE_ID, PRODUCT_ID)).thenReturn(true);
        when(mockStoreFacade.getAuctionForProduct(STORE_ID, PRODUCT_ID)).thenReturn(AUCTION_ID);
        when(mockStoreFacade.isStockAvailable(STORE_ID, PRODUCT_ID, 1)).thenReturn(true);
        when(mockStoreFacade.isValidBidAmount(AUCTION_ID, INVALID_BID_PRICE)).thenReturn(false);
        
        // Act
        Response<Auction> response = bidService.submitBid(VALID_SESSION_TOKEN, STORE_ID, PRODUCT_ID, INVALID_BID_PRICE);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertEquals("Error message should mention bid price too low", 
                     "Bid price does not meet the minimum requirements", 
                     response.getErrorMessage());
        assertNull("Value should be null", response.getValue());
        verify(mockStoreFacade, never()).addBid(anyString(), anyString(), anyFloat());
    }
    
    /**
     * BidService inner class for bid submission functionality.
     */
    private static class BidService {
        private static final String CLASS_NAME = BidService.class.getSimpleName();
        
        private final TokenService tokenService;
        private final StoreFacade storeFacade;
        
        public BidService(TokenService tokenService, StoreFacade storeFacade) {
            this.tokenService = tokenService;
            this.storeFacade = storeFacade;
        }
        
        /**
         * Validates if the provided session token is valid.
         */
        private boolean isInvalid(String sessionToken) {
            return !tokenService.validateToken(sessionToken);
        }
        
        /**
         * Submits a bid for a product in a store.
         * Use Case 3.9: Submitting a Purchase Bid for a Single Product
         *
         * @param sessionToken The user's session token
         * @param storeId The ID of the store that sells the product
         * @param productId The ID of the product to bid on
         * @param bidPrice The price offered by the user
         * @return Response with the auction object if successful, or an error
         */
        public Response<Auction> submitBid(String sessionToken, String storeId, String productId, float bidPrice) {
            if (isInvalid(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, "submitBid", "Invalid session token");
                return new Response<>(new Error("Invalid session token"));
            }
            
            try {
                String userId = tokenService.extractId(sessionToken);
                
                // Check if the product supports bidding
                if (!storeFacade.isProductBiddable(storeId, productId)) {
                    TradingLogger.logError(CLASS_NAME, "submitBid", "Product does not support bidding");
                    return new Response<>(new Error("This product does not support bidding"));
                }
                
                // Check if product is available (assuming quantity of 1 for bidding)
                if (!storeFacade.isStockAvailable(storeId, productId, 1)) {
                    TradingLogger.logError(CLASS_NAME, "submitBid", "Product not available");
                    return new Response<>(new Error("Product is not available in stock"));
                }
                
                // Get the auction ID for this product
                String auctionId = storeFacade.getAuctionForProduct(storeId, productId);
                
                // Check if the bid price meets the minimum requirements
                if (!storeFacade.isValidBidAmount(auctionId, bidPrice)) {
                    TradingLogger.logError(CLASS_NAME, "submitBid", "Bid price too low");
                    return new Response<>(new Error("Bid price does not meet the minimum requirements"));
                }
                
                // Submit the bid
                Auction auction = storeFacade.addBid(auctionId, userId, bidPrice);
                TradingLogger.logEvent(CLASS_NAME, "submitBid", "Bid submitted successfully for auction: " + auctionId);
                return new Response<>(auction);
            } catch (Exception ex) {
                TradingLogger.logError(CLASS_NAME, "submitBid", "Failed to submit bid: %s", ex.getMessage());
                return new Response<>(new Error(ex.getMessage()));
            }
        }
    }
}