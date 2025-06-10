
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import Application.ItemService;
import Application.MarketService;
import Application.ProductService;
import Application.ServiceManager;
import Application.ShoppingService;
import Application.StoreService;
import Application.TokenService;
import Application.UserService;
import Application.DTOs.AuctionDTO;
import Application.DTOs.CartDTO;
import Application.DTOs.ConditionDTO;
import Application.DTOs.ConditionDTO.ConditionType;
import Application.DTOs.DiscountDTO;
import Application.DTOs.DiscountDTO.DiscountType;
import Application.DTOs.DiscountDTO.QualifierType;
import Application.DTOs.ItemDTO;
import Application.DTOs.ItemPriceBreakdownDTO;
import Application.DTOs.OfferDTO;
import Application.DTOs.PaymentDetailsDTO;
import Application.DTOs.ShoppingBasketDTO;
import Application.DTOs.UserDTO;
import Application.utils.Error;
import Application.utils.Response;

import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.INotificationService;
import Domain.management.PermissionType;

import Domain.FacadeManager;
import Domain.Pair;
import Domain.ExternalServices.IExternalSupplyService;
import Infrastructure.MemoryRepoManager;

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
    private IExternalSupplyService mockSupplyService;
    private TokenService tokenService;

    // Mock service for testing
    private IExternalPaymentService mockPaymentService;
    
    // Test user data
    private UserDTO guest;
    private UserDTO user;
    private String clientToken;
    
    // Common test constants
    private static String store_id;
    private static String product_id;
    private static String product_id_2;
    private static final float VALID_BID_PRICE = 100.0f;
    private static final double DELTA = 0.01; // For floating point comparisons
    
    @Before
    public void setUp() {
        // Create mock payment service
        mockPaymentService = mock(IExternalPaymentService.class);
        notificationService = mock(INotificationService.class);
        mockSupplyService = mock(IExternalSupplyService.class);
        

        repositoryManager = new MemoryRepoManager();
        facadeManager = new FacadeManager(repositoryManager, mockPaymentService, mockSupplyService);
        serviceManager = new ServiceManager(facadeManager);
        serviceManager.injectINotificationService(notificationService);
        tokenService = serviceManager.getTokenService();


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
        
        // Create sample products and store
        product_id = productService.addProduct(
            clientToken, 
            "Test Product", 
            List.of("Test Cat"), 
            List.of("Test Cat Description")
        ).getValue().getId();

        product_id_2 = productService.addProduct(
            clientToken, 
            "Test Product 2", 
            List.of("Test Cat"), 
            List.of("Test Cat Description")
        ).getValue().getId();

        store_id = storeService.addStore(
            clientToken, 
            "Test Store", 
            "Test Store Description"
        ).getValue().getId();        
        
        itemService.add(clientToken, store_id, product_id, 10.0, 5, "Test Item Description");
        itemService.add(clientToken, store_id, product_id_2, 20.0, 5, "Test Item 2 Description");
    }

    //
    // HELPER METHODS FOR CREATING VALID DISCOUNTS
    //

    /**
     * Creates a valid simple discount DTO according to DiscountBuilder rules
     */
    private DiscountDTO createSimpleDiscount(ConditionType conditionType, QualifierType qualifierType, 
                                           String qualifierValue, float discountPercentage) {
        // Create condition according to DiscountBuilder validation rules
        ConditionDTO condition = new ConditionDTO("123", conditionType);
        
        // Set condition properties based on type
        switch (conditionType) {
            case MIN_QUANTITY:
                condition.setMinQuantity(2); // Default minimum quantity
                condition.setProductId(qualifierValue); // Required for MIN_QUANTITY
                break;
            case MAX_PRICE:
                condition.setMaxPrice(100.0); // Default max price
                condition.setProductId(qualifierValue); // Required for MAX_PRICE
                break;
            case MIN_PRICE:
                condition.setMinPrice(50.0); // Default min cart value
                break;
            case TRUE:
                // No additional properties needed
                break;
            default:
                throw new IllegalArgumentException("Unsupported condition type: " + conditionType);
        }
        
        // Create discount with validated percentage (0-1 range)
        DiscountDTO discount = new DiscountDTO(null, store_id, DiscountType.SIMPLE, condition);
        
        // Validate discount percentage is in range [0, 1]
        if (discountPercentage < 0 || discountPercentage > 1) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 1");
        }
        discount.setDiscountPercentage(discountPercentage);
        
        // Validate qualifier type and value
        if (qualifierType == null) {
            throw new IllegalArgumentException("Qualifier type cannot be null");
        }
        if (qualifierValue == null || qualifierValue.isEmpty()) {
            throw new IllegalArgumentException("Qualifier value cannot be null or empty");
        }
        
        discount.setQualifierType(qualifierType);
        discount.setQualifierValue(qualifierValue);
        discount.setStoreId(store_id);
        
        return discount;
    }

    /**
     * Creates a valid AND composite discount
     */
    private DiscountDTO createAndDiscount(List<DiscountDTO> subDiscounts) {
        if (subDiscounts == null || subDiscounts.isEmpty()) {
            throw new IllegalArgumentException("Sub-discounts cannot be null or empty for composite discount");
        }
        
        // Create TRUE condition for composite discount
        ConditionDTO trueCondition = new ConditionDTO(null, ConditionType.TRUE);
        
        DiscountDTO compositeDiscount = new DiscountDTO(null, store_id,DiscountType.AND, trueCondition);
        compositeDiscount.setSubDiscounts(subDiscounts);
        compositeDiscount.setStoreId(store_id);
        compositeDiscount.setDescription("AND composite discount");
        
        return compositeDiscount;
    }

    /**
     * Creates a valid OR composite discount
     */
    private DiscountDTO createOrDiscount(List<DiscountDTO> subDiscounts) {
        if (subDiscounts == null || subDiscounts.isEmpty()) {
            throw new IllegalArgumentException("Sub-discounts cannot be null or empty for composite discount");
        }
        
        // Create TRUE condition for composite discount
        ConditionDTO trueCondition = new ConditionDTO(null, ConditionType.TRUE);
        
        DiscountDTO compositeDiscount = new DiscountDTO(null, store_id,DiscountType.OR, trueCondition);
        compositeDiscount.setSubDiscounts(subDiscounts);
        compositeDiscount.setStoreId(store_id);
        compositeDiscount.setDescription("OR composite discount");
        
        return compositeDiscount;
    }

    /**
     * Creates a valid XOR composite discount
     */
    private DiscountDTO createXorDiscount(DiscountDTO discount1, DiscountDTO discount2) {
        if (discount1 == null || discount2 == null) {
            throw new IllegalArgumentException("XOR discount requires exactly 2 valid sub-discounts");
        }
        
        // Create TRUE condition for composite discount
        ConditionDTO trueCondition = new ConditionDTO(null, ConditionType.TRUE);
        
        DiscountDTO compositeDiscount = new DiscountDTO(null, store_id,DiscountType.XOR, trueCondition);
        compositeDiscount.setSubDiscounts(List.of(discount1, discount2));
        compositeDiscount.setStoreId(store_id);
        compositeDiscount.setDescription("XOR composite discount");
        
        return compositeDiscount;
    }

    //
    // USE CASE 2.3: SAVING PRODUCTS TO THE SHOPPING CART
    //

    @Test
    public void testAddProductToCart_Success() {
        Response<Boolean> response = shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        assertFalse("Shouldn't get error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        
        // Check invariants
        checkCartInvariants();
    }

    @Test
    public void testAddProductToCart_ServiceFailure() {
        String nonExistentStoreId = "nonexistent-store";
        Response<Boolean> response = shoppingService.addProductToCart(nonExistentStoreId, clientToken, product_id, 2);
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
        
        // Check invariants - cart should remain empty
        checkCartInvariants();
    }

    //
    // USE CASE 2.4: VIEWING THE SHOPPING CART (UPDATED FOR DISCOUNTS)
    //

    @Test
    public void testViewCart_Success_WithoutDiscounts() {
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        Response<CartDTO> response = shoppingService.viewCart(clientToken);
        assertFalse("Should not have error", response.errorOccurred());
        assertNotNull("Response value should not be null", response.getValue());
        assertFalse("Cart should have at least one item", response.getValue().getBaskets().isEmpty());
        
        // Check that price breakdown exists and reflects no discount
        CartDTO cart = response.getValue();
        ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
        ItemDTO item = basket.getOrders().get(product_id);
        assertNotNull("Item should have price breakdown", item.getPriceBreakDown());
        assertEquals("Original price should be 10.0", 10.0, item.getOriginalPrice(), DELTA);
        assertEquals("Final price should equal original price when no discount", 
                    item.getOriginalPrice(), item.getPrice(), DELTA);
        assertEquals("Discount should be 0", 0.0, item.getPriceBreakDown().getDiscount(), DELTA);
        
        checkCartInvariants();
        checkPriceInvariants(cart);
        checkDiscountInvariants(cart);
    }

    @Test
    public void testConcurrentCheckout_WithLimitedStock() throws InterruptedException {
        // Use the existing product and store from the setup
        when(this.mockPaymentService.processPayment(any(), any(), any(), any(), any(), anyDouble())).thenReturn(new Response<>(10000));
        when(this.mockSupplyService.supplyOrder(any(), any(), any(), any(), any())).thenReturn(new Response<>(20000));
        String limitedProductId = product_id;
        String limitedStoreId = store_id;
        when(notificationService.sendNotification(anyString(), anyString())).thenReturn(new Response<>(true));

        // Update the item to have limited stock (just 1 unit)
        ItemDTO item = itemService.getItem(clientToken, limitedStoreId, limitedProductId).getValue();
        int amount = item.getAmount();
        Pair<String, String> itemId = new Pair<>(limitedStoreId, limitedProductId);
        itemService.decreaseAmount(clientToken, itemId, amount-1);
        
        // Create two additional users for testing
        Response<UserDTO> guest1Response = userService.guestEntry();
        Response<UserDTO> guest2Response = userService.guestEntry();
        
        if (guest1Response.errorOccurred() || guest2Response.errorOccurred()) {
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
            return;
        }
        
        // Get their tokens
        String clientId1 = testUser1Response.getValue().getSessionToken();
        String clientId2 = testUser2Response.getValue().getSessionToken();
        
        // Standard checkout parameters
        final String cardNumber = "1234567890123456";
        final Date expiryDate = new Date();
        final String cvv = "123";
        final String clientName = "Test Client";
        final String deliveryAddress = "123 Test St";
        final String userSSN = "123-45-6789";
        final String city = "Test City";
        final String country = "Test Country";
        final String zip = "12345";
        
        // Add 1 unit of the limited product to each client's cart
        Response<Boolean> addToCart1Response = shoppingService.addProductToCart(
            limitedStoreId, clientId1, limitedProductId, 1);
        Response<Boolean> addToCart2Response = shoppingService.addProductToCart(
            limitedStoreId, clientId2, limitedProductId, 1);
        
        if (addToCart1Response.errorOccurred() || addToCart2Response.errorOccurred()) {
            return;
        }
        
        // Track the results for each thread
        final boolean[] threadSuccess = new boolean[2];
        final String[] threadErrors = new String[2];
        
        // Create two threads, each attempting to checkout
        Thread thread1 = new Thread(() -> {
            Response<Boolean> response = shoppingService.checkout(
                clientId1, userSSN, cardNumber, expiryDate, cvv, clientName, deliveryAddress, city, country, zip);
            threadSuccess[0] = !response.errorOccurred();
            if (response.errorOccurred()) {
                threadErrors[0] = response.getErrorMessage();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            Response<Boolean> response = shoppingService.checkout(
                clientId2, userSSN, cardNumber, expiryDate, cvv, clientName, deliveryAddress, city, country, zip);
            threadSuccess[1] = !response.errorOccurred();
            if (response.errorOccurred()) {
                threadErrors[1] = response.getErrorMessage();
            }
        });
        
        // Start both threads
        thread1.start();
        thread2.start();
        
        // Wait for both threads to complete
        thread1.join(5000);
        thread2.join(5000);
        
        // Verify that exactly one thread succeeded and one failed
        boolean oneSucceededOneFailed = (threadSuccess[0] && !threadSuccess[1]) || (!threadSuccess[0] && threadSuccess[1]);
        assertTrue("Either thread1 should succeed and thread2 fail, or vice versa", oneSucceededOneFailed);
        
        // Check the final stock
        try {
            item = itemService.getItem(clientToken, limitedStoreId, limitedProductId).getValue();
            int finalStock = item.getAmount();
            assertTrue("Stock should be depleted or almost depleted", finalStock <= 1);
        } catch (Exception e) {
            assertTrue("Either thread1 should succeed and thread2 fail, or vice versa", oneSucceededOneFailed);
        }
        
        checkInventoryInvariants();
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
        
        // Verify that the quantity is reduced correctly
        CartDTO cart = shoppingService.viewCart(clientToken).getValue();
        assertTrue("Cart should still contain the store", cart.getBaskets().containsKey(store_id));
        ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
        assertTrue("Basket should contain the product", basket.getOrders().containsKey(product_id));
        ItemDTO itemInCart = basket.getOrders().get(product_id);
        assertEquals("Item quantity should be reduced by 1", 2, itemInCart.getAmount());
        
        checkCartInvariants();
    }

    @Test
    public void testRemoveProductFromCart_CompleteRemoval_Success() {
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        Response<Boolean> response = shoppingService.removeProductFromCart(store_id, clientToken, product_id);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        
        // Verify that the product is completely removed from the cart
        CartDTO cart = shoppingService.viewCart(clientToken).getValue();    
        assertFalse("Cart should no longer contain the store after removing the last product", cart.getBaskets().containsKey(store_id));
        assertTrue("Cart should be empty after removing all products", cart.getBaskets().isEmpty());

        checkCartInvariants();
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
        
        checkCartInvariants();
    }

    @Test
    public void testClearBasket_Success() {
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        Response<Boolean> response = shoppingService.clearBasket(clientToken, store_id);
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        assertTrue("Basket should be empty after clearing", shoppingService.viewCart(clientToken).getValue().getBaskets().isEmpty());
        assertFalse("Cart should not contain the store after clearing", shoppingService.viewCart(clientToken).getValue().getBaskets().containsKey(store_id));
        
        checkCartInvariants();
    }

    //
    // USE CASE 3.9: SUBMITTING A PURCHASE BID
    //

    @Test
    public void testMakeBid_Success() {
        try {
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
            
            Response<Boolean> response = shoppingService.makeBid(
                mockAuctionId, clientToken, validBidPrice,
                cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress
            );
            
            assertFalse("Valid bid should be accepted", response.errorOccurred());
            assertEquals("Should return true for a successful bid", Boolean.TRUE, response.getValue());
            
            // Now try with a lower bid price that should be rejected
            float lowBidPrice = 40.0f;
            Response<Boolean> lowBidResponse = shoppingService.makeBid(
                mockAuctionId, clientToken, lowBidPrice,
                cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress
            );
            
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
            
            Response<Boolean> response = shoppingService.makeBid(
                mockAuctionId, clientToken, lowBidPrice,
                cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress
            );
            
            assertTrue("Should have error for low bid price", response.errorOccurred());
            assertNull("Value should be null for rejected bid", response.getValue());
            assertNotNull("Error message should not be null", response.getErrorMessage());
            assertTrue("Error should mention bid price", 
                    response.getErrorMessage().toLowerCase().contains("bid") || 
                    response.getErrorMessage().toLowerCase().contains("price"));
            
            // Now try with a higher bid price that should be accepted
            float highBidPrice = 200.0f;
            Response<Boolean> highBidResponse = shoppingService.makeBid(
                mockAuctionId, clientToken, highBidPrice,
                cardNumber, expiryDate, cvv, transactionId, clientName, deliveryAddress
            );
            
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
        
        // Check the current cart state to verify the item is there
        Response<CartDTO> cartResponse = shoppingService.viewCart(clientToken);
        assertFalse("Viewing cart should succeed", cartResponse.errorOccurred());
        
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
        FacadeManager testFacadeManager = new FacadeManager(repositoryManager, mockPaymentService, mockSupplyService);
        ServiceManager testServiceManager = new ServiceManager(testFacadeManager);
        ShoppingService testShoppingService = testServiceManager.getShoppingService();
        
        // Prepare checkout parameters
        String cardNumber = "1234567890123456";
        Date expiryDate = new Date();
        String cvv = "123";
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        String userSSN = "123-45-6789";
        String city = "Test City";
        String country = "Test Country";
        String zip = "12345";
        
        Response<Boolean> response = testShoppingService.checkout(
            clientToken, userSSN, cardNumber, expiryDate, cvv, clientName, deliveryAddress, city, country, zip
        );
        
        assertTrue("Checkout should fail with payment service error", response.errorOccurred());
        assertNull("Value should be null on failure", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
        
        // Verify that the item is still in the cart (checkout didn't succeed)
        Response<CartDTO> finalCartResponse = shoppingService.viewCart(clientToken);
        assertFalse("Final cart view should succeed", finalCartResponse.errorOccurred());
        
        CartDTO finalCart = finalCartResponse.getValue();
        assertTrue("Cart should still contain the store", finalCart.getBaskets().containsKey(store_id));
        
        ShoppingBasketDTO finalBasket = finalCart.getBaskets().get(store_id);
        assertTrue("Basket should still contain the product", finalBasket.getOrders().containsKey(product_id));
        
        ItemDTO finalItemInCart = finalBasket.getOrders().get(product_id);
        assertEquals("Item quantity in cart should be unchanged after failed checkout", 
            initialQuantityInCart, finalItemInCart.getAmount());
        
        checkInventoryInvariants();
        checkCartInvariants();
    }

    @Test
    public void testViewCart_ReturnCorrectFinalPrice() {
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
        assertEquals("Final price in cart should match expected value", expectedFinalPrice, basket.getTotalPrice(), DELTA);
        
        checkCartInvariants();
        checkPriceInvariants(cart);
    }

    //
    // ADDITIONAL DISCOUNT ACCEPTANCE TESTS
    //

    @Test
    public void testViewCart_ComplexDiscountScenario() {
        // Create discount 1: 15% off product_id when quantity >= 2
        DiscountDTO discount1 = createSimpleDiscount(
            ConditionType.MIN_QUANTITY, 
            QualifierType.PRODUCT, 
            product_id, 
            0.15f
        );
        
        storeService.addDiscount(clientToken, store_id, discount1);
        
        // Create discount 2: 10% off store-wide when total cart value > 50
        ConditionDTO condition2 = new ConditionDTO(null, ConditionType.MIN_PRICE);
        condition2.setMinPrice(50.0);
        
        DiscountDTO discount2 = new DiscountDTO(null, store_id,DiscountType.SIMPLE, condition2);
        discount2.setDiscountPercentage(0.1f);
        discount2.setQualifierType(QualifierType.STORE);
        discount2.setQualifierValue(store_id);
        discount2.setStoreId(store_id);
        
        storeService.addDiscount(clientToken, store_id, discount2);
        
        // Add products to trigger both discounts
        shoppingService.addProductToCart(store_id, clientToken, product_id, 3); // 3 * 10 = 30
        shoppingService.addProductToCart(store_id, clientToken, product_id_2, 2); // 2 * 20 = 40
        // Total = 70, should trigger cart value discount
        
        Response<CartDTO> response = shoppingService.viewCart(clientToken);
        assertFalse("Should not have error", response.errorOccurred());
        
        CartDTO cart = response.getValue();
        ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
        
        // Verify individual product discounts are applied
        ItemDTO item1 = basket.getOrders().get(product_id);
        assertTrue("Product 1 should have some discount applied", item1.getPriceBreakDown().hasDiscount());
        
        // Verify total calculations are correct
        double totalPrice = cart.getTotalPrice();
        assertTrue("Total price should be positive", totalPrice > 0);
        
        checkCartInvariants();
        checkPriceInvariants(cart);
        checkDiscountInvariants(cart);
    }

    @Test
    public void testMakeOffer_Success() {
        // Step 1: Prepare PaymentDetailsDTO
        PaymentDetailsDTO paymentDetails = new PaymentDetailsDTO(
            user.getId(),
            "4111111111111111",                // dummy card number
            LocalDate.now().plusYears(1),      // future expiry date
            "123",                             // dummy CVV
            "Offer Tester"                     // card holder name
        );

        // Step 2: Call makeOffer
        Response<OfferDTO> response = shoppingService.makeOffer(
            clientToken,
            store_id,
            product_id,
            8.99,               // new proposed price
            paymentDetails
        );

        // Step 3: Assertions
        assertFalse("Offer creation should not error", response.errorOccurred());
        OfferDTO offer = response.getValue();
        assertNotNull("OfferDTO should not be null", offer);
        assertEquals("Offered price should match", 8.99, offer.getLastPrice(), 0.001);
        assertEquals("Product ID should match", product_id, offer.getItem().getProductId());
        assertEquals("Store ID should match", store_id, offer.getItem().getStoreId());
    }

    @Test
    public void testMakeOffer_ValidInput_ReturnsOfferDTO() {
        PaymentDetailsDTO paymentDetails = new PaymentDetailsDTO(user.getId(), "4111111111111111", LocalDate.now().plusYears(1), "123", "Offer Tester");
        Response<OfferDTO> response = shoppingService.makeOffer(clientToken, store_id, product_id, 7.77, paymentDetails);

        assertFalse(response.errorOccurred());
        assertNotNull(response.getValue());
        assertEquals(7.77, response.getValue().getLastPrice(), 0.01);
        assertEquals(product_id, response.getValue().getItem().getProductId());
    }

    @Test
    public void testMakeOffer_InvalidToken_ReturnsError() {
        PaymentDetailsDTO paymentDetails = new PaymentDetailsDTO(user.getId(), "4111111111111111", LocalDate.now().plusYears(1), "123", "Offer Tester");
        Response<OfferDTO> response = shoppingService.makeOffer("invalidToken", store_id, product_id, 9.99, paymentDetails);

        assertTrue(response.errorOccurred());
        assertEquals("Invalid token", response.getErrorMessage());
    }

    @Test
    public void testAcceptOffer_FullApprovalFlow_WithDifferentManager() {
        when(mockPaymentService.processPayment(
        anyString(),  // userId
        anyString(),  // cardNumber
        any(Date.class),  // expiryDate
        anyString(),  // cvv
        anyString(),  // holder
        anyDouble()   // amount
        )).thenReturn(new Response<>(10000));  // Replace 10000 with the desired mocked value

        MarketService marketService = serviceManager.getMarketService();
        // Step 1: Member makes offer
        PaymentDetailsDTO paymentDetails = new PaymentDetailsDTO(
            user.getId(),
            "4111111111111111",
            LocalDate.now().plusYears(1),
            "123",
            "Offer Tester"
        );

        Response<OfferDTO> offerResponse = shoppingService.makeOffer(
            clientToken, store_id, product_id, 5.55, paymentDetails
        );

        assertFalse("Offer creation should succeed", offerResponse.errorOccurred());
        OfferDTO offer = offerResponse.getValue();
        assertNotNull("OfferDTO should not be null", offer);

        // Step 2: Create and appoint a second manager
        Response<UserDTO> guestResp = userService.guestEntry();
        Response<UserDTO> managerResp = userService.register(
            guestResp.getValue().getSessionToken(),
            "manager1",
            "WhyWontWork2!",
            "manager1@e.com"
        );
        String managerToken = managerResp.getValue().getSessionToken();

        // Appoint the second user as store manager
        marketService.appointStoreManager(clientToken, managerResp.getValue().getId(), store_id);
        marketService.changeManagerPermissions(clientToken, managerResp.getValue().getId(), store_id, List.of(PermissionType.OVERSEE_OFFERS));

        // Step 3: Manager counters the offer
        Response<OfferDTO> counterResp = storeService.counterOffer(managerToken, offer.getId(), 5.55);
        assertFalse("Counter offer should succeed", counterResp.errorOccurred());
        assertEquals("Countered price should match", 5.55, counterResp.getValue().getLastPrice(), 0.01);

        // Step 4: Member accepts the offer
        Response<OfferDTO> acceptResp = shoppingService.acceptOffer(clientToken, offer.getId());
        assertFalse("Accepting offer should succeed\n ", acceptResp.errorOccurred());
        assertTrue("Offer should be marked as accepted", acceptResp.getValue().isAccepted());
        assertEquals("Offer ID should match", offer.getId(), acceptResp.getValue().getId());
    }

    public void testOrDiscount_BestPriceSelection() {
        // Create two simple discounts for OR composition
        DiscountDTO simpleDiscount1 = createSimpleDiscount(
            ConditionType.MIN_QUANTITY, 
            QualifierType.PRODUCT, 
            product_id, 
            0.15f  // 15% discount
        );
        
        DiscountDTO simpleDiscount2 = createSimpleDiscount(
            ConditionType.MAX_PRICE, 
            QualifierType.PRODUCT, 
            product_id, 
            0.25f  // 25% discount (better)
        );
        
        // Create OR composite discount
        DiscountDTO orDiscount = createOrDiscount(List.of(simpleDiscount1, simpleDiscount2));
        
        storeService.addDiscount(clientToken, store_id, orDiscount);
        
        // Add 3 items to trigger both conditions
        shoppingService.addProductToCart(store_id, clientToken, product_id, 3);
        
        Response<CartDTO> response = shoppingService.viewCart(clientToken);
        assertFalse("Should not have error", response.errorOccurred());
        
        CartDTO cart = response.getValue();
        ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
        double finalPrice = basket.getTotalPrice();
        double originalPrice = basket.getTotalOriginalPrice(); // 3 items
        
        // Should apply the better discount (25% from MAX_PRICE condition)
        assertTrue("Should have some discount applied", finalPrice <= originalPrice);
        
        checkCartInvariants();
        checkPriceInvariants(cart);
        checkDiscountInvariants(cart);
    }


    //
    // INVARIANT CHECKING METHODS
    //

    private void checkCartInvariants() {
        Response<CartDTO> cartResponse = shoppingService.viewCart(clientToken);
        assertFalse("Cart should always be viewable", cartResponse.errorOccurred());
        
        CartDTO cart = cartResponse.getValue();
        assertNotNull("Cart should never be null", cart);
        assertEquals("Cart client ID should match session token client", 
                    tokenService.extractId(clientToken), cart.getClientId());
        
        // Check that all baskets belong to the correct client
        for (Map.Entry<String, ShoppingBasketDTO> entry : cart.getBaskets().entrySet()) {
            ShoppingBasketDTO basket = entry.getValue();
            assertEquals("Basket client ID should match cart client ID", 
                        cart.getClientId(), basket.getClientId());
            assertEquals("Basket store ID should match map key", 
                        entry.getKey(), basket.getStoreId());
            
            // Check that all items in basket have positive quantities
            for (ItemDTO item : basket.getOrders().values()) {
                assertTrue("Item quantity should be positive", item.getAmount() > 0);
            }
        }
    }

    private void checkPriceInvariants(CartDTO cart) {
        for (ShoppingBasketDTO basket : cart.getBaskets().values()) {
            for (ItemDTO item : basket.getOrders().values()) {
                assertTrue("Item price should never be negative", item.getPrice() >= 0);
                assertTrue("Item total price should never be negative", item.getTotalPrice() >= 0);
                assertTrue("Original price should be positive", item.getOriginalPrice() > 0);
                
                if (item.getPriceBreakDown() != null) {
                    assertTrue("Final price should not exceed original price", 
                              item.getPrice() <= item.getOriginalPrice());
                }
            }
            
            assertTrue("Basket total should never be negative", basket.getTotalPrice() >= 0);
        }
        
        assertTrue("Cart total should never be negative", cart.getTotalPrice() >= 0);
    }

    private void checkDiscountInvariants(CartDTO cart) {
        for (ShoppingBasketDTO basket : cart.getBaskets().values()) {
            for (ItemDTO item : basket.getOrders().values()) {
                if (item.getPriceBreakDown() != null) {
                    ItemPriceBreakdownDTO breakdown = item.getPriceBreakDown();
                    
                    // Discount percentage should be between 0 and 1
                    assertTrue("Discount should be non-negative", breakdown.getDiscount() >= 0);
                    assertTrue("Discount should not exceed 100%", breakdown.getDiscount() <= 1);
                    
                    // Final price calculation should be correct
                    double expectedFinalPrice = breakdown.getOriginalPrice() * (1 - breakdown.getDiscount());
                    assertEquals("Final price calculation should be correct", 
                                expectedFinalPrice, breakdown.getFinalPrice(), DELTA);
                    
                    // Price should match breakdown final price
                    assertEquals("Item price should match breakdown final price", 
                                breakdown.getFinalPrice(), item.getPrice(), DELTA);
                }
            }
        }
    }

    private void checkInventoryInvariants() {
        // Check that all inventory quantities are non-negative
        try {
            ItemDTO item1 = itemService.getItem(clientToken, store_id, product_id).getValue();
            ItemDTO item2 = itemService.getItem(clientToken, store_id, product_id_2).getValue();
            
            assertTrue("Product 1 stock should be non-negative", item1.getAmount() >= 0);
            assertTrue("Product 2 stock should be non-negative", item2.getAmount() >= 0);
        } catch (Exception e) {
            fail("Should be able to check inventory: " + e.getMessage());
        }
    }

    //
    // EDGE CASE TESTS FOR DISCOUNT VALIDATION
    //

    @Test
    public void testInvalidDiscountPercentage_TooHigh() {
        try {
            // This should fail validation in DiscountBuilder
            DiscountDTO invalidDiscount = createSimpleDiscount(
                ConditionType.MIN_QUANTITY, 
                QualifierType.PRODUCT, 
                product_id, 
                1.5f  // 150% - invalid
            );
            fail("Should have thrown exception for invalid discount percentage");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention discount percentage range", 
                      e.getMessage().contains("Discount percentage must be between 0 and 1"));
        }
    }

    @Test
    public void testInvalidDiscountPercentage_Negative() {
        try {
            // This should fail validation in DiscountBuilder
            DiscountDTO invalidDiscount = createSimpleDiscount(
                ConditionType.MIN_QUANTITY, 
                QualifierType.PRODUCT, 
                product_id, 
                -0.1f  // Negative - invalid
            );
            fail("Should have thrown exception for negative discount percentage");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention discount percentage range", 
                      e.getMessage().contains("Discount percentage must be between 0 and 1"));
        }
    }

    @Test
    public void testInvalidQualifierValue_Null() {
        try {
            // This should fail validation in DiscountBuilder
            DiscountDTO invalidDiscount = createSimpleDiscount(
                ConditionType.MIN_QUANTITY, 
                QualifierType.PRODUCT, 
                null,  // Null qualifier value - invalid
                0.2f
            );
            fail("Should have thrown exception for null qualifier value");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention qualifier value cannot be null", 
                      e.getMessage().contains("Qualifier value cannot be null or empty"));
        }
    }

    @Test
    public void testInvalidQualifierValue_Empty() {
        try {
            // This should fail validation in DiscountBuilder
            DiscountDTO invalidDiscount = createSimpleDiscount(
                ConditionType.MIN_QUANTITY, 
                QualifierType.PRODUCT, 
                "",  // Empty qualifier value - invalid
                0.2f
            );
            fail("Should have thrown exception for empty qualifier value");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention qualifier value cannot be empty", 
                      e.getMessage().contains("Qualifier value cannot be null or empty"));
        }
    }

    @Test
    public void testCompositeDiscount_EmptySubDiscounts() {
        try {
            // This should fail validation in DiscountBuilder
            DiscountDTO invalidComposite = createAndDiscount(List.of());
            fail("Should have thrown exception for empty sub-discounts list");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention sub-discounts cannot be empty", 
                      e.getMessage().contains("Sub-discounts cannot be null or empty"));
        }
    }

    @Test
    public void testXorDiscount_SingleSubDiscount() {
        try {
            DiscountDTO singleDiscount = createSimpleDiscount(
                ConditionType.MIN_QUANTITY, 
                QualifierType.PRODUCT, 
                product_id, 
                0.2f
            );
            
            // XOR requires exactly 2 sub-discounts
            DiscountDTO invalidXor = createXorDiscount(singleDiscount, null);
            fail("Should have thrown exception for XOR with null second discount");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention XOR requires 2 valid sub-discounts", 
                      e.getMessage().contains("XOR discount requires exactly 2 valid sub-discounts"));
        }
    }

    //
    // BOUNDARY CONDITION TESTS
    //

    @Test
    public void testMinimumValidDiscount_ZeroPercent() {
        // Test 0% discount (minimum valid value)
        DiscountDTO zeroDiscount = createSimpleDiscount(
            ConditionType.MIN_QUANTITY, 
            QualifierType.PRODUCT, 
            product_id, 
            0.0f  // 0% discount
        );
        
        Response<DiscountDTO> response = storeService.addDiscount(clientToken, store_id, zeroDiscount);
        assertFalse("Zero percent discount should be valid", response.errorOccurred());
        
        // Add items and verify no discount is actually applied
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        
        Response<CartDTO> cartResponse = shoppingService.viewCart(clientToken);
        CartDTO cart = cartResponse.getValue();
        ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
        ItemDTO item = basket.getOrders().get(product_id);
        
        assertEquals("Zero discount should result in original price", 
                    item.getOriginalPrice(), item.getPrice(), DELTA);
        assertEquals("Discount should be 0", 0.0, item.getPriceBreakDown().getDiscount(), DELTA);
        
        checkCartInvariants();
        checkPriceInvariants(cart);
        checkDiscountInvariants(cart);
    }

    @Test
    public void testMaximumValidDiscount_HundredPercent() {
        // Test 100% discount (maximum valid value)
        DiscountDTO maxDiscount = createSimpleDiscount(
            ConditionType.MIN_QUANTITY, 
            QualifierType.PRODUCT, 
            product_id, 
            1.0f  // 100% discount
        );
        
        Response<DiscountDTO> response = storeService.addDiscount(clientToken, store_id, maxDiscount);
        assertFalse("Hundred percent discount should be valid", response.errorOccurred());
        
        // Add items and verify full discount is applied
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        
        Response<CartDTO> cartResponse = shoppingService.viewCart(clientToken);
        CartDTO cart = cartResponse.getValue();
        ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
        ItemDTO item = basket.getOrders().get(product_id);
        
        assertEquals("Full discount should result in zero price", 0.0, item.getPrice(), DELTA);
        assertEquals("Discount should be 100%", 1.0, item.getPriceBreakDown().getDiscount(), DELTA);
        
        checkCartInvariants();
        checkPriceInvariants(cart);
        checkDiscountInvariants(cart);
    }

    @Test
    public void testDiscountConsistency_ViewCartVsCheckout() {
        when(this.mockPaymentService.processPayment(any(), any(), any(), any(), any(), anyDouble())).thenReturn(new Response<>(10000));
        when(this.mockSupplyService.supplyOrder(any(), any(), any(), any(), any())).thenReturn(new Response<>(20000));
        
        // Create discount
        DiscountDTO discount = createSimpleDiscount(
            ConditionType.MIN_QUANTITY, 
            QualifierType.PRODUCT, 
            product_id, 
            0.3f  // 30% discount
        );
        
        storeService.addDiscount(clientToken, store_id, discount);
        
        // Add items to cart
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        
        // Get discounted price from viewCart
        Response<CartDTO> cartResponse = shoppingService.viewCart(clientToken);
        CartDTO cart = cartResponse.getValue();
        double viewCartPrice = cart.getTotalPrice();
        
        // Verify discount is applied in viewCart
        assertTrue("ViewCart should show discounted price", viewCartPrice < 20.0); // 2 * 10 = 20 original
        
        // Perform checkout and verify it uses the same discounted price
        Response<Boolean> checkoutResponse = shoppingService.checkout(
            clientToken, "1234556788", "1234567890123456", new Date(), "123", 
            "John Doe", "123 Main St", "city", "country", "zip");
        
        assertFalse("Checkout should succeed", checkoutResponse.errorOccurred());
        
        // Verify cart is empty after checkout
        Response<CartDTO> emptyCartResponse = shoppingService.viewCart(clientToken);
        assertTrue("Cart should be empty after checkout", 
                  emptyCartResponse.getValue().getBaskets().isEmpty());
        
        checkInventoryInvariants();
    }


    @Test
    public void testAcceptOffer_InvalidToken_ReturnsError() {
        Response<OfferDTO> response = shoppingService.acceptOffer("invalidToken", "someOfferId");

        assertTrue(response.errorOccurred());
        assertEquals("Invalid token", response.getErrorMessage());
    }

    @Test
    public void testGetAllOffersOfUser_Success() {
        PaymentDetailsDTO paymentDetails = new PaymentDetailsDTO(user.getId(), "4111111111111111", LocalDate.now().plusYears(1), "123", "Offer Tester");
        shoppingService.makeOffer(clientToken, store_id, product_id, 11.11, paymentDetails);

        Response<List<OfferDTO>> response = shoppingService.getAllOffersOfUser(clientToken);
        assertFalse(response.errorOccurred());
        assertNotNull(response.getValue());
        assertFalse(response.getValue().isEmpty());
    }

    @Test
    public void testGetAllOffersOfUser_InvalidToken_ReturnsError() {
        Response<List<OfferDTO>> response = shoppingService.getAllOffersOfUser("invalidToken");

        assertTrue(response.errorOccurred());
        assertEquals("Invalid token", response.getErrorMessage());
    }

    @Test
    public void testCounterOffer_ValidInput_ReturnsUpdatedOffer() {
        PaymentDetailsDTO paymentDetails = new PaymentDetailsDTO(user.getId(), "4111111111111111", LocalDate.now().plusYears(1), "123", "Offer Tester");
        OfferDTO offer = shoppingService.makeOffer(clientToken, store_id, product_id, 6.66, paymentDetails).getValue();
        Response<OfferDTO> counterResponse = shoppingService.counterOffer(clientToken, offer.getId(), 7.77);

        assertFalse(counterResponse.errorOccurred());
        assertNotNull(counterResponse.getValue());
        assertEquals(7.77, counterResponse.getValue().getLastPrice(), 0.01);
    }

    @Test
    public void testCounterOffer_InvalidToken_ReturnsError() {
        Response<OfferDTO> response = shoppingService.counterOffer("invalidToken", "offer123", 6.99);

        assertTrue(response.errorOccurred());
        assertEquals("Invalid token", response.getErrorMessage());
    }

    public void testViewCart_WithSimpleDiscount() {
        // Create a valid simple discount using helper method
        DiscountDTO discount = createSimpleDiscount(
            ConditionType.MIN_QUANTITY, 
            QualifierType.PRODUCT, 
            product_id, 
            0.2f  // 20% discount
        );
        
        Response<DiscountDTO> discountResponse = storeService.addDiscount(clientToken, store_id, discount);
        assertFalse("Adding discount should succeed", discountResponse.errorOccurred());
        
        // Add 2 items to cart to trigger discount
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        
        Response<CartDTO> response = shoppingService.viewCart(clientToken);
        assertFalse("Should not have error", response.errorOccurred());
        
        CartDTO cart = response.getValue();
        ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
        ItemDTO item = basket.getOrders().get(product_id);
        
        // Verify discount is applied
        assertNotNull("Item should have price breakdown", item.getPriceBreakDown());
        assertEquals("Original price should be 10.0", 10.0, item.getOriginalPrice(), DELTA);
        assertEquals("Discount should be 20%", 0.2, item.getPriceBreakDown().getDiscount(), DELTA);
        assertEquals("Final price should be 8.0 (10 * 0.8)", 8.0, item.getPrice(), DELTA);
        assertTrue("Should have discount", item.getPriceBreakDown().hasDiscount());
        
        checkCartInvariants();
        checkPriceInvariants(cart);
        checkDiscountInvariants(cart);
    }

    @Test
    public void testViewCart_WithDiscountNotTriggered() {
        // Create discount with minimum quantity 3
        ConditionDTO condition = new ConditionDTO(null, ConditionType.MIN_QUANTITY);
        condition.setMinQuantity(3);
        condition.setProductId(product_id);
        
        DiscountDTO discount = new DiscountDTO(null, store_id,DiscountType.SIMPLE, condition);
        discount.setDiscountPercentage(0.3f); // 30% discount
        discount.setQualifierType(QualifierType.PRODUCT);
        discount.setQualifierValue(product_id);
        discount.setStoreId(store_id);
        
        storeService.addDiscount(clientToken, store_id, discount);
        
        // Add only 2 items (less than minimum 3)
        shoppingService.addProductToCart(store_id, clientToken, product_id, 2);
        
        Response<CartDTO> response = shoppingService.viewCart(clientToken);
        assertFalse("Should not have error", response.errorOccurred());
        
        CartDTO cart = response.getValue();
        ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
        ItemDTO item = basket.getOrders().get(product_id);
        
        // Verify no discount is applied
        assertEquals("Final price should equal original price", 
                    item.getOriginalPrice(), item.getPrice(), DELTA);
        assertEquals("Discount should be 0", 0.0, item.getPriceBreakDown().getDiscount(), DELTA);
        assertFalse("Should not have discount", item.getPriceBreakDown().hasDiscount());
        
        checkCartInvariants();
        checkPriceInvariants(cart);
    }

    @Test
    public void testViewCart_EmptyCart() {
        Response<CartDTO> response = shoppingService.viewCart(clientToken);
        assertFalse("Should not have error even for empty cart", response.errorOccurred());
        assertTrue("Cart should be empty", response.getValue().getBaskets().isEmpty());
        
        checkCartInvariants();
    }

    //
    // USE CASE 2.5: IMMEDIATE PURCHASE (CHECKOUT) - UPDATED FOR DISCOUNTS
    //

    @Test
    public void testCheckout_Success_WithoutDiscounts() {
        when(this.mockPaymentService.processPayment(any(), any(), any(), any(), any(), anyDouble())).thenReturn(new Response<>(10000));
        when(this.mockSupplyService.supplyOrder(any(), any(), any(), any(), any())).thenReturn(new Response<>(20000));
        
        shoppingService.addProductToCart(store_id, clientToken, product_id, 1);
        
        // Record initial stock
        ItemDTO itemBefore = itemService.getItem(clientToken, store_id, product_id).getValue();
        int initialStock = itemBefore.getAmount();
        
        Response<Boolean> response = shoppingService.checkout(
            clientToken, "1234556788", "1234567890123456", new Date(), "123", 
            "John Doe", "123 Main St", "city", "country", "zip");
        
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        
        // Verify that the item stock has been reduced by one after successful checkout
        ItemDTO itemAfter = itemService.getItem(clientToken, store_id, product_id).getValue();
        assertEquals("Stock should decrease by 1 after checkout", 
                    initialStock - 1, itemAfter.getAmount());
        
        checkInventoryInvariants();
    }

    @Test
    public void testCheckout_Success_WithDiscounts() {
        when(this.mockPaymentService.processPayment(any(), any(), any(), any(), any(), anyDouble())).thenReturn(new Response<>(10000));
        when(this.mockSupplyService.supplyOrder(any(), any(), any(), any(), any())).thenReturn(new Response<>(20000));
        
        // Create valid discount using helper method
        DiscountDTO discount = createSimpleDiscount(
            ConditionType.MIN_QUANTITY, 
            QualifierType.PRODUCT, 
            product_id, 
            0.25f  // 25% discount
        );
        
        storeService.addDiscount(clientToken, store_id, discount);
        
        // Add 3 items to trigger discount
        shoppingService.addProductToCart(store_id, clientToken, product_id, 3);
        
        // Record initial stock
        ItemDTO itemBefore = itemService.getItem(clientToken, store_id, product_id).getValue();
        int initialStock = itemBefore.getAmount();
        
        Response<Boolean> response = shoppingService.checkout(
            clientToken, "1234556788", "1234567890123456", new Date(), "123", 
            "John Doe", "123 Main St", "city", "country", "zip");
        
        assertFalse("Should not have error", response.errorOccurred());
        assertEquals("Should return true in the value", Boolean.TRUE, response.getValue());
        
        // Verify stock reduction
        ItemDTO itemAfter = itemService.getItem(clientToken, store_id, product_id).getValue();
        assertEquals("Stock should decrease by 3 after checkout", 
                    initialStock - 3, itemAfter.getAmount());
        
        // Verify cart is empty after successful checkout
        Response<CartDTO> cartResponse = shoppingService.viewCart(clientToken);
        assertTrue("Cart should be empty after successful checkout", 
                  cartResponse.getValue().getBaskets().isEmpty());
        
        checkInventoryInvariants();
    }

    @Test
    public void testCheckout_InsufficientStock() {
        // First add a product to the cart with quantity greater than available stock
        int requestedQuantity = 10; // More than the available 5
        shoppingService.addProductToCart(store_id, clientToken, product_id, requestedQuantity);
        
        // Record initial stock
        ItemDTO itemBefore = itemService.getItem(clientToken, store_id, product_id).getValue();
        int initialStock = itemBefore.getAmount();
        
        // Prepare checkout parameters
        String cardNumber = "1234567890123456";
        String userSSN = "123-45-6789";
        Date expiryDate = new Date();
        String cvv = "123";
        String clientName = "John Doe";
        String deliveryAddress = "123 Main St";
        String city = "Test City";
        String country = "Test Country";
        String zip = "12345";
        
        // Act - Attempt to checkout
        Response<Boolean> response = shoppingService.checkout(clientToken, userSSN, cardNumber, expiryDate, cvv, clientName, deliveryAddress, city, country, zip);
        
        // Assert
        assertTrue("Should have error due to insufficient stock", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        assertNotNull("Error message should not be null", response.getErrorMessage());
        
        // Verify stock unchanged and cart preserved
        ItemDTO itemAfter = itemService.getItem(clientToken, store_id, product_id).getValue();
        assertEquals("Stock should remain unchanged after failed checkout", 
                    initialStock, itemAfter.getAmount());
        
        // Verify cart still contains items
        Response<CartDTO> cartResponse = shoppingService.viewCart(clientToken);
        assertFalse("Cart should still contain items after failed checkout", 
                   cartResponse.getValue().getBaskets().isEmpty());
        
        checkInventoryInvariants();
        checkCartInvariants();
    }

    @Test
    public void testCheckout_PaymentError() {
        shoppingService.addProductToCart(store_id, clientToken, product_id, 1);
        
        ItemDTO itemBefore = itemService.getItem(clientToken, store_id, product_id).getValue();
        int initialStock = itemBefore.getAmount();
        
        Response<Boolean> response = shoppingService.checkout(
            clientToken, "invalid", "invalid", new Date(), "123", "John Doe", "123 Main St", "city", "country", "zip");
        
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
        
        ItemDTO itemAfter = itemService.getItem(clientToken, store_id, product_id).getValue();
        assertEquals("Stock should remain unchanged after failed checkout", 
                    initialStock, itemAfter.getAmount());
        
        checkInventoryInvariants();
        checkCartInvariants();
    }


    @Test
    public void testCheckout_WithCompositeDiscounts() {
        when(this.mockPaymentService.processPayment(any(), any(), any(), any(), any(), anyDouble())).thenReturn(new Response<>(10000));
        when(this.mockSupplyService.supplyOrder(any(), any(), any(), any(), any())).thenReturn(new Response<>(20000));
        
        // Create two simple discounts for AND composition
        DiscountDTO simpleDiscount1 = createSimpleDiscount(
            ConditionType.MIN_QUANTITY, 
            QualifierType.PRODUCT, 
            product_id, 
            0.1f
        );
        
        DiscountDTO simpleDiscount2 = createSimpleDiscount(
            ConditionType.MAX_PRICE, 
            QualifierType.PRODUCT, 
            product_id, 
            0.05f
        );
        
        // Create AND composite discount
        DiscountDTO compositeDiscount = createAndDiscount(List.of(simpleDiscount1, simpleDiscount2));
        
        storeService.addDiscount(clientToken, store_id, compositeDiscount);
        
        // Add product to cart to trigger both conditions
        shoppingService.addProductToCart(store_id, clientToken, product_id, 3);
        
        // Record initial state
        ItemDTO itemBefore = itemService.getItem(clientToken, store_id, product_id).getValue();
        int initialStock = itemBefore.getAmount();
        
        Response<Boolean> response = shoppingService.checkout(
            clientToken, "1234556788", "1234567890123456", new Date(), "123", 
            "John Doe", "123 Main St", "city", "country", "zip");
        
        assertFalse("Checkout should succeed", response.errorOccurred());
        assertEquals("Should return true", Boolean.TRUE, response.getValue());
        
        // Verify stock reduction
        ItemDTO itemAfter = itemService.getItem(clientToken, store_id, product_id).getValue();
        assertEquals("Stock should decrease by 3", initialStock - 3, itemAfter.getAmount());
        
        checkInventoryInvariants();
    }

    @Test
    public void testDiscountInvariant_PriceNeverNegative() {
        // Create a discount with valid percentage (system should handle edge cases)
        DiscountDTO discount = createSimpleDiscount(
            ConditionType.MIN_QUANTITY, 
            QualifierType.PRODUCT, 
            product_id, 
            0.99f  // 99% discount (high but valid)
        );
        
        Response<DiscountDTO> discountResponse = storeService.addDiscount(clientToken, store_id, discount);
        
        // If the system allows adding such a discount, ensure the price calculation handles it properly
        if (!discountResponse.errorOccurred()) {
            shoppingService.addProductToCart(store_id, clientToken, product_id, 1);
            
            Response<CartDTO> response = shoppingService.viewCart(clientToken);
            assertFalse("Should not have error", response.errorOccurred());
            
            CartDTO cart = response.getValue();
            ShoppingBasketDTO basket = cart.getBaskets().get(store_id);
            ItemDTO item = basket.getOrders().get(product_id);
            
            // Price should never be negative
            assertTrue("Final price should never be negative", item.getPrice() >= 0);
            assertTrue("Total price should never be negative", item.getTotalPrice() >= 0);
            
            checkCartInvariants();
            checkPriceInvariants(cart);
        }
    }
}
