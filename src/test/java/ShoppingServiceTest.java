import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

import Application.DTOs.OrderDTO;
import Application.ShoppingService;
import Application.utils.Response;
import Domain.Pair;
import Domain.TokenService;
import Domain.ExternalServices.IPaymentService;
import Domain.Shopping.IReceiptRepository;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.StoreFacade;
import Domain.Store.Product;
import Domain.Store.Store;
import Domain.User.IUserRepository;
import Domain.User.Member;
import Domain.User.User;
import Infrastructure.Repositories.MemoryAuctionRepository;
import Infrastructure.Repositories.MemoryFeedbackRepository;
import Infrastructure.Repositories.MemoryItemRepository;
import Infrastructure.Repositories.MemoryProductRepository;
import Infrastructure.Repositories.MemoryReceiptRepository;
import Infrastructure.Repositories.MemoryShoppingBasketRepository;
import Infrastructure.Repositories.MemoryShoppingCartRepository;
import Infrastructure.Repositories.MemoryStoreRepository;
import Infrastructure.Repositories.MemoryUserRepository;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IProductRepository;
import Domain.Store.IStoreRepository;

public class ShoppingServiceTest {

    private ShoppingService shoppingService;

    
    private IShoppingCartRepository cartRepository;
    private IShoppingBasketRepository basketRepository;
    private ItemFacade itemFacade;
    private StoreFacade storeFacade;
    private IReceiptRepository receiptRepository;
    private IProductRepository productRepository;
    private IShoppingCartFacade cartFacade;
    private IPaymentService paymentService;
    private IItemRepository itemRepository;
    private IStoreRepository storeRepository;
    private IAuctionRepository auctionRepository;
    private IUserRepository userRepository;
    static UUID userId = UUID.randomUUID();
    static TokenService tokenService = new TokenService();
    
    // Common test constants
    private static final String CLIENT_ID = tokenService.generateToken(userId.toString());
    private static final String STORE_ID = "store123";
    private static final String PRODUCT_ID = "product123";
    private static final String AUCTION_ID = "auction123";
    private static final float VALID_BID_PRICE = 100.0f;

    @Before
    public void setUp() {
        // Initialize mocks
        // MockitoAnnotations.initMocks(this);
        paymentService = mock(IPaymentService.class);
        
        // Create real implementations for repositories
        productRepository = new MemoryProductRepository();
        receiptRepository = new MemoryReceiptRepository();
        basketRepository = new MemoryShoppingBasketRepository();
        cartRepository = new MemoryShoppingCartRepository();
        itemRepository = new MemoryItemRepository();
        storeRepository = new MemoryStoreRepository();
        IFeedbackRepository feedbackRepository = new MemoryFeedbackRepository();
        auctionRepository = new MemoryAuctionRepository();
        userRepository = new MemoryUserRepository();
        
        // Set up test data - create a product
        Product product = new Product(PRODUCT_ID, "Test Product", new HashSet<>());
        productRepository.add(PRODUCT_ID, product);
        
        // Create a store
        Store store = new Store(STORE_ID, "Test Store", "Test Store Description", "owner123");
        storeRepository.add(STORE_ID, store);
        
        // Initialize facades
        itemFacade = new ItemFacade(itemRepository, productRepository, storeRepository);
        
        // Create a test item
        Item item = new Item(STORE_ID, PRODUCT_ID, 10.0, 5, "Test Item Description");
        itemFacade.add(new Pair<>(STORE_ID, PRODUCT_ID), item);
        
        // Initialize StoreFacade
        storeFacade = new StoreFacade(
            storeRepository, 
            feedbackRepository, 
            itemRepository,
            userRepository,
            auctionRepository
        );
        
        // Create an auction for testing
        storeFacade.setStoreRepository(storeRepository);
        storeFacade.setItemRepository(itemRepository);
        storeFacade.setAuctionRepository(auctionRepository);
        storeFacade.setFeedbackRepository(feedbackRepository);
        storeFacade.setGetUser(userRepository);
        
        // Create ShoppingCartFacade
        cartFacade = new ShoppingCartFacade(
            cartRepository,
            basketRepository,
            paymentService,
            itemFacade,
            storeFacade,
            receiptRepository,
            productRepository
        );
        
        // Initialize the ShoppingService with real repositories and facades
        shoppingService = new ShoppingService(
            cartRepository,
            basketRepository,
            itemFacade,
            storeFacade,
            receiptRepository,
            productRepository,
            tokenService
        );


        User user = new Member(userId, "Member1", "passpass", "email@email.com");
        this.userRepository.add(userId.toString(), user);
        
        // Set the cart facade on the shopping service if it has a setter method
        // If there is no setter method, you may need to modify the ShoppingService class
        // shoppingService.setCartFacade(cartFacade);
    }

    //
    // USE CASE 2.3: SAVING PRODUCTS TO THE SHOPPING CART
    //

    @Test
    public void testAddProductToCart_Success() {
        assertTrue(CLIENT_ID, tokenService.validateToken(CLIENT_ID));
        // Act - use the real implementation to add a product
        Response<Boolean> response = shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        
        // Assert
        assertFalse("Shouldn't get error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }
    
    @Test
    public void testAddProductToCart_ServiceFailure() {
        // Use a non-existent store to trigger a failure
        String nonExistentStoreId = "nonexistent-store";
        
        // Act
        Response<Boolean> response = shoppingService.addProductToCart(nonExistentStoreId, CLIENT_ID, PRODUCT_ID, 2);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
    }

    //
    // USE CASE 2.4: VIEWING THE SHOPPING CART
    //

    @Test
    public void testViewCart_Success() {
        // First add a product to the cart
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        
        // Act
        Response<Set<OrderDTO>> response = shoppingService.viewCart(CLIENT_ID);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertNotNull("Response value should not be null", response.getValue());
        assertFalse("Cart should have at least one item", response.getValue().isEmpty());
    }

    @Test
    public void testViewCart_EmptyCart() {
        // Make sure the cart is empty by using a new client ID
        String newClientId = "new-client";
        String sessionToken = tokenService.generateToken(newClientId);
        // Act
        Response<Set<OrderDTO>> response = shoppingService.viewCart(sessionToken);
        
        // Assert
        assertFalse("Should not have error even for empty cart", response.errorOccurred());
        assertTrue("Cart should be empty", response.getValue().isEmpty());
    }
    
    @Test
    public void testViewCart_ServiceFailure() {
        // We'll use a null client ID to trigger a failure
        String nullClientId = null;
        
        // Act
        Response<Set<OrderDTO>> response = shoppingService.viewCart(nullClientId);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
    }

    //
    // USE CASE 2.5: IMMEDIATE PURCHASE (CHECKOUT)
    //

    @Test
    public void testCheckout_Success() {
        // First add a product to the cart
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 1);
        
        // Prepare checkout parameters
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        // Act
        Response<Boolean> response = shoppingService.checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        // Note: If payment service is mocked to always succeed, this should not error
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    @Test
    public void testCheckout_PaymentError() {
        // First add a product to the cart
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 1);
        
        // Use invalid card details to trigger a payment error
        String invalidCardNumber = "invalid";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        // Act
        Response<Boolean> response = shoppingService.checkout(CLIENT_ID, invalidCardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
    }
    
    //
    // CART MANAGEMENT - REMOVE PRODUCT
    //
    
    @Test
    public void testRemoveProductFromCart_WithQuantity_Success() {
        // First add a product to the cart
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 3);
        
        // Act - Remove fewer than total quantity
        Response<Boolean> response = shoppingService.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 1);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }
    
    @Test
    public void testRemoveProductFromCart_CompleteRemoval_Success() {
        // First add a product to the cart
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        
        // Act - Remove the product entirely
        Response<Boolean> response = shoppingService.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }
    
    //
    // CART MANAGEMENT - CLEAR OPERATIONS
    //
    
    @Test
    public void testClearCart_Success() {
        // First add a product to the cart
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        
        // Act
        Response<Boolean> response = shoppingService.clearCart(CLIENT_ID);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        
        // Verify cart is empty by checking with viewCart
        Response<Set<OrderDTO>> cartResponse = shoppingService.viewCart(CLIENT_ID);
        assertTrue("Cart should be empty after clearing", cartResponse.getValue().isEmpty());
    }
    
    @Test
    public void testClearBasket_Success() {
        // First add a product to the cart
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        
        // Act
        Response<Boolean> response = shoppingService.clearBasket(CLIENT_ID, STORE_ID);
        
        // Assert
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        
        // Verify basket is empty by checking with viewCart
        Response<Set<OrderDTO>> cartResponse = shoppingService.viewCart(CLIENT_ID);
        assertTrue("Basket should be empty after clearing", cartResponse.getValue().isEmpty());
    }
    
    //
    // USE CASE 3.9: SUBMITTING A PURCHASE BID
    //
    
    @Test
    public void testMakeBid_Success() {
        try {
            // Create an auction first
            String auctionDate = java.time.LocalDate.now().plusDays(1).toString();
            storeFacade.addAuction(STORE_ID, PRODUCT_ID, auctionDate, 50.0);
            
            // Find the auction ID since it's generated dynamically
            String actualAuctionId = storeFacade.getAllStoreAuctions(STORE_ID).get(0).getAuctionId();
            
            // Act
            Response<Boolean> response = shoppingService.makeBid(actualAuctionId, CLIENT_ID, VALID_BID_PRICE);
            
            // Assert
            assertFalse("Should not have error", response.errorOccurred());
            assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testMakeBid_BidRejected() {
        try {
            // Create an auction first with a high start price
            String auctionDate = java.time.LocalDate.now().plusDays(1).toString();
            storeFacade.addAuction(STORE_ID, PRODUCT_ID, auctionDate, 150.0);
            
            // Find the auction ID since it's generated dynamically
            String actualAuctionId = storeFacade.getAllStoreAuctions(STORE_ID).get(0).getAuctionId();
            
            // Act - try to make a bid below the start price
            float lowBidPrice = 100.0f; // Lower than the 150.0 start price
            Response<Boolean> response = shoppingService.makeBid(actualAuctionId, CLIENT_ID, lowBidPrice);
            
            // Assert
            assertTrue("Should have error", response.errorOccurred());
            assertNull("Value should be null", response.getValue());
            assertNotNull("Error message should not be null", response.getErrorMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
}