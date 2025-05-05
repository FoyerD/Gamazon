import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.IReceiptRepository;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Shopping.ShoppingBasket;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Pair;
import Domain.TokenService;
import Domain.Store.Feedback;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IProductRepository;
import Domain.Store.IStoreRepository;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;
import Domain.User.LoginManager;
import Domain.User.Member;
import Domain.User.User;
import Domain.management.MarketFacade;
import Domain.management.PermissionType;
import Infrastructure.Repositories.MemoryAuctionRepository;
import Infrastructure.Repositories.MemoryFeedbackRepository;
import Infrastructure.Repositories.MemoryItemRepository;
import Infrastructure.Repositories.MemoryProductRepository;
import Infrastructure.Repositories.MemoryReceiptRepository;
import Infrastructure.Repositories.MemoryShoppingBasketRepository;
import Infrastructure.Repositories.MemoryShoppingCartRepository;
import Infrastructure.Repositories.MemoryStoreRepository;
import Infrastructure.Repositories.MemoryUserRepository;
import ch.qos.logback.core.subst.Token;
import Domain.Store.Item;
import Domain.Store.ItemFacade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Application.ItemService;
import Application.MarketService;
import Application.StoreService;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import Application.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MarketServiceTest {

    // Market
    private MarketService marketService;
    private MarketFacade marketFacade;
    private IPaymentService mockPaymentService;
    private ISupplyService mockSupplyService;
    private INotificationService mockNotificationService;

    // Token
    private TokenService tokenService;

    // User
    private UserService userService;
    private IUserRepository userRepository;
    UUID userId = UUID.randomUUID();
    
    // Store
    private IItemRepository itemRepository;
    private IProductRepository productRepository;
    private IStoreRepository storeRepository;
    private IAuctionRepository auctionRepository;
    private IFeedbackRepository feedbackRepository;

    private StoreFacade storeFacade;
    private ItemFacade itemFacade;

    private String tokenId;
    private StoreService storeService;
    private String storeId;
    private ItemService itemService;
    private String productId;


    // Shopping Cart
    private ShoppingCartFacade shoppingCartFacade;
    private IShoppingCartRepository shoppingCartRepository;
    private IShoppingBasketRepository shoppingBasketRepository; 
    private IReceiptRepository receiptRepository;


    @BeforeEach
    public void setUp() {
        // Mock external services
        mockPaymentService = mock(IPaymentService.class);
        mockSupplyService = mock(ISupplyService.class);
        mockNotificationService = mock(INotificationService.class);
        
        marketFacade = MarketFacade.getInstance();

        // Store and Item setup
        storeFacade = new StoreFacade();
        itemRepository = new MemoryItemRepository();
        productRepository = new MemoryProductRepository();
        storeRepository = new MemoryStoreRepository();

        itemFacade = new ItemFacade(itemRepository, productRepository, storeRepository);
        auctionRepository = new MemoryAuctionRepository();
        feedbackRepository= new MemoryFeedbackRepository();
        userRepository = new MemoryUserRepository();

        this.tokenService = new TokenService();
        this.storeFacade = new StoreFacade(storeRepository, feedbackRepository, itemRepository, userRepository, auctionRepository);
        storeService = new StoreService(storeFacade, tokenService);

        // Shopping Cart setup
        shoppingCartRepository = new MemoryShoppingCartRepository();
        shoppingBasketRepository = new MemoryShoppingBasketRepository();
        receiptRepository = new MemoryReceiptRepository();
        
        
        shoppingCartFacade = new ShoppingCartFacade(shoppingCartRepository, shoppingBasketRepository, mockPaymentService,
                itemFacade, storeFacade, receiptRepository, productRepository);

        // Market setup
        marketFacade.initFacades(userRepository, itemRepository, storeFacade, shoppingCartFacade);
        marketFacade.updatePaymentService(mockPaymentService);
        marketFacade.updateSupplyService(mockSupplyService);
        marketFacade.updateNotificationService(mockNotificationService);
        marketService = new MarketService(marketFacade, tokenService);
        
        // User Service setup
        userService = new UserService(new LoginManager(new MemoryUserRepository()), tokenService);
        tokenId = this.tokenService.generateToken(userId.toString());
        User user = new Member(userId, "Member1", "passpass", "email@email.com");
        this.userRepository.add(userId.toString(), user);


        // Open a store
        Response<StoreDTO> response = storeService.addStore(tokenId, "storeName", "A new store");
        storeId = response.getValue().getId();
        productId = UUID.randomUUID().toString();


    }

    @Test
    public void testOpenMarket(){
        // Open the market and verify that the market is open
        Response<Void> response = marketService.openMarket(tokenId);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void testaddProductsToInventory_noPermissions() {
        Map<String, Integer> products = Map.of(productId, 5);
        Response<Void> response = marketService.addProductsToInventory(tokenId, storeId, products);
        assertTrue(response.errorOccurred());    
    }

    @Test
    public void testRemoveProductsFromInventory_noPermissions() {
        Map<String, Integer> productsToRemove = Map.of(productId, 1);
        Response<Void> response = marketService.removeProductsFromInventory(tokenId, storeId, productsToRemove);
        assertTrue(response.errorOccurred());
        // Item item = itemResponse.getValue();
        // assertEquals(2, item.getAmount(), "Product quantity mismatch after removing from inventory");
    }

    @Test
    public void testUpdatePaymentService() {
        Response<Void> response = marketService.updatePaymentService(tokenId, mockPaymentService);
        assertFalse(response.errorOccurred());
        verify(mockPaymentService, never()).initialize(); // Ensure no unexpected initialization
    }

    @Test
    public void testUpdateNotificationService() {
        Response<Void> response = marketService.updateNotificationService(tokenId, mockNotificationService);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void testUpdateSupplyService() {
        Response<Void> response = marketService.updateSupplyService(tokenId, mockSupplyService);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void testUpdatePaymentServiceURL() throws IOException {
        String newUrl = "http://new-payment-service.com";
        Response<Void> response = marketService.updatePaymentServiceURL(tokenId, newUrl);
        assertFalse(response.errorOccurred());
        verify(mockPaymentService).updatePaymentServiceURL(newUrl);
    }

    // @Test
    // public void testAppointStoreManager() {
    //     String appointeeUsername = "newManager";
    //     Response<Void> response = marketService.appointStoreManager(guestToken, "ownerUser", appointeeUsername, storeId);
    //     assertFalse(response.errorOccurred());
    // }

    // @Test
    // public void testRemoveStoreManager() {
    //     String managerUsername = "existingManager";
    //     Response<Void> response = marketService.removeStoreManager(guestToken, "ownerUser", managerUsername, storeId);
    //     assertFalse(response.errorOccurred());
    // }

    // @Test
    // public void testAppointStoreOwner() {
    //     String appointeeUsername = "newOwner";
    //     Response<Void> response = marketService.appointStoreOwner(guestToken, "ownerUser", appointeeUsername, storeId);
    //     assertFalse(response.errorOccurred());
    // }

    // @Test
    // public void testCloseStore() {
    //     Response<Void> response = marketService.closeStore(guestToken, storeId);
    //     assertFalse(response.errorOccurred());
    // }

    // @Test
    // public void testMarketCloseStore() {
    //     Response<Void> response = marketService.marketCloseStore(guestToken, storeId);
    //     assertFalse(response.errorOccurred());
    // }

    // @Test
    // public void testGetManagersPermissions() {
    //     Response<Map<String, List<PermissionType>>> response = marketService.getManagersPermissions(guestToken, storeId);
    //     assertFalse(response.errorOccurred());
    //     assertNotNull(response.getValue());
    // }

    // @Test
    // public void testRespondToUserMessage() {
    //     String comment = "Thank you for your feedback!";
    //     Response<Boolean> response = marketService.respondToUserMessage(guestToken, storeId, productId, "userId", comment);
    //     assertFalse(response.errorOccurred(), "Failed to respond to user message");
    //     assertTrue(response.getValue(), "Response to user message was not successful");
    // }

    // @Test
    // public void testGetUserMessage() {
    //     Response<Feedback> response = marketService.getUserMessage(guestToken, storeId, productId, "userId");
    //     assertFalse(response.errorOccurred(), "Failed to fetch user message");
    //     assertNotNull(response.getValue(), "Feedback is null");
    // }

    // @Test
    // public void testGetStorePurchaseHistory() {
    //     LocalDateTime from = LocalDateTime.now().minusDays(30);
    //     LocalDateTime to = LocalDateTime.now();
    //     Response<List<ShoppingBasket>> response = marketService.getStorePurchaseHistory(guestToken, storeId, from, to);
    //     assertFalse(response.errorOccurred(), "Failed to fetch store purchase history");
    //     assertNotNull(response.getValue(), "Purchase history is null");
    // }

    // @Test
    // public void testOpenMarket() {
    //     Response<Void> response = marketService.openMarket(guestToken);
    //     assertFalse(response.errorOccurred(), "Failed to open market");
    // }
        

    
}