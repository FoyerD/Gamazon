import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;

import Application.DTOs.OrderDTO;
import Application.ShoppingService;
import Application.utils.Response;
import Domain.Pair;
import Domain.TokenService;
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

    @Test
    public void testCheckout_PaymentServiceFreezesOrTimesOut() {
        // Create a bad payment service that hangs/freezes for a long time
        IPaymentService badPaymentService = new IPaymentService() {
            @Override
            public void updatePaymentServiceURL(String url) {
                // Do nothing
            }

            @Override
            public Response<Boolean> processPayment(String card_owner, String card_number, Date expiry_date, String cvv, 
                                                double price, long andIncrement, String name, String deliveryAddress) {
                try {
                    // Simulate a long-running process or timeout
                    Thread.sleep(5000); // 5 seconds delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // Return success after the delay
                return Response.success(true);
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
        
        // Act - attempt to checkout with the slow payment service
        // Use a separate thread with a timeout to test system behavior with slow payment service
        final Response<Boolean>[] responseHolder = new Response[1];
        Thread checkoutThread = new Thread(() -> {
            responseHolder[0] = shoppingServiceWithBadPayment.checkout(
                CLIENT_ID, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
        });
        
        // Start the checkout and give it limited time to complete
        checkoutThread.start();
        try {
            checkoutThread.join(2000); // Wait max 2 seconds for checkout to complete
            if (checkoutThread.isAlive()) {
                // Checkout thread is still running (payment service is taking too long)
                checkoutThread.interrupt(); // Try to interrupt it
                fail("Checkout operation timed out - payment service is taking too long");
            }
        } catch (InterruptedException e) {
            fail("Test was interrupted");
        }
        
        // If we got here, check to see if the checkout completed
        Response<Boolean> response = responseHolder[0];
        if (response != null) {
            // The checkout completed (probably with an error)
            assertTrue("Should have error due to payment service issues", response.errorOccurred());
        }
        
        // Verify that the inventory was rolled back (item quantity not decreased)
        Item item = itemFacade.getItem(STORE_ID, PRODUCT_ID);
        assertEquals("Item quantity should be unchanged after failed checkout", 5, item.getAmount());
    }

    @Test
    public void testConcurrentCheckouts_WithPaymentServiceFailure() throws InterruptedException {
        // Create a payment service that fails for every other transaction
        final int[] callCount = {0};
        IPaymentService unreliablePaymentService = new IPaymentService() {
            @Override
            public void updatePaymentServiceURL(String url) {
                // Do nothing
            }

            @Override
            public Response<Boolean> processPayment(String card_owner, String card_number, Date expiry_date, String cvv, 
                                                double price, long andIncrement, String name, String deliveryAddress) {
                callCount[0]++;
                
                if (callCount[0] % 2 == 0) {
                    // Every even-numbered call fails
                    return Response.error("Payment service intermittently unavailable");
                } else {
                    // Odd-numbered calls succeed
                    return Response.success(true);
                }
            }

            @Override
            public void initialize() {
                // Do nothing
            }
        };

        // Create two client tokens
        String clientId1 = tokenService.generateToken(UUID.randomUUID().toString());
        String clientId2 = tokenService.generateToken(UUID.randomUUID().toString());
        
        // Add same product to both clients' carts
        shoppingService.addProductToCart(STORE_ID, clientId1, PRODUCT_ID, 1);
        shoppingService.addProductToCart(STORE_ID, clientId2, PRODUCT_ID, 1);
        
        // Replace the mocked payment service with our unreliable one
        ShoppingService shoppingServiceWithUnreliablePayment = new ShoppingService(
            cartRepository,
            basketRepository,
            itemFacade,
            storeFacade,
            receiptRepository,
            productRepository,
            tokenService
        );
        
        // Use reflection to set the unreliable payment service
        try {
            java.lang.reflect.Field cartFacadeField = ShoppingService.class.getDeclaredField("cartFacade");
            cartFacadeField.setAccessible(true);
            Object cartFacade = cartFacadeField.get(shoppingServiceWithUnreliablePayment);
            
            java.lang.reflect.Field paymentServiceField = cartFacade.getClass().getDeclaredField("paymentService");
            paymentServiceField.setAccessible(true);
            paymentServiceField.set(cartFacade, unreliablePaymentService);
        } catch (Exception e) {
            fail("Failed to set unreliable payment service: " + e.getMessage());
        }
        
        // Standard checkout parameters
        final String cardNumber = "1234567890123456";
        final Date expiryDate = new Date();
        final String cvv = "123";
        final long transactionId = 12345L;
        final String clientName = "Test Client";
        final String deliveryAddress = "123 Test St";
        
        // Track the results for each thread
        final boolean[] threadSuccess = new boolean[2];
        final String[] threadErrors = new String[2];
        
        // Create two threads, each attempting to checkout
        Thread thread1 = new Thread(() -> {
            Response<Boolean> response = shoppingServiceWithUnreliablePayment.checkout(
                clientId1, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
            threadSuccess[0] = !response.errorOccurred();
            if (response.errorOccurred()) {
                threadErrors[0] = response.getErrorMessage();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            Response<Boolean> response = shoppingServiceWithUnreliablePayment.checkout(
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
        
        // Verify that exactly one thread succeeded and one failed (since our payment service fails every other call)
        assertTrue("One thread should succeed and one should fail",
                (threadSuccess[0] && !threadSuccess[1]) || (!threadSuccess[0] && threadSuccess[1]));
        
        // The failed thread should have a payment-related error
        if (!threadSuccess[0]) {
            assertNotNull("Thread 1 should have an error message", threadErrors[0]);
            assertTrue("Thread 1 error should mention payment issue", 
                    threadErrors[0].toLowerCase().contains("payment"));
        }
        
        if (!threadSuccess[1]) {
            assertNotNull("Thread 2 should have an error message", threadErrors[1]);
            assertTrue("Thread 2 error should mention payment issue", 
                    threadErrors[1].toLowerCase().contains("payment"));
        }
        
        // Check how many successful transactions were made (should be 1)
        // We can verify by checking how much the item's stock decreased
        Item updatedItem = itemFacade.getItem(STORE_ID, PRODUCT_ID);
        assertEquals("Item quantity should decrease by 1 (only one successful transaction)", 
                    4, updatedItem.getAmount());
    }
}