import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.IReceiptRepository;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Shopping.Receipt;
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
import Domain.Store.Product;
import Domain.Store.Store;

import org.junit.jupiter.api.BeforeAll;
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
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    
    // Store
    private IItemRepository itemRepository;
    private IProductRepository productRepository;
    private IStoreRepository storeRepository;
    private IAuctionRepository auctionRepository;
    private IFeedbackRepository feedbackRepository;

    private StoreFacade storeFacade;

    private String tokenId1;
    private String tokenId2;
    private StoreService storeService;
    private ItemFacade itemFacade;
    private ItemService itemService;

    private String storeId1;
    private String storeId2;

    private final String productId1 = "product1";
    private final String productId2 = "product2";
    private final String productId3 = "product3";

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
        tokenId1 = this.tokenService.generateToken(userId1.toString());
        tokenId2 = this.tokenService.generateToken(userId2.toString());
        addUser(userId1, "Member1", "passpass1", "email1@email.com");
        addUser(userId2, "Member2", "passpass2", "email2@email.com");

        this.itemFacade = new ItemFacade(itemRepository, productRepository, storeRepository);
        this.itemService = new ItemService(itemFacade, tokenService);

        storeId1 = marketService.addStore(tokenId1, "Store One", "A store for testing").getValue().getId();
        addProduct(storeId1, "Store One", userId1.toString(), productId1, "In Stock Item", 49.99f, 10, "In Stock Item");
        addProduct(storeId1, "Store One", userId1.toString(), productId2, "Out of Stock Item", 19.99f, 0, "Out of Stock Item");

    }

    private void addProduct(String storeId, String storeName, String founderId, String productId, String productName, float price, int amount, String itemName) {
        if (productRepository.get(productId) == null) {
            productRepository.add(productId, new Product(productId, productName));
        }
        Item item = new Item(storeId, productId, price, amount, itemName);
    
        itemFacade.add(storeId, productId, price, amount, itemName);
    }

    private void addUser(UUID userId, String userName, String password, String email) {
        User user = new Member(userId, userName, password, email);
        this.userRepository.add(userId.toString(), user);
    }

    @Test
    public void givenMarketClosed_whenOpenMarket_thenMarketIsOpened() {
        Response<Void> response = marketService.openMarket(tokenId1);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenOpenStore_whenCloseStore_thenStoreIsClosed() {
        Response<Void> response = marketService.closeStore(tokenId1, storeId1);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenStoreExists_whenMarketClosesStore_thenStoreIsClosedByMarket() {
        Response<Void> response = marketService.marketCloseStore(tokenId1, storeId1);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenValidUsers_whenAppointingStoreManager_thenStoreManagerIsAppointed() {
        String appointerUsername = userRepository.getMemberUsername(userId1.toString());
        String appointeeUsername = userRepository.getMemberUsername(userId2.toString());
        Response<Void> response = marketService.appointStoreManager(tokenId1, appointerUsername, appointeeUsername, storeId1);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenStoreManagerExists_whenRemovingStoreManager_thenManagerIsRemoved() {
        String appointerUsername = userRepository.getMemberUsername(userId1.toString());
        String appointeeUsername = userRepository.getMemberUsername(userId2.toString());
        Response<Void> response1 = marketService.appointStoreManager(tokenId1, appointerUsername, appointeeUsername, storeId1);
        assertFalse(response1.errorOccurred());
        Response<Void> response2 = marketService.removeStoreManager(tokenId1, appointerUsername, appointeeUsername, storeId1);
        assertFalse(response2.errorOccurred());
    }

    @Test
    public void givenValidUsers_whenAppointingStoreOwner_thenStoreOwnerIsAppointed() {
        String appointerUsername = userRepository.getMemberUsername(userId1.toString());
        String appointeeUsername = userRepository.getMemberUsername(userId2.toString());
        Response<Void> response = marketService.appointStoreOwner(tokenId1, appointerUsername, appointeeUsername, storeId1);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenStoreHasPurchases_whenGettingPurchaseHistory_thenReceiptsReturned() {
        Response<List<Receipt>> response = marketService.getStorePurchaseHistory(tokenId1, storeId1);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenStoreHasManagers_whenGettingPermissions_thenPermissionsAreReturned() {
        Response<Map<String, List<PermissionType>>> response = marketService.getManagersPermissions(tokenId1, storeId1);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenManagerExists_whenChangingPermissions_thenPermissionsAreUpdated() {
        String appointerUsername = userRepository.getMemberUsername(userId1.toString());
        String appointeeUsername = userRepository.getMemberUsername(userId2.toString());
        List<PermissionType> newPermissions = List.of(PermissionType.ADMINISTER_STORE);
        Response<Void> response1 = marketService.appointStoreManager(tokenId1, appointerUsername, appointeeUsername, storeId1);
        assertFalse(response1.errorOccurred());
        Response<Void> response2 = marketService.changeManagerPermissions(tokenId1, appointerUsername, appointeeUsername, storeId1, newPermissions);
        assertFalse(response2.errorOccurred());
    }

    @Test
    public void givenStoreHasInventory_whenAddingProducts_thenInventoryIsIncreased() {
        Map<String, Integer> products = Map.of(productId1, 5);
        Response<Void> response = marketService.addProductsToInventory(tokenId1, storeId1, products);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenStoreHasInventory_whenRemovingProducts_thenInventoryIsDecreased() {
        Map<String, Integer> productsToRemove = Map.of(productId1, 1);
        Response<Void> response = marketService.removeProductsFromInventory(tokenId1, storeId1, productsToRemove);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenValidPaymentService_whenUpdatingPaymentService_thenServiceIsUpdated() {
        Response<Void> response = marketService.updatePaymentService(tokenId1, mockPaymentService);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenValidNotificationService_whenUpdatingNotificationService_thenServiceIsUpdated() {
        Response<Void> response = marketService.updateNotificationService(tokenId1, mockNotificationService);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenValidSupplyService_whenUpdatingSupplyService_thenServiceIsUpdated() {
        Response<Void> response = marketService.updateSupplyService(tokenId1, mockSupplyService);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenNewUrl_whenUpdatingPaymentServiceUrl_thenServiceUrlIsUpdated() throws IOException {
        String newUrl = "http://new-payment-service.com";
        Response<Void> response = marketService.updatePaymentServiceURL(tokenId1, newUrl);
        assertFalse(response.errorOccurred());
        verify(mockPaymentService).updatePaymentServiceURL(newUrl);
    }

    @Test
    public void givenUserMessageExists_whenRespondingToMessage_thenResponseIsSuccessful() {
        String comment = "Thank you for your feedback!";
        Response<Boolean> response = marketService.respondToUserMessage(tokenId1, storeId1, productId1, userId1.toString(), comment);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenFeedbackIdExists_whenGettingUserMessage_thenFeedbackIsReturned() {
        String feedbackId = UUID.randomUUID().toString();
        Response<Feedback> response = marketService.getUserMessage(tokenId1, storeId1, userId1.toString(), feedbackId);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenWrongAppointee_whenAppointingStoreManager_thenErrorOccurs() {
        String appointeeUsername = "newManager";
        Response<Void> response = marketService.appointStoreManager(tokenId1, "ownerUser", appointeeUsername, storeId1);
        assertTrue(response.errorOccurred());
    }

    @Test
    public void givenWrongAppointer_whenRemovingStoreManager_thenErrorOccurs() {
        String managerUsername = "existingManager";
        Response<Void> response = marketService.removeStoreManager(tokenId1, "ownerUser", managerUsername, storeId1);
        assertTrue(response.errorOccurred());
    }

    @Test
    public void givenWrongApointee_whenAppointingStoreOwner_thenErrorOccurs() {
        String appointeeUsername = "newOwner";
        Response<Void> response = marketService.appointStoreOwner(tokenId1, "ownerUser", appointeeUsername, storeId1);
        assertTrue(response.errorOccurred());
    }

    @Test
    public void givenInvalidPermissions_whenChangingManagerPermissions_thenErrorOccurs() {
        List<PermissionType> newPermissions = List.of(PermissionType.ADMINISTER_STORE);
        Response<Void> response = marketService.changeManagerPermissions(tokenId1, "ownerUser", "managerUser", storeId1, newPermissions);
        assertTrue(response.errorOccurred());
    }

    @Test
    public void givenStoreAlreadyClosed_whenClosingStore_thenErrorOccurs() {
        Response<Void> response1 = marketService.closeStore(tokenId1, storeId1);
        Response<Void> response2 = marketService.closeStore(tokenId1, storeId1);
        assertTrue(response2.errorOccurred());
    }

    @Test
    public void givenStoreAlreadyClosed_whenMarketClosesStore_thenErrorOccurs() {
        Response<Void> response1 = marketService.marketCloseStore(tokenId1, storeId1);
        Response<Void> response2 = marketService.marketCloseStore(tokenId1, storeId1);
        assertTrue(response2.errorOccurred());
    }

    @Test
    public void givenNoPermission_whenRespondingToUserMessage_thenErrorOccurs() {
        String comment = "Thank you for your feedback!";
        Response<Boolean> response = marketService.respondToUserMessage(tokenId2, storeId1, productId1, userId2.toString(), comment);
        assertTrue(response.errorOccurred());
    }

    @Test
    public void givenNoPermission_whenGettingUserMessage_thenErrorOccurs() {
        String feedbackId = UUID.randomUUID().toString();
        Response<Feedback> response = marketService.getUserMessage(tokenId2, storeId1, userId2.toString(), feedbackId);
        assertTrue(response.errorOccurred());
    }

    @Test
    public void givenEmptyPurchaseHistory_whenGettingStoreHistory_thenEmptyListReturned() {
        Response<List<Receipt>> response = marketService.getStorePurchaseHistory(tokenId1, storeId1);
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().isEmpty());
    }

    @Test
    public void givenNoPermission_whenGettingStorePurchaseHistory_thenErrorOccurs() {
        Response<List<Receipt>> response = marketService.getStorePurchaseHistory(tokenId2, storeId1);
        assertTrue(response.errorOccurred());
    }

    @Test
    public void givenInvalidToken_whenAddingProductsToInventory_thenErrorOccurs() {
        Map<String, Integer> products = Map.of(productId1, 5);
        Response<Void> response = marketService.addProductsToInventory("invalidToken", storeId1, products);
        assertTrue(response.errorOccurred(), "Adding products with an invalid token should fail");
    }

    @Test
    public void givenInvalidStoreId_whenRemovingProductsFromInventory_thenErrorOccurs() {
        Map<String, Integer> productsToRemove = Map.of(productId1, 1);
        Response<Void> response = marketService.removeProductsFromInventory(tokenId1, "invalidStoreId", productsToRemove);
        assertTrue(response.errorOccurred(), "Removing products from an invalid store should fail");
    }

    @Test
    public void givenInvalidToken_whenUpdatingPaymentService_thenErrorOccurs() {
        Response<Void> response = marketService.updatePaymentService("invalidToken", mockPaymentService);
        assertTrue(response.errorOccurred(), "Updating payment service with an invalid token should fail");
    }

    @Test
    public void givenInvalidToken_whenUpdatingNotificationService_thenErrorOccurs() {
        Response<Void> response = marketService.updateNotificationService("invalidToken", mockNotificationService);
        assertTrue(response.errorOccurred(), "Updating notification service with an invalid token should fail");
    }

    @Test
    public void givenInvalidToken_whenUpdatingSupplyService_thenErrorOccurs() {
        Response<Void> response = marketService.updateSupplyService("invalidToken", mockSupplyService);
        assertTrue(response.errorOccurred(), "Updating supply service with an invalid token should fail");
    }


        

    
}