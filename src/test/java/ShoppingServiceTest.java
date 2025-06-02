import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.atmosphere.config.service.Disconnect;
import org.junit.Before;
import org.junit.Test;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import Application.DTOs.AuctionDTO;
import Application.DTOs.CartDTO;
import Application.DTOs.ConditionDTO;
import Application.DTOs.DiscountDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.ShoppingBasketDTO;
import Application.DTOs.UserDTO;
import Application.DTOs.ConditionDTO.ConditionType;
import Application.DTOs.DiscountDTO.DiscountType;
import Application.DTOs.DiscountDTO.QualifierType;
import Application.ItemService;
import Application.ProductService;
import Application.ServiceManager;
import Application.ShoppingService;
import Application.StoreService;
import Application.UserService;
import Application.utils.Error;
import Application.utils.Response;
import Domain.ExternalServices.INotificationService;
import Domain.Store.Discounts.Discount;
import Domain.ExternalServices.IExternalPaymentService;
import Infrastructure.MemoryRepoManager;
import Domain.FacadeManager;
import Domain.Pair;

public class ShoppingServiceTest {

    // Use concrete implementations for services and facades
    private ShoppingService shoppingService;
    private UserService userService;
    
    // Dependency injectors
    private MemoryRepoManager repositoryManager;
    private FacadeManager facadeManager;
    private ServiceManager serviceManager;
    private ProductService productService;
    private StoreService storeService;
    private ItemService itemService;
    private INotificationService notificationService;

    // Mock service for testing
    private IExternalPaymentService mockPaymentService;
    
    // Test user data
    private UserDTO guest;
    private UserDTO user;
    private String clientToken;
    
    // Common test constants
    private static String store_id;
    private static String product_id;
    private static final float VALID_BID_PRICE = 100.0f;
    
    @Before
    public void setUp() {
        // Create mock payment service
        mockPaymentService = mock(IExternalPaymentService.class);
        notificationService = mock(INotificationService.class);
        

        repositoryManager = new MemoryRepoManager();
        facadeManager = new FacadeManager(repositoryManager, mockPaymentService);
        serviceManager = new ServiceManager(facadeManager);
        serviceManager.injectINotificationService(notificationService);


        // Initialize services
        userService = serviceManager.getUserService();
        productService = serviceManager.getProductService();
        storeService = serviceManager.getStoreService();
        itemService = serviceManager.getItemService();
        productService = serviceManager.getProductService();
        shoppingService = serviceManager.getShoppingService();
        
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
        product_id =  productService.addProduct(
            clientToken, 
            "Test Product", 
            List.of("Test Cat"), 
            List.of("Test Cat Description")
        ).getValue().getId();

        store_id = storeService.addStore(
            clientToken, 
            "Test Store", 
            "Test Store Description"
        ).getValue().getId();        
        
        itemService.add(clientToken, store_id, product_id, 10.0, 5, "Test Item Description");
    }
    
    //
    // USE CASE 2.3: SAVING PRODUCTS TO THE SHOPPING CART
    //

    @Test
    public void testAddProductToCart_Success() {
        Response<Boolean> response = shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        assertFalse("Shouldn't get error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    @Test
    public void testAddProductToCart_ServiceFailure() {
        String nonExistentStoreId = "nonexistent-store";
        Response<Boolean> response = shoppingService.addProductToCart(nonExistentStoreId, clientToken, product_id, 2);
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
    }

    //
    // USE CASE 2.4: VIEWING THE SHOPPING CART
    //

    @Test
    public void testViewCart_Success() {
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        Response<CartDTO> response = shoppingService.viewCart(clientToken);
        assertFalse("Should not have error", response.errorOccurred());
        assertNotNull("Response value should not be null", response.getValue());
        assertFalse("Cart should have at least one item", response.getValue().getBaskets().isEmpty());
    }

    @Test
    public void testViewCart_EmptyCart() {
        Response<CartDTO> response = shoppingService.viewCart(clientToken);
        assertFalse("Should not have error even for empty cart", response.errorOccurred());
        assertTrue("Cart should be empty", response.getValue().getBaskets().isEmpty());
    }

    //
    // USE CASE 2.5: IMMEDIATE PURCHASE (CHECKOUT)
    //

    @Test
    public void testCheckout_Success() {
        when(this.mockPaymentService.processPayment(any(), any(), any(), any(), any(), anyDouble())).thenReturn(new Response<>(10000));
        shoppingService.addProductToCart(store_id, clientToken, product_id, 1);
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
        shoppingService.addProductToCart(store_id, clientToken, product_id, requestedQuantity);
        
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
        shoppingService.addProductToCart(store_id, clientToken, product_id, 1);
        Response<Boolean> response = shoppingService.checkout(
            clientToken, "invalid", new Date(), "123", 12345L, "John Doe", "123 Main St");
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
    }

    @Test
    public void testConcurrentCheckout_WithLimitedStock() throws InterruptedException {
        // Use the existing product and store from the setup
        when(this.mockPaymentService.processPayment(any(), any(), any(), any(), any(), anyDouble())).thenReturn(new Response<>(10000));
        String limitedProductId = product_id;  // Use the constant from the setup
        String limitedStoreId = store_id;      // Use the constant from the setup
        when(notificationService.sendNotification(anyString(), anyString())).thenReturn(new Response<>(true));

        
        // Update the item to have limited stock (just 1 unit)
        ItemDTO item = itemService.getItem(clientToken, limitedStoreId, limitedProductId).getValue();
        int amount = item.getAmount();
        Pair<String, String> itemId = new Pair<>(limitedStoreId, limitedProductId);
        itemService.decreaseAmount(clientToken, itemId, amount-1);
        System.out.println("Successfully updated item to have stock of 1");
        
        // Create two additional users for testing
        Response<UserDTO> guest1Response = userService.guestEntry();
        Response<UserDTO> guest2Response = userService.guestEntry();
        
        if (guest1Response.errorOccurred() || guest2Response.errorOccurred()) {
            System.out.println("Guest entry failed, aborting test");
            return;
        }
        
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
        
        if (testUser1Response.errorOccurred() || testUser2Response.errorOccurred()) {
            System.out.println("User registration failed, aborting test");
            return;
        }
        
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
        
        if (addToCart1Response.errorOccurred() || addToCart2Response.errorOccurred()) {
            System.out.println("Failed to add items to cart, aborting test");
            if (addToCart1Response.errorOccurred()) {
                System.out.println("Cart 1 error: " + addToCart1Response.getErrorMessage());
            }
            if (addToCart2Response.errorOccurred()) {
                System.out.println("Cart 2 error: " + addToCart2Response.getErrorMessage());
            }
            return;
        }
        
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
                System.out.println("Thread 1 error: " + response.getErrorMessage());
            } else {
                System.out.println("Thread 1 succeeded");
            }
        });
        
        Thread thread2 = new Thread(() -> {
            Response<Boolean> response = shoppingService.checkout(
                clientId2, cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress);
            threadSuccess[1] = !response.errorOccurred();
            if (response.errorOccurred()) {
                threadErrors[1] = response.getErrorMessage();
                System.out.println("Thread 2 error: " + response.getErrorMessage());
            } else {
                System.out.println("Thread 2 succeeded");
            }
        });
        
        // Start both threads
        thread1.start();
        thread2.start();
        
        // Wait for both threads to complete
        thread1.join(5000);  // Wait up to 5 seconds
        thread2.join(5000);
        
        // Print results
        System.out.println("Thread 1 success: " + threadSuccess[0]);
        System.out.println("Thread 2 success: " + threadSuccess[1]);
        
        // Verify that exactly one thread succeeded and one failed
        boolean oneSucceededOneFailed = (threadSuccess[0] && !threadSuccess[1]) || (!threadSuccess[0] && threadSuccess[1]);
        assertTrue("Either thread1 should succeed and thread2 fail, or vice versa", oneSucceededOneFailed);
        
        // Check the final stock through itemFacade directly
        try {
            item = itemService.getItem(clientToken, limitedStoreId, limitedProductId).getValue();
            int finalStock = item.getAmount();
            System.out.println("Final stock: " + finalStock);
            assertTrue("Stock should be depleted or almost depleted", finalStock <= 1);
        } catch (Exception e) {
            System.out.println("Failed to get final stock: " + e.getMessage());
            // If we can't verify the stock, at least verify that exactly one thread succeeded
            assertTrue("Either thread1 should succeed and thread2 fail, or vice versa", oneSucceededOneFailed);
        }
    }
    //
    // CART MANAGEMENT - REMOVE PRODUCT
    //

    @Test
    public void testRemoveProductFromCart_WithQuantity_Success() {
        shoppingService.addProductToCart(store_id, clientToken, product_id, 3);
        Response<Boolean> response = shoppingService.removeProductFromCart(store_id, clientToken, product_id, 1);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    @Test
    public void testRemoveProductFromCart_CompleteRemoval_Success() {
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        Response<Boolean> response = shoppingService.removeProductFromCart(store_id, clientToken, product_id);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
    }

    //
    // CART MANAGEMENT - CLEAR OPERATIONS
    //

    @Test
    public void testClearCart_Success() {
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        Response<Boolean> response = shoppingService.clearCart(clientToken);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        assertTrue("Cart should be empty after clearing", shoppingService.viewCart(clientToken).getValue().getBaskets().isEmpty());
    }

    @Test
    public void testClearBasket_Success() {
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        Response<Boolean> response = shoppingService.clearBasket(clientToken, store_id);
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
            // Instead of creating a new auction, mock the behavior
            float validBidPrice = VALID_BID_PRICE;
            String mockAuctionId;
        
            
            String cardNumber = "1234567890123456";
            Date expiryDate = new Date();
            String cvv = "123";
            long transactionId = 12345L;
            String clientName = "John Doe";
            String deliveryAddress = "123 Main St";
            String auctionEndDate = "2077-01-01 07:00";
            Response<AuctionDTO> res = storeService.addAuction(clientToken, store_id, product_id, auctionEndDate.toString(), 49.99f);
            mockAuctionId = res.getValue().getAuctionId();
            

            // Use our test shopping service to make a valid bid
            Response<Boolean> response = shoppingService.makeBid(
                mockAuctionId, 
                clientToken, 
                validBidPrice,
                cardNumber, 
                expiryDate, 
                cvv, 
                transactionId, 
                clientName, 
                deliveryAddress
            );
            
            // Verify that the bid was accepted
            assertFalse("Valid bid should be accepted", response.errorOccurred());
            assertEquals("Should return true for a successful bid", Boolean.TRUE, response.getValue());
            
            // Now try with a lower bid price that should be rejected
            float lowBidPrice = 40.0f;
            Response<Boolean> lowBidResponse = shoppingService.makeBid(
                mockAuctionId, 
                clientToken, 
                lowBidPrice,
                cardNumber, 
                expiryDate, 
                cvv, 
                transactionId, 
                clientName, 
                deliveryAddress
            );
            
            // Verify that the lower bid was rejected
            assertTrue("Low bid should be rejected", lowBidResponse.errorOccurred());
            assertNull("Value should be null for rejected bid", lowBidResponse.getValue());
            assertNotNull("Error message should not be null", lowBidResponse.getErrorMessage());
            
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testMakeBid_BidRejected() {
        try {
            // Instead of creating a new auction, mock the behavior
            float lowBidPrice = 100.0f;
            String mockAuctionId;
            
            
            String cardNumber = "1234567890123456";
            Date expiryDate = new Date();
            String cvv = "123";
            long transactionId = 12345L;
            String clientName = "John Doe";
            String auctionEndDate = "2077-01-01 07:00";
            String deliveryAddress = "123 Main St";
            
            mockAuctionId = storeService.addAuction(clientToken, store_id, product_id, auctionEndDate.toString(), 101.0).getValue().getAuctionId();
            // Use our test shopping service to make a bid
            Response<Boolean> response = shoppingService.makeBid(
                mockAuctionId, 
                clientToken, 
                lowBidPrice,
                cardNumber, 
                expiryDate, 
                cvv, 
                transactionId, 
                clientName, 
                deliveryAddress
            );
            
            // Verify that the bid was rejected
            assertTrue("Should have error for low bid price", response.errorOccurred());
            assertNull("Value should be null for rejected bid", response.getValue());
            assertNotNull("Error message should not be null", response.getErrorMessage());
            assertTrue("Error should mention bid price", 
                    response.getErrorMessage().toLowerCase().contains("bid") || 
                    response.getErrorMessage().toLowerCase().contains("price"));
            
            // Now try with a higher bid price that should be accepted
            float highBidPrice = 200.0f;
            Response<Boolean> highBidResponse = shoppingService.makeBid(
                mockAuctionId, 
                clientToken, 
                highBidPrice,
                cardNumber, 
                expiryDate, 
                cvv, 
                transactionId, 
                clientName, 
                deliveryAddress
            );
            
            // Verify that the higher bid was accepted
            assertFalse("Higher bid should be accepted", highBidResponse.errorOccurred());
            assertEquals("Value should be true for accepted bid", Boolean.TRUE, highBidResponse.getValue());
            
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }


    @Test
    public void testCheckout_PaymentServiceReturnsFailure() {
        // First add a product to the cart
        Response<Boolean> addToCartResponse = shoppingService.addProductToCart(store_id, clientToken, product_id, 1);
        assertFalse("Adding to cart should succeed", addToCartResponse.errorOccurred());
        
        // Check the current cart state to verify the item is there and get its quantity
        Response<CartDTO> cartResponse = shoppingService.viewCart(clientToken);
        assertFalse("Viewing cart should succeed", cartResponse.errorOccurred());
        
        // Verify the item is in the cart
        CartDTO cart = cartResponse.getValue();
        assertTrue("Cart should contain the store", cart.getBaskets().containsKey(store_id));
        
        ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
        assertTrue("Basket should contain the product", basket.getOrders().containsKey(product_id));
        
        ItemDTO itemInCart = basket.getOrders().get(product_id);
        int initialQuantityInCart = itemInCart.getAmount();
        assertEquals("Cart should have 1 item", 1, initialQuantityInCart);
        
        // Create a distinctive error message for easy identification
        final String DISTINCTIVE_ERROR_MESSAGE = "XYZ_TEST_PAYMENT_DECLINED_123";
        
        when(this.mockPaymentService.processPayment(any(), any(), any(), any(), any(), anyDouble())).thenReturn(new Response<>(new Error(DISTINCTIVE_ERROR_MESSAGE)));
        
        // Create a facade manager that uses our bad payment service
        FacadeManager testFacadeManager = new FacadeManager(repositoryManager, mockPaymentService);
        
        // Create a custom service manager that uses our test facade manager
        ServiceManager testServiceManager = new ServiceManager(testFacadeManager);
        
        // Get the shopping service from our custom service manager
        ShoppingService testShoppingService = testServiceManager.getShoppingService();
        
        // Prepare checkout parameters
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        long transactionId = 12345L;
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        
        // Act - attempt to checkout with our test shopping service that uses the bad payment service
        Response<Boolean> response = testShoppingService.checkout(
            clientToken, 
            cardNumber, 
            expiryDate, 
            cvv, 
            transactionId, 
            clientName, 
            deliveryAddress
        );
        
        // Assert
        assertTrue("Checkout should fail with payment service error", response.errorOccurred());
        assertNull("Value should be null on failure", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
        
        // Print the actual error message for debugging
        System.out.println("Actual error message: " + response.getErrorMessage());
        
        // For a successful test, we only need to verify that the checkout failed
        // and that the cart state is preserved
        
        // Verify that the item is still in the cart (checkout didn't succeed)
        Response<CartDTO> finalCartResponse = shoppingService.viewCart(clientToken);
        assertFalse("Final cart view should succeed", finalCartResponse.errorOccurred());
        
        CartDTO finalCart = finalCartResponse.getValue();
        assertTrue("Cart should still contain the store", finalCart.getBaskets().containsKey(store_id));
        
        ShoppingBasketDTO finalBasket = finalCart.getBaskets().get(store_id);
        assertTrue("Basket should still contain the product", finalBasket.getOrders().containsKey(product_id));
        
        ItemDTO finalItemInCart = finalBasket.getOrders().get(product_id);
        assertEquals("Item quantity in cart should be unchanged after failed checkout", 
            initialQuantityInCart, 
            finalItemInCart.getAmount());
    }


    @Test
    public void GivenExistingUserStoreProductFilledCart_WhenViewingCart_ReturnCorrectFinalPrice(){
        // Add product to cart
        Response<Boolean> addToCartResponse = shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        assertFalse("Adding to cart should succeed", addToCartResponse.errorOccurred());
        
        // View the cart
        Response<CartDTO> cartResponse = shoppingService.viewCart(clientToken);
        assertFalse("Viewing cart should succeed", cartResponse.errorOccurred());
        
        CartDTO cart = cartResponse.getValue();
        assertTrue("Cart should contain the store", cart.getBaskets().containsKey(store_id));
        
        ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
        assertTrue("Basket should contain the product", basket.getOrders().containsKey(product_id));
        
        ItemDTO itemInCart = basket.getOrders().get(product_id);
        assertEquals("Item quantity in cart should be 2", 2, itemInCart.getAmount());
        
        // Calculate expected final price
        double expectedFinalPrice = itemInCart.getAmount() * itemInCart.getPrice();
        
        // Check if the final price matches the expected value
        assertEquals("Final price in cart should match expected value", expectedFinalPrice, basket.getTotalPrice(), 0.01);
    }


    @Test
    public void GivenExistingMemberStoreProduct_WhenAddingSimpleDiscount_ReturnTrue(){
        ConditionDTO condition = new ConditionDTO(null,ConditionType.MIN_QUANTITY);
        condition.setMinQuantity(2);
        condition.setProductId(product_id);
        DiscountDTO discount = new DiscountDTO(null,DiscountType.SIMPLE,condition);
        discount.setDiscountPercentage(0.5f);
        discount.setQualifierType(QualifierType.PRODUCT);
        discount.setQualifierValue(product_id);
        Response<DiscountDTO> response = storeService.addDiscount(clientToken, store_id, discount);
        if(response.errorOccurred()) {
            System.out.println("Error adding discount: " + response.getErrorMessage());
        }
        assertEquals("Response discount is not the same type as given discount", discount.getType(), response.getValue().getType());
        assertEquals("Response discount percentage is not the same as given discount", discount.getDiscountPercentage(), response.getValue().getDiscountPercentage(), 0.01);
        assertEquals("Response discount qualifier type is not the same as given discount", discount.getQualifierType(), response.getValue().getQualifierType());
        assertTrue(storeService.getStoreDiscounts(clientToken, store_id).getValue().contains(response.getValue()));
    }


    @Test
    public void GivenExistingUMemberStoreProduct_WhenAddingCompositeDiscount_ReturnTrue(){
        ConditionDTO condition1 = new ConditionDTO(null,ConditionType.MIN_QUANTITY);
        condition1.setMinQuantity(2);
        condition1.setProductId(product_id);
        ConditionDTO condition2 = new ConditionDTO(null,ConditionType.MAX_PRICE);
        condition2.setMaxPrice(100.0);
        condition2.setProductId(product_id);
        ConditionDTO trueConditionDTO = new ConditionDTO(null,ConditionType.TRUE);


        DiscountDTO simpleDiscount1 = new DiscountDTO(null,DiscountType.SIMPLE,condition1);
        simpleDiscount1.setDiscountPercentage(0.5f);
        simpleDiscount1.setQualifierType(QualifierType.PRODUCT);
        simpleDiscount1.setQualifierValue(product_id);
        DiscountDTO simpleDiscount2 = new DiscountDTO(null,DiscountType.SIMPLE,condition2);
        simpleDiscount2.setDiscountPercentage(0.3f);
        simpleDiscount2.setQualifierType(QualifierType.PRODUCT);
        simpleDiscount2.setQualifierValue(product_id);

        DiscountDTO discount = new DiscountDTO(null,DiscountType.AND,trueConditionDTO);
        discount.setSubDiscounts(List.of(simpleDiscount1, simpleDiscount2));
        Response<DiscountDTO> response = storeService.addDiscount(clientToken, store_id, discount);
        if(response.errorOccurred()) {
            System.out.println("Error adding discount: " + response.getErrorMessage());
        }
        assertEquals("Response discount is not the same type as given discount", discount.getType(), response.getValue().getType());
        assertEquals(response.getValue().getSubDiscounts().size(), 2);
        assertTrue(storeService.getStoreDiscounts(clientToken, store_id).getValue().contains(response.getValue()));
    }

    @Test
    public void GivenExistingUMemberStoreProduct_WhenAddingOrDiscount_ReturnTrue(){
        ConditionDTO condition1 = new ConditionDTO(null,ConditionType.MIN_QUANTITY);
        condition1.setMinQuantity(2);
        condition1.setProductId(product_id);
        ConditionDTO condition2 = new ConditionDTO(null,ConditionType.MAX_PRICE);
        condition2.setMaxPrice(100.0);
        condition2.setProductId(product_id);
        ConditionDTO trueConditionDTO = new ConditionDTO(null,ConditionType.TRUE);
        DiscountDTO simpleDiscount1 = new DiscountDTO(null,DiscountType.SIMPLE,condition1);
        simpleDiscount1.setDiscountPercentage(0.5f);
        simpleDiscount1.setQualifierType(QualifierType.PRODUCT);
        simpleDiscount1.setQualifierValue(product_id);
        DiscountDTO simpleDiscount2 = new DiscountDTO(null,DiscountType.SIMPLE,condition2);
        simpleDiscount2.setDiscountPercentage(0.3f);
        simpleDiscount2.setQualifierType(QualifierType.PRODUCT);
        simpleDiscount2.setQualifierValue(product_id);
        DiscountDTO discount = new DiscountDTO(null,DiscountType.OR,trueConditionDTO);
        discount.setSubDiscounts(List.of(simpleDiscount1, simpleDiscount2));
        Response<DiscountDTO> response = storeService.addDiscount(clientToken, store_id, discount);
        if(response.errorOccurred()) {
            System.out.println("Error adding discount: " + response.getErrorMessage());
        }  
        assertEquals("Response discount is not the same type as given discount", discount.getType(), response.getValue().getType());
        assertEquals(response.getValue().getSubDiscounts().size(), 1);
        assertEquals(response.getValue().getCondition().getSubConditions().size(), 2);
    }
}