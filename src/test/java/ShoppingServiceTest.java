import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;

import Application.DTOs.CartDTO;
import Application.ShoppingService;
import Application.TokenService;
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

    @Mock
    private IPaymentService paymentService;
        
    private ShoppingService shoppingService;

    private IShoppingCartRepository cartRepository;
    private IShoppingBasketRepository basketRepository;
    private ItemFacade itemFacade;
    private StoreFacade storeFacade;
    private IReceiptRepository receiptRepository;
    private IProductRepository productRepository;
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
    private static final float VALID_BID_PRICE = 100.0f;

    @Before
    public void setUp() {
        // Initialize repositories
        productRepository = new MemoryProductRepository();
        receiptRepository = new MemoryReceiptRepository();
        basketRepository = new MemoryShoppingBasketRepository();
        cartRepository = new MemoryShoppingCartRepository();
        itemRepository = new MemoryItemRepository();
        storeRepository = new MemoryStoreRepository();
        IFeedbackRepository feedbackRepository = new MemoryFeedbackRepository();
        auctionRepository = new MemoryAuctionRepository();
        userRepository = new MemoryUserRepository();

        // Create sample product and store
        Product product = new Product(PRODUCT_ID, "Test Product", new HashSet<>());
        productRepository.add(PRODUCT_ID, product);

        Store store = new Store(STORE_ID, "Test Store", "Test Store Description", "owner123");
        storeRepository.add(STORE_ID, store);

        itemFacade = new ItemFacade(itemRepository, productRepository, storeRepository);
        itemFacade.add(STORE_ID, PRODUCT_ID, 10.0, 5, "Test Item Description");

        storeFacade = new StoreFacade(
            storeRepository,
            feedbackRepository,
            itemRepository,
            userRepository,
            auctionRepository
        );
        storeFacade.setStoreRepository(storeRepository);
        storeFacade.setItemRepository(itemRepository);
        storeFacade.setAuctionRepository(auctionRepository);
        storeFacade.setFeedbackRepository(feedbackRepository);
        storeFacade.setGetUser(userRepository);
        
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
        userRepository.add(userId.toString(), user);
    }

    //
    // USE CASE 2.3: SAVING PRODUCTS TO THE SHOPPING CART
    //

    @Test
    public void testAddProductToCart_Success() {
        assertTrue(CLIENT_ID, tokenService.validateToken(CLIENT_ID));
        Response<Boolean> response = shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        assertFalse("Shouldn't get error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    @Test
    public void testAddProductToCart_ServiceFailure() {
        String nonExistentStoreId = "nonexistent-store";
        Response<Boolean> response = shoppingService.addProductToCart(nonExistentStoreId, CLIENT_ID, PRODUCT_ID, 2);
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
    }

    //
    // USE CASE 2.4: VIEWING THE SHOPPING CART
    //

    @Test
    public void testViewCart_Success() {
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        Response<CartDTO> response = shoppingService.viewCart(CLIENT_ID);
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
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 1);
        Response<Boolean> response = shoppingService.checkout(
            CLIENT_ID, "1234567890123456", new Date(), "123", 12345L, "John Doe", "123 Main St");
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    @Test
    public void testCheckout_InsufficientStock() {
        // First add a product to the cart with quantity greater than available stock
        // From setup, we know the item has stock of 5
        int requestedQuantity = 10; // More than the available 5
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, requestedQuantity);
        
        // Prepare checkout parameters
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        // Act - Attempt to checkout
        Response<Boolean> response = shoppingService.checkout(CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
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
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 1);
        Response<Boolean> response = shoppingService.checkout(
            CLIENT_ID, "invalid", new Date(), "123", 12345L, "John Doe", "123 Main St");
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
    }

    //TODO! Refactor this test to use services
    @Test
    public void testConcurrentCheckout_WithLimitedStock() throws InterruptedException {
        // Create a product with limited stock (e.g., just 1 unit)
        final String limitedProductId = "limitedProduct";
        final String limitedStoreId = STORE_ID;
        final int availableStock = 1;
        
        // Create a product with limited stock
        Product limitedProduct = new Product(limitedProductId, "Limited Stock Product", new HashSet<>());
        productRepository.add(limitedProductId, limitedProduct);
        
        // Create an item with just 1 unit available
        Item limitedItem = new Item(limitedStoreId, limitedProductId, 10.0, availableStock, "Limited Stock Item");
        itemFacade.add(new Pair<>(limitedStoreId, limitedProductId), limitedItem);
        
        // Create tokens for two different clients
        String clientId1 = tokenService.generateToken(UUID.randomUUID().toString());
        String clientId2 = tokenService.generateToken(UUID.randomUUID().toString());
        
        // Standard checkout parameters
        final String cardNumber = "1234567890123456";
        final Date expiryDate = new Date();
        final String cvv = "123";
        final long transactionId = 12345L;
        final String clientName = "Test Client";
        final String deliveryAddress = "123 Test St";
        
        // Add 1 unit of the limited product to each client's cart
        shoppingService.addProductToCart(limitedStoreId, clientId1, limitedProductId, 1);
        shoppingService.addProductToCart(limitedStoreId, clientId2, limitedProductId, 1);
        
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
        
        // Verify the item's stock is now 0 (all units purchased)
        Item updatedItem = itemFacade.getItem(limitedStoreId, limitedProductId);
        assertEquals("Stock should be depleted", 0, updatedItem.getAmount());
    }
    //
    // CART MANAGEMENT - REMOVE PRODUCT
    //

    @Test
    public void testRemoveProductFromCart_WithQuantity_Success() {
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 3);
        Response<Boolean> response = shoppingService.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 1);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    @Test
    public void testRemoveProductFromCart_CompleteRemoval_Success() {
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        Response<Boolean> response = shoppingService.removeProductFromCart(STORE_ID, CLIENT_ID, PRODUCT_ID);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    //
    // CART MANAGEMENT - CLEAR OPERATIONS
    //

    @Test
    public void testClearCart_Success() {
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        Response<Boolean> response = shoppingService.clearCart(CLIENT_ID);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        assertTrue("Cart should be empty after clearing", shoppingService.viewCart(CLIENT_ID).getValue().getBaskets().isEmpty());
    }

    @Test
    public void testClearBasket_Success() {
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 2);
        Response<Boolean> response = shoppingService.clearBasket(CLIENT_ID, STORE_ID);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        assertTrue("Basket should be empty after clearing", shoppingService.viewCart(CLIENT_ID).getValue().getBaskets().isEmpty());
    }

    //
    // USE CASE 3.9: SUBMITTING A PURCHASE BID
    //

    @Test
    public void testMakeBid_Success() {
        try {
            String auctionDate = java.time.LocalDate.now().plusDays(1).toString();
            storeFacade.addAuction(STORE_ID, PRODUCT_ID, auctionDate, 50.0);
            String actualAuctionId = storeFacade.getAllStoreAuctions(STORE_ID).get(0).getAuctionId();

            String cardNumber = "1234567890123456";
            Date expiryDate = new Date();
            String cvv = "123";
            long transactionId = 12345L;
            String clientName = "John Doe";
            String deliveryAddress = "123 Main St";

            Response<Boolean> response = shoppingService.makeBid(
                actualAuctionId, CLIENT_ID, VALID_BID_PRICE,
                cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress
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
            String auctionDate = java.time.LocalDate.now().plusDays(1).toString();
            storeFacade.addAuction(STORE_ID, PRODUCT_ID, auctionDate, 150.0);
            String actualAuctionId = storeFacade.getAllStoreAuctions(STORE_ID).get(0).getAuctionId();

            float lowBidPrice = 100.0f;

            String cardNumber = "1234567890123456";
            Date expiryDate = new Date();
            String cvv = "123";
            long transactionId = 12345L;
            String clientName = "John Doe";
            String deliveryAddress = "123 Main St";

            Response<Boolean> response = shoppingService.makeBid(
                actualAuctionId, CLIENT_ID, lowBidPrice,
                cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress
            );

            assertTrue("Should have error", response.errorOccurred());
            assertNull("Value should be null", response.getValue());
            assertNotNull("Error message should not be null", response.getErrorMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    @Test
    public void testCheckout_PaymentServiceThrowsException() {
        // Create a bad payment service that throws an exception
        IPaymentService badPaymentService = new IPaymentService() {
            @Override
            public void updatePaymentServiceURL(String url) {
                // Do nothing
            }

            @Override
            public Response<Boolean> processPayment(String card_owner, String card_number, Date expiry_date, String cvv, 
                                                double price, long andIncrement, String name, String deliveryAddress) {
                throw new RuntimeException("Payment service connection failure");
            }

            @Override
            public void initialize() {
                // Do nothing
            }
        };

        // First add a product to the cart
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 1);
        
        // Replace the mocked payment service with our bad one
        // We need to recreate the shopping service with our bad payment service
        ShoppingService shoppingServiceWithBadPayment = new ShoppingService(
            cartRepository,
            basketRepository,
            itemFacade,
            storeFacade,
            receiptRepository,
            productRepository,
            tokenService
        );
        
        // Use reflection to set the bad payment service
        try {
            java.lang.reflect.Field cartFacadeField = ShoppingService.class.getDeclaredField("cartFacade");
            cartFacadeField.setAccessible(true);
            Object cartFacade = cartFacadeField.get(shoppingServiceWithBadPayment);
            
            java.lang.reflect.Field paymentServiceField = cartFacade.getClass().getDeclaredField("paymentService");
            paymentServiceField.setAccessible(true);
            paymentServiceField.set(cartFacade, badPaymentService);
        } catch (Exception e) {
            fail("Failed to set bad payment service: " + e.getMessage());
        }
        
        // Prepare checkout parameters
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        // Act - attempt to checkout with the bad payment service
        Response<Boolean> response = shoppingServiceWithBadPayment.checkout(
            CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        assertTrue("Should have error due to payment service failure", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
        assertTrue("Error should mention payment failure", 
                response.getErrorMessage().toLowerCase().contains("payment") || 
                response.getErrorMessage().toLowerCase().contains("failed") ||
                response.getErrorMessage().toLowerCase().contains("exception"));
        
        // Verify that the inventory was rolled back (item quantity not decreased)
        Item item = itemFacade.getItem(STORE_ID, PRODUCT_ID);
        assertEquals("Item quantity should be unchanged after failed checkout", 5, item.getAmount());
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
                // Create a failure response using the Response class
                return Response.error("Payment declined: Insufficient funds");
            }

            @Override
            public void initialize() {
                // Do nothing
            }
        };

        // First add a product to the cart
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 1);
        
        // Replace the mocked payment service with our bad one
        // We need to recreate the shopping service with our bad payment service
        ShoppingService shoppingServiceWithBadPayment = new ShoppingService(
            cartRepository,
            basketRepository,
            itemFacade,
            storeFacade,
            receiptRepository,
            productRepository,
            tokenService
        );
        
        // Use reflection to set the bad payment service
        try {
            java.lang.reflect.Field cartFacadeField = ShoppingService.class.getDeclaredField("cartFacade");
            cartFacadeField.setAccessible(true);
            Object cartFacade = cartFacadeField.get(shoppingServiceWithBadPayment);
            
            java.lang.reflect.Field paymentServiceField = cartFacade.getClass().getDeclaredField("paymentService");
            paymentServiceField.setAccessible(true);
            paymentServiceField.set(cartFacade, badPaymentService);
        } catch (Exception e) {
            fail("Failed to set bad payment service: " + e.getMessage());
        }
        
        // Prepare checkout parameters
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        // Act - attempt to checkout with the bad payment service
        Response<Boolean> response = shoppingServiceWithBadPayment.checkout(
            CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        
        // Assert
        assertTrue("Should have error due to payment service failure", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
        assertTrue("Error should mention payment declined", 
                response.getErrorMessage().toLowerCase().contains("payment") || 
                response.getErrorMessage().toLowerCase().contains("declined") ||
                response.getErrorMessage().toLowerCase().contains("funds"));
        
        // Verify that the inventory was rolled back (item quantity not decreased)
        Item item = itemFacade.getItem(STORE_ID, PRODUCT_ID);
        assertEquals("Item quantity should be unchanged after failed checkout", 5, item.getAmount());
    }
}
