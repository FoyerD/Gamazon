import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;

import Application.DTOs.AuctionDTO;
import Application.DTOs.CartDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.ProductDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.ServiceManager;
import Application.ShoppingService;
import Application.TokenService;
import Application.UserService;
import Application.utils.Response;
import Domain.ExternalServices.IPaymentService;
import Domain.Shopping.IReceiptRepository;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.StoreFacade;
import Domain.Store.Product;
import Domain.Store.Store;
import Domain.User.IUserRepository;
import Domain.User.Member;
import Domain.User.User;
import Infrastructure.MemoryRepoManager;
import Domain.FacadeManager;
import Domain.Pair;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IProductRepository;
import Domain.Store.IStoreRepository;

public class ShoppingServiceTest {

    // Use concrete implementations for services and facades
    private ShoppingService shoppingService;
    private UserService userService;
    private TokenService tokenService;
    private ItemFacade itemFacade;
    private StoreFacade storeFacade;
    
    // Repository manager
    private MemoryRepoManager repositoryManager;

    // Service manager
    private ServiceManager serviceManager;
    
    // Repositories accessed through the manager
    private IShoppingCartRepository cartRepository;
    private IShoppingBasketRepository basketRepository;
    private IReceiptRepository receiptRepository;
    private IProductRepository productRepository;
    private IItemRepository itemRepository;
    private IStoreRepository storeRepository;
    private IAuctionRepository auctionRepository;
    private IUserRepository userRepository;
    private IFeedbackRepository feedbackRepository;
    
    // Mock service for testing
    private IPaymentService mockPaymentService;
    
    // Test user data
    private UserDTO guest;
    private UserDTO user;
    private String clientToken;
    
    // Common test constants
    private static final String STORE_ID = "store123";
    private static final String PRODUCT_ID = "product123";
    private static final float VALID_BID_PRICE = 100.0f;
    
    @Before
    public void setUp() {
        // Initialize the token service
        tokenService = new TokenService();
        
        // Create mock payment service
        mockPaymentService = mock(IPaymentService.class);
        
        // Initialize repository manager
        repositoryManager = new MemoryRepoManager();

        // Initialize service manager
        serviceManager = new ServiceManager(new FacadeManager(repositoryManager, mockPaymentService));
        
        // Get repositories through the manager
        productRepository = repositoryManager.getProductRepository();
        receiptRepository = repositoryManager.getReceiptRepository();
        basketRepository = repositoryManager.getShoppingBasketRepository();
        cartRepository = repositoryManager.getShoppingCartRepository();
        itemRepository = repositoryManager.getItemRepository();
        storeRepository = repositoryManager.getStoreRepository();
        feedbackRepository = repositoryManager.getFeedbackRepository();
        auctionRepository = repositoryManager.getAuctionRepository();
        userRepository = repositoryManager.getUserRepository();
        
        // Create Domain.User.LoginManager for UserService
        Domain.User.LoginManager loginManager = new Domain.User.LoginManager(userRepository);
        
        // Initialize UserService
        userService = new UserService(loginManager, tokenService);
        
        // Register a test user using UserService
        Response<UserDTO> guestResponse = userService.guestEntry();
        guest = guestResponse.getValue();
        
        Response<UserDTO> userResponse = userService.register(
            guest.getSessionToken(), 
            "Member1", 
            "WhyWontWork1!", // Using consistent password format
            "email@email.com"
        );
        
        user = userResponse.getValue();
        clientToken = user.getSessionToken();
        
        // Create sample product and store
        Product product = new Product(PRODUCT_ID, "Test Product", new HashSet<>());
        productRepository.add(PRODUCT_ID, product);
        
        Store store = new Store(STORE_ID, "Test Store", "Test Store Description", "owner123");
        storeRepository.add(STORE_ID, store);
        
        // Initialize item facade
        itemFacade = new ItemFacade(itemRepository, productRepository, storeRepository);
        
        // Add a test item
        itemFacade.add(STORE_ID, PRODUCT_ID, 10.0, 5, "Test Item Description");
        
        // Initialize store facade
        storeFacade = new StoreFacade(
            storeRepository,
            feedbackRepository,
            itemRepository,
            userRepository,
            auctionRepository
        );
        
        // Initialize the ShoppingService with our repositories and facades
        shoppingService = new ShoppingService(
            cartRepository,
            basketRepository,
            itemFacade,
            storeFacade,
            receiptRepository,
            productRepository,
            tokenService
        );
    }
    
    //
    // USE CASE 2.3: SAVING PRODUCTS TO THE SHOPPING CART
    //

    @Test
    public void testAddProductToCart_Success() {
        assertTrue(clientToken, tokenService.validateToken(clientToken));
        Response<Boolean> response = shoppingService.addProductToCart(STORE_ID, clientToken, PRODUCT_ID, 2);
        assertFalse("Shouldn't get error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    @Test
    public void testAddProductToCart_ServiceFailure() {
        String nonExistentStoreId = "nonexistent-store";
        Response<Boolean> response = shoppingService.addProductToCart(nonExistentStoreId, clientToken, PRODUCT_ID, 2);
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
    }

    //
    // USE CASE 2.4: VIEWING THE SHOPPING CART
    //

    @Test
    public void testViewCart_Success() {
        shoppingService.addProductToCart(STORE_ID, clientToken, PRODUCT_ID, 2);
        Response<CartDTO> response = shoppingService.viewCart(clientToken);
        assertFalse("Should not have error", response.errorOccurred());
        assertNotNull("Response value should not be null", response.getValue());
        assertFalse("Cart should have at least one item", response.getValue().getBaskets().isEmpty());
    }

    @Test
    public void testViewCart_EmptyCart() {
        String sessionToken = tokenService.generateToken("new-client");
        Response<CartDTO> response = shoppingService.viewCart(sessionToken);
        assertFalse("Should not have error even for empty cart", response.errorOccurred());
        assertTrue("Cart should be empty", response.getValue().getBaskets().isEmpty());
    }

    //
    // USE CASE 2.5: IMMEDIATE PURCHASE (CHECKOUT)
    //

    @Test
    public void testCheckout_Success() {
        shoppingService.addProductToCart(STORE_ID, clientToken, PRODUCT_ID, 1);
        Response<Boolean> response = shoppingService.checkout(
            clientToken, "1234567890123456", new Date(), "123", 12345L, "John Doe", "123 Main St");
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    @Test
    public void testCheckout_InsufficientStock() {
        // First add a product to the cart with quantity greater than available stock
        // From setup, we know the item has stock of 5
        int requestedQuantity = 10; // More than the available 5
        shoppingService.addProductToCart(STORE_ID, clientToken, PRODUCT_ID, requestedQuantity);
        
        // Prepare checkout parameters
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        // Act - Attempt to checkout
        Response<Boolean> response = shoppingService.checkout(clientToken, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        assertTrue("Should have error due to insufficient stock", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
        // Optionally, check that the error message mentions stock or inventory
        assertTrue("Error should mention insufficient stock", 
                response.getErrorMessage().toLowerCase().contains("stock") || 
                response.getErrorMessage().toLowerCase().contains("inventory") ||
                response.getErrorMessage().toLowerCase().contains("quantity"));
    }

    @Test
    public void testCheckout_PaymentError() {
        shoppingService.addProductToCart(STORE_ID, clientToken, PRODUCT_ID, 1);
        Response<Boolean> response = shoppingService.checkout(
            clientToken, "invalid", new Date(), "123", 12345L, "John Doe", "123 Main St");
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
    }

    @Test
    public void testConcurrentCheckout_WithLimitedStock() throws InterruptedException {
        // Create a limited product using the ProductService
        Response<ProductDTO> limitedProductResponse = serviceManager.getProductService().addProduct(
            clientToken, 
            "Limited Stock Product", 
            List.of("limited"), 
            List.of("A product with limited stock")
        );
        
        assertFalse("Product creation should succeed", limitedProductResponse.errorOccurred());
        ProductDTO limitedProduct = limitedProductResponse.getValue();
        String limitedProductId = limitedProduct.getId();
        
        // Create a store to sell the limited product using StoreService
        Response<StoreDTO> limitedStoreResponse = serviceManager.getStoreService().addStore(
            clientToken,
            "Limited Stock Store", 
            "A store that sells limited stock items"
        );
        
        assertFalse("Store creation should succeed", limitedStoreResponse.errorOccurred());
        StoreDTO limitedStore = limitedStoreResponse.getValue();
        String limitedStoreId = limitedStore.getId();
        
        // Add the item to the store with just 1 unit available using ItemService
        Response<ItemDTO> limitedItemResponse = serviceManager.getItemService().add(
            clientToken,
            limitedStoreId,
            limitedProductId,
            10.0f,
            1, // Only 1 unit available
            "Limited Stock Item"
        );
        
        assertFalse("Item addition should succeed", limitedItemResponse.errorOccurred());
        
        // Create two additional users for testing
        Response<UserDTO> guest1Response = userService.guestEntry();
        Response<UserDTO> guest2Response = userService.guestEntry();
        
        // Register them
        Response<UserDTO> testUser1Response = userService.register(
            guest1Response.getValue().getSessionToken(),
            "TestUser1",
            "WhyWontWork1!",
            "testuser1@example.com"
        );
        
        Response<UserDTO> testUser2Response = userService.register(
            guest2Response.getValue().getSessionToken(),
            "TestUser2", 
            "WhyWontWork2!",
            "testuser2@example.com"
        );
        
        assertFalse("First user registration should succeed", testUser1Response.errorOccurred());
        assertFalse("Second user registration should succeed", testUser2Response.errorOccurred());
        
        // Get their tokens
        String clientId1 = testUser1Response.getValue().getSessionToken();
        String clientId2 = testUser2Response.getValue().getSessionToken();
        
        // Standard checkout parameters
        final String cardNumber = "1234567890123456";
        final Date expiryDate = new Date();
        final String cvv = "123";
        final long transactionId = 12345L;
        final String clientName = "Test Client";
        final String deliveryAddress = "123 Test St";
        
        // Add 1 unit of the limited product to each client's cart using ShoppingService
        Response<Boolean> addToCart1Response = shoppingService.addProductToCart(
            limitedStoreId, 
            clientId1, 
            limitedProductId, 
            1
        );
        
        Response<Boolean> addToCart2Response = shoppingService.addProductToCart(
            limitedStoreId, 
            clientId2, 
            limitedProductId, 
            1
        );
        
        assertFalse("First cart addition should succeed", addToCart1Response.errorOccurred());
        assertFalse("Second cart addition should succeed", addToCart2Response.errorOccurred());
        
        // Track the results for each thread
        final boolean[] threadSuccess = new boolean[2];
        final String[] threadErrors = new String[2];
        
        // Create two threads, each attempting to checkout
        Thread thread1 = new Thread(() -> {
            Response<Boolean> response = shoppingService.checkout(
                clientId1, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
            threadSuccess[0] = !response.errorOccurred();
            if (response.errorOccurred()) {
                threadErrors[0] = response.getErrorMessage();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            Response<Boolean> response = shoppingService.checkout(
                clientId2, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
            threadSuccess[1] = !response.errorOccurred();
            if (response.errorOccurred()) {
                threadErrors[1] = response.getErrorMessage();
            }
        });
        
        // Start both threads
        thread1.start();
        thread2.start();
        
        // Wait for both threads to complete
        thread1.join(5000);  // Wait up to 5 seconds
        thread2.join(5000);
        
        // Verify that exactly one thread succeeded and one failed
        assertTrue("Either thread1 succeeded and thread2 failed, or vice versa",
                (threadSuccess[0] && !threadSuccess[1]) || (!threadSuccess[0] && threadSuccess[1]));
        
        // At least one thread should have failed with a stock-related error
        if (!threadSuccess[0]) {
            assertNotNull("Thread 1 should have an error message", threadErrors[0]);
            assertTrue("Thread 1 error should mention stock limitation", 
                    threadErrors[0].toLowerCase().contains("stock") || 
                    threadErrors[0].toLowerCase().contains("inventory") ||
                    threadErrors[0].toLowerCase().contains("quantity"));
        }
        
        if (!threadSuccess[1]) {
            assertNotNull("Thread 2 should have an error message", threadErrors[1]);
            assertTrue("Thread 2 error should mention stock limitation", 
                    threadErrors[1].toLowerCase().contains("stock") || 
                    threadErrors[1].toLowerCase().contains("inventory") ||
                    threadErrors[1].toLowerCase().contains("quantity"));
        }
        
        // Verify the item's stock is now 0 by getting it through the ItemService
        Response<ItemDTO> updatedItemResponse = serviceManager.getItemService().getItem(
            clientToken, 
            limitedStoreId, 
            limitedProductId
        );
        
        assertFalse("Item retrieval should succeed", updatedItemResponse.errorOccurred());
        assertEquals("Stock should be depleted", 0, updatedItemResponse.getValue().getAmount());
    }
    //
    // CART MANAGEMENT - REMOVE PRODUCT
    //

    @Test
    public void testRemoveProductFromCart_WithQuantity_Success() {
        shoppingService.addProductToCart(STORE_ID, clientToken, PRODUCT_ID, 3);
        Response<Boolean> response = shoppingService.removeProductFromCart(STORE_ID, clientToken, PRODUCT_ID, 1);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    @Test
    public void testRemoveProductFromCart_CompleteRemoval_Success() {
        shoppingService.addProductToCart(STORE_ID, clientToken, PRODUCT_ID, 2);
        Response<Boolean> response = shoppingService.removeProductFromCart(STORE_ID, clientToken, PRODUCT_ID);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    //
    // CART MANAGEMENT - CLEAR OPERATIONS
    //

    @Test
    public void testClearCart_Success() {
        shoppingService.addProductToCart(STORE_ID, clientToken, PRODUCT_ID, 2);
        Response<Boolean> response = shoppingService.clearCart(clientToken);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        assertTrue("Cart should be empty after clearing", shoppingService.viewCart(clientToken).getValue().getBaskets().isEmpty());
    }

    @Test
    public void testClearBasket_Success() {
        shoppingService.addProductToCart(STORE_ID, clientToken, PRODUCT_ID, 2);
        Response<Boolean> response = shoppingService.clearBasket(clientToken, STORE_ID);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        assertTrue("Basket should be empty after clearing", shoppingService.viewCart(clientToken).getValue().getBaskets().isEmpty());
    }

    //
    // USE CASE 3.9: SUBMITTING A PURCHASE BID
    //

    @Test
    public void testMakeBid_Success() {
        try {
            // Create auction using StoreService instead of directly using storeFacade
            String auctionDate = java.time.LocalDate.now().plusDays(1).toString();
            
            Response<AuctionDTO> auctionResponse = serviceManager.getStoreService().addAuction(
                clientToken, 
                STORE_ID, 
                PRODUCT_ID, 
                auctionDate, 
                50.0
            );
            
            assertFalse("Auction creation should succeed", auctionResponse.errorOccurred());
            
            // Get auctions through the StoreService
            Response<List<AuctionDTO>> auctionsResponse = serviceManager.getStoreService().getAllStoreAuctions(
                clientToken, 
                STORE_ID
            );
            
            assertFalse("Getting auctions should succeed", auctionsResponse.errorOccurred());
            assertFalse("Auctions list should not be empty", auctionsResponse.getValue().isEmpty());
            
            String actualAuctionId = auctionsResponse.getValue().get(0).getAuctionId();

            String cardNumber = "1234567890123456";
            Date expiryDate = new Date();
            String cvv = "123";
            long transactionId = 12345L;
            String clientName = "John Doe";
            String deliveryAddress = "123 Main St";

            // Continue using ShoppingService for making bids
            Response<Boolean> response = shoppingService.makeBid(
                actualAuctionId, 
                clientToken, 
                VALID_BID_PRICE,
                cardNumber, 
                expiryDate, 
                cvv, 
                transactionId, 
                clientName, 
                deliveryAddress
            );

            assertFalse("Should not have error", response.errorOccurred());
            assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testMakeBid_BidRejected() {
        try {
            // Create auction with a higher starting price using StoreService
            String auctionDate = java.time.LocalDate.now().plusDays(1).toString();
            
            Response<AuctionDTO> auctionResponse = serviceManager.getStoreService().addAuction(
                clientToken, 
                STORE_ID, 
                PRODUCT_ID, 
                auctionDate, 
                150.0
            );
            
            assertFalse("Auction creation should succeed", auctionResponse.errorOccurred());
            
            // Get auctions through the StoreService
            Response<List<AuctionDTO>> auctionsResponse = serviceManager.getStoreService().getAllStoreAuctions(
                clientToken, 
                STORE_ID
            );
            
            assertFalse("Getting auctions should succeed", auctionsResponse.errorOccurred());
            assertFalse("Auctions list should not be empty", auctionsResponse.getValue().isEmpty());

            String actualAuctionId = auctionsResponse.getValue().get(0).getAuctionId();

            float lowBidPrice = 100.0f;

            String cardNumber = "1234567890123456";
            Date expiryDate = new Date();
            String cvv = "123";
            long transactionId = 12345L;
            String clientName = "John Doe";
            String deliveryAddress = "123 Main St";

            // Continue using ShoppingService for making bids
            Response<Boolean> response = shoppingService.makeBid(
                actualAuctionId, 
                clientToken, 
                lowBidPrice,
                cardNumber, 
                expiryDate, 
                cvv, 
                transactionId, 
                clientName, 
                deliveryAddress
            );

            assertTrue("Should have error", response.errorOccurred());
            assertNull("Value should be null", response.getValue());
            assertNotNull("Error message should not be null", response.getErrorMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }


    @Test
    public void testCheckout_PaymentServiceReturnsFailure() {
        // Create a bad payment service that returns a failure response
        IPaymentService badPaymentService = new IPaymentService() {
            @Override
            public void updatePaymentServiceURL(String url) {
                // Do nothing
            }

            @Override
            public Response<Boolean> processPayment(String card_owner, String card_number, Date expiry_date, String cvv, 
                                                double price, long andIncrement, String name, String deliveryAddress) {
                // Create a failure response
                return Response.error("Payment declined: Insufficient funds");
            }

            @Override
            public void initialize() {
                // Do nothing
            }
        };

        // First add a product to the cart
        shoppingService.addProductToCart(STORE_ID, clientToken, PRODUCT_ID, 1);

        // Save the initial item state by getting it through ItemService
        Response<ItemDTO> initialItemResponse = serviceManager.getItemService().getItem(
            clientToken, 
            STORE_ID, 
            PRODUCT_ID
        );
        assertFalse("Initial item retrieval should succeed", initialItemResponse.errorOccurred());
        int initialQuantity = initialItemResponse.getValue().getAmount();
        
        // Update the payment service through the market service
        Response<Void> updatePaymentResponse = serviceManager.getMarketService().updatePaymentService(
            clientToken, 
            badPaymentService
        );
        assertFalse("Updating payment service should succeed", updatePaymentResponse.errorOccurred());
        
        // Prepare checkout parameters
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        // Act - attempt to checkout with the bad payment service
        Response<Boolean> response = shoppingService.checkout(
            clientToken, 
            cardNumber, 
            expiryDate, 
            cvv, 
            transactionId, 
            clientName, 
            deliveryAddress
        );
        
        // Assert
        assertTrue("Should have error due to payment service failure", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
        assertTrue("Error should mention payment declined", 
                response.getErrorMessage().toLowerCase().contains("payment") || 
                response.getErrorMessage().toLowerCase().contains("declined") ||
                response.getErrorMessage().toLowerCase().contains("funds"));
        
        // Verify that the inventory was rolled back using ItemService
        Response<ItemDTO> finalItemResponse = serviceManager.getItemService().getItem(
            clientToken, 
            STORE_ID, 
            PRODUCT_ID
        );
        assertFalse("Final item retrieval should succeed", finalItemResponse.errorOccurred());
        assertEquals("Item quantity should be unchanged after failed checkout", 
            initialQuantity, 
            finalItemResponse.getValue().getAmount()
        );
        
        // Restore original payment service to not affect other tests
        serviceManager.getMarketService().updatePaymentService(clientToken, mockPaymentService);
    }

}