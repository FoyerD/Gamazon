import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import Application.DTOs.CartDTO;
import Application.ShoppingService;
import Application.utils.Response;
import Domain.TokenService;
import Domain.Shopping.IReceiptRepository;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartRepository;
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
    public void testCheckout_PaymentError() {
        shoppingService.addProductToCart(STORE_ID, CLIENT_ID, PRODUCT_ID, 1);
        Response<Boolean> response = shoppingService.checkout(
            CLIENT_ID, "invalid", new Date(), "123", 12345L, "John Doe", "123 Main St");
        assertTrue("Should have error", response.errorOccurred());
        assertNull("Value should be null", response.getValue());
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
}
