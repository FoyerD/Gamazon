import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.IReceiptRepository;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Shopping.Receipt;
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
import static org.mockito.ArgumentMatchers.anyString;
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
        
        // Configure mockNotificationService to return successful responses by default
        when(mockNotificationService.sendNotification(anyString(), anyString()))
            .thenReturn(Response.success(true));
        
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
        this.itemService = new ItemService(itemFacade);

        storeId1 = marketService.addStore(tokenId1, "Store One", "A store for testing").getValue().getId();
        addProduct(storeId1, "Store One", userId1.toString(), productId1, "In Stock Item", 49.99f, 10, "In Stock Item");
        addProduct(storeId1, "Store One", userId1.toString(), productId2, "Out of Stock Item", 19.99f, 0, "Out of Stock Item");
    }

    private void addProduct(String storeId, String storeName, String founderId, String productId, String productName, float price, int amount, String itemName) {
        if (productRepository.get(productId) == null) {
            productRepository.add(productId, new Product(productId, productName));
        }
        Item item = new Item(storeId, productId, price, amount, itemName);
    
        itemFacade.add(new Pair<>(storeId, productId), item);
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
        // Configure mockNotificationService for this specific test
        when(mockNotificationService.sendNotification(anyString(), anyString()))
            .thenReturn(Response.success(true));
            
        Response<Void> response = marketService.marketCloseStore(tokenId1, storeId1);
        assertFalse(response.errorOccurred());
    }


    @Test
    public void givenStoreExistsWithNotificationFailure_whenMarketClosesStore_thenOperationFails() {
        // Configure mockNotificationService to fail
        when(mockNotificationService.sendNotification(anyString(), anyString()))
            .thenReturn(Response.error("Notification failure"));
            
        Response<Void> response = marketService.marketCloseStore(tokenId1, storeId1);
        assertTrue(response.errorOccurred());
        assertTrue(response.getErrorMessage().contains("notification") || 
                response.getErrorMessage().contains("failed"));
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
    public void testConcurrentAppointmentOfDifferentStoreManagers() throws InterruptedException {
        // Create two additional users who will be concurrently appointed as managers
        UUID userId3 = UUID.randomUUID();
        UUID userId4 = UUID.randomUUID();
        addUser(userId3, "Member3", "passpass3", "email3@email.com");
        addUser(userId4, "Member4", "passpass4", "email4@email.com");
        
        // Get owner username
        String ownerUsername = userRepository.getMemberUsername(userId1.toString());
        
        // Get usernames of candidates
        final String candidate1Username = userRepository.getMemberUsername(userId3.toString());
        final String candidate2Username = userRepository.getMemberUsername(userId4.toString());
        
        // Track the results of each thread
        final boolean[] threadSuccess = new boolean[2];
        final String[] threadErrors = new String[2];
        
        // Create two threads, each attempting to appoint a different manager
        Thread thread1 = new Thread(() -> {
            Response<Void> response = marketService.appointStoreManager(
                tokenId1, ownerUsername, candidate1Username, storeId1);
            threadSuccess[0] = !response.errorOccurred();
            if (response.errorOccurred()) {
                threadErrors[0] = response.getErrorMessage();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            Response<Void> response = marketService.appointStoreManager(
                tokenId1, ownerUsername, candidate2Username, storeId1);
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
        
        // Print diagnostic information
        System.out.println("Thread 1 success: " + threadSuccess[0]);
        if (!threadSuccess[0]) {
            System.out.println("Thread 1 error: " + threadErrors[0]);
        }
        
        System.out.println("Thread 2 success: " + threadSuccess[1]);
        if (!threadSuccess[1]) {
            System.out.println("Thread 2 error: " + threadErrors[1]);
        }
        
        // Verify the final state using the new isStoreManager method
        if (threadSuccess[0]) {
            boolean isManager = marketFacade.isStoreManager(candidate1Username, storeId1);
            assertTrue(isManager, "Candidate 1 should be a manager if appointment succeeded");
        }
        
        if (threadSuccess[1]) {
            boolean isManager = marketFacade.isStoreManager(candidate2Username, storeId1);
            assertTrue(isManager, "Candidate 2 should be a manager if appointment succeeded");
        }
        
        // Verify at least one appointment succeeded
        assertTrue(threadSuccess[0] || threadSuccess[1], 
            "At least one manager appointment should succeed");
    }
    
    @Test
    public void givenValidUsers_whenAppointingStoreOwner_thenStoreOwnerIsAppointed() {
        String appointerUsername = userRepository.getMemberUsername(userId1.toString());
        String appointeeUsername = userRepository.getMemberUsername(userId2.toString());
        Response<Void> response = marketService.appointStoreOwner(tokenId1, appointerUsername, appointeeUsername, storeId1);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void testConcurrentAppointmentOfDifferentStoreOwners() throws InterruptedException {
        // Create two additional users who will be concurrently appointed as owners
        UUID userId3 = UUID.randomUUID();
        UUID userId4 = UUID.randomUUID();
        addUser(userId3, "Member3", "passpass3", "email3@email.com");
        addUser(userId4, "Member4", "passpass4", "email4@email.com");
        
        // Get owner username
        String ownerUsername = userRepository.getMemberUsername(userId1.toString());
        
        // Get usernames of candidates
        final String candidate1Username = userRepository.getMemberUsername(userId3.toString());
        final String candidate2Username = userRepository.getMemberUsername(userId4.toString());
        
        // Track the results of each thread
        final boolean[] threadSuccess = new boolean[2];
        final String[] threadErrors = new String[2];
        
        // Create two threads, each attempting to appoint a different owner
        Thread thread1 = new Thread(() -> {
            Response<Void> response = marketService.appointStoreOwner(
                tokenId1, ownerUsername, candidate1Username, storeId1);
            threadSuccess[0] = !response.errorOccurred();
            if (response.errorOccurred()) {
                threadErrors[0] = response.getErrorMessage();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            Response<Void> response = marketService.appointStoreOwner(
                tokenId1, ownerUsername, candidate2Username, storeId1);
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
        
        // Print diagnostic information
        System.out.println("Thread 1 success: " + threadSuccess[0]);
        if (!threadSuccess[0]) {
            System.out.println("Thread 1 error: " + threadErrors[0]);
        }
        
        System.out.println("Thread 2 success: " + threadSuccess[1]);
        if (!threadSuccess[1]) {
            System.out.println("Thread 2 error: " + threadErrors[1]);
        }
        
        // Verify the final state using the new isStoreOwner method
        if (threadSuccess[0]) {
            boolean isOwner = marketFacade.isStoreOwner(candidate1Username, storeId1);
            assertTrue(isOwner, "Candidate 1 should be an owner if appointment succeeded");
        }
        
        if (threadSuccess[1]) {
            boolean isOwner = marketFacade.isStoreOwner(candidate2Username, storeId1);
            assertTrue(isOwner, "Candidate 2 should be an owner if appointment succeeded");
        }
        
        // Verify at least one appointment succeeded
        assertTrue(threadSuccess[0] || threadSuccess[1], 
            "At least one owner appointment should succeed");
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


     @Test
    public void testNotificationService_ReturnsFailure() {
        // Create a notification service that returns a failure response
        INotificationService badNotificationService = new INotificationService() {
            @Override
            public Response<Boolean> sendNotification(String name, String content) {
                // Return a failure response
                return Response.error("Notification service unavailable");
            }

            @Override
            public void initialize() {
                // Do nothing
            }
        };

        // Replace the mock notification service with our bad one
        Response<Void> updateResponse = marketService.updateNotificationService(tokenId1, badNotificationService);
        assertFalse(updateResponse.errorOccurred(), "Updating notification service should succeed");

        // Try to close a store, which should trigger notifications
        Response<Void> response = marketService.closeStore(tokenId1, storeId1);
        
        // Assert that the operation still succeeds even with notification failures
        // This assumes notifications are not critical for the operation to complete
        assertFalse(response.errorOccurred(), 
            "Store closure should succeed even if notifications fail");
    }

    @Test
    public void testNotificationService_ThrowsException() {
        // Create a notification service that throws an exception
        INotificationService badNotificationService = new INotificationService() {
            @Override
            public Response<Boolean> sendNotification(String name, String content) {
                throw new RuntimeException("Notification service connection error");
            }

            @Override
            public void initialize() {
                // Do nothing
            }
        };

        // Replace the mock notification service with our bad one
        Response<Void> updateResponse = marketService.updateNotificationService(tokenId1, badNotificationService);
        assertFalse(updateResponse.errorOccurred(), "Updating notification service should succeed");

        // Try to close a store, which should trigger notifications
        Response<Void> response = marketService.closeStore(tokenId1, storeId1);
        
        // Assert whether the operation should fail or succeed with notification exceptions
        // This depends on how your system is designed to handle notification failures
        // Option 1: If notifications are critical and failures should stop operations
        assertTrue(response.errorOccurred(), 
            "Store closure should fail when notifications throw exceptions");
        assertTrue(response.getErrorMessage().contains("notification") || 
                response.getErrorMessage().contains("error"), 
            "Error message should mention notification failure");
        
        // Option 2: If notifications are non-critical and operations should proceed
        // assertFalse(response.errorOccurred(), 
        //     "Store closure should succeed even if notifications throw exceptions");
    }

    @Test
    public void testNotificationService_FailsDuringInitialization() {
        // Create a notification service that fails during initialization
        INotificationService badNotificationService = new INotificationService() {
            @Override
            public Response<Boolean> sendNotification(String name, String content) {
                return Response.success(true);
            }

            @Override
            public void initialize() {
                throw new RuntimeException("Failed to initialize notification service");
            }
        };

        // Replace the mock notification service with our bad one
        Response<Void> updateResponse = marketService.updateNotificationService(tokenId1, badNotificationService);
        assertFalse(updateResponse.errorOccurred(), "Updating notification service should succeed");

        // Try to open the market, which should trigger notification service initialization
        Response<Void> response = marketService.openMarket(tokenId1);
        
        // Assert
        assertTrue(response.errorOccurred(), 
            "Market opening should fail when notification service fails to initialize");
        assertTrue(response.getErrorMessage().contains("initialize") || 
                response.getErrorMessage().contains("notification"), 
            "Error message should mention initialization failure");
    }

    @Test
    public void testMarketCloseStore_WithNotificationFailure() {
        // Create a notification service that returns failures for some users but not others
        INotificationService selectiveFailureService = new INotificationService() {
            @Override
            public Response<Boolean> sendNotification(String name, String content) {
                // Fail for specific usernames (e.g., even-length names)
                return Response.error("Failed to notify user: " + name);
                
            }

            @Override
            public void initialize() {
                // Initialize successfully
            }
        };

        // Replace the mock notification service with our selective failure service
        Response<Void> updateResponse = marketService.updateNotificationService(tokenId1, selectiveFailureService);
        assertFalse(updateResponse.errorOccurred(), "Updating notification service should succeed");

        // Appoint a store manager to increase the number of users to be notified
        String appointerUsername = userRepository.getMemberUsername(userId1.toString());
        String appointeeUsername = userRepository.getMemberUsername(userId2.toString());
        Response<Void> appointResponse = marketService.appointStoreManager(
            tokenId1, appointerUsername, appointeeUsername, storeId1);
        assertFalse(appointResponse.errorOccurred(), "Appointing store manager should succeed");

        // Try to close the store market-wide, which should trigger notifications to multiple users
        Response<Void> response = marketService.marketCloseStore(tokenId1, storeId1);
        
        // Depending on how your system handles partial notification failures:
        // Option 1: If any notification failure should cause the operation to fail
        assertTrue(response.errorOccurred(), 
            "Market store closure should fail when some notifications fail");
        assertTrue(response.getErrorMessage().contains("notification") || 
                response.getErrorMessage().contains("failed"), 
            "Error message should mention notification failure");
        
        // Option 2: If notification failures should be logged but not stop the operation
        // assertFalse(response.errorOccurred(), 
        //     "Market store closure should succeed even if some notifications fail");
    }

    @Test
    public void testGetNotificationService_AfterFailure() {
        // Create a notification service that will fail
        INotificationService badNotificationService = new INotificationService() {
            @Override
            public Response<Boolean> sendNotification(String name, String content) {
                return Response.error("Notification service unavailable");
            }

            @Override
            public void initialize() {
                // Do nothing
            }
        };

        // Replace the mock notification service with our bad one
        Response<Void> updateResponse = marketService.updateNotificationService(tokenId1, badNotificationService);
        assertFalse(updateResponse.errorOccurred(), "Updating notification service should succeed");

        // Try to get the notification service
        Response<INotificationService> response = marketService.getNotificationService(tokenId1);
        
        // Assert that we can still get the service even if it's failing
        assertFalse(response.errorOccurred(), "Getting notification service should succeed");
        assertNotNull(response.getValue(), "Notification service should not be null");
        
        // Test that the returned service is indeed our bad one
        Response<Boolean> notifyResponse = response.getValue().sendNotification("testUser", "Test content");
        assertTrue(notifyResponse.errorOccurred(), "The returned notification service should fail when used");
        assertEquals("Notification service unavailable", notifyResponse.getErrorMessage());
    }   

}