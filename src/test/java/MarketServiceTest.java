import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.Receipt;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Pair;
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
import Domain.FacadeManager;
import Domain.IRepoManager;
import Domain.management.PermissionType;
import Infrastructure.MemoryRepoManager;


import org.junit.Before;
import org.junit.Test;

import Application.ItemService;
import Application.MarketService;
import Application.ProductService;
import Application.ServiceManager;
import Application.StoreService;
import Application.TokenService;
import Application.DTOs.ProductDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import Application.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MarketServiceTest {

    private ServiceManager serviceManager;
    private FacadeManager facadeManager;
    private IRepoManager repositoryManager;

    private MarketService marketService;
    private UserService userService;
    private StoreService storeService;
    private ItemService itemService;
    private ProductService productService;

    private TokenService tokenService;

    private String tokenId1;
    private String tokenId2;
    private UserDTO guest1;
    private UserDTO guest2;
    private UserDTO user1;
    private UserDTO user2;
    private ProductDTO product1;
    private StoreDTO store1;

    IPaymentService mockPaymentService;
    ISupplyService mockSupplyService;
    INotificationService mockNotificationService;
    @Before
    public void setUp() {
        mockPaymentService = mock(IPaymentService.class);
        mockSupplyService = mock(ISupplyService.class);
        mockNotificationService = mock(INotificationService.class);


        repositoryManager = new MemoryRepoManager();
        facadeManager = new FacadeManager(repositoryManager, mockPaymentService);
        serviceManager = new ServiceManager(facadeManager);

        marketService = serviceManager.getMarketService();
        userService = serviceManager.getUserService();
        storeService = serviceManager.getStoreService();
        itemService = serviceManager.getItemService();
        tokenService = serviceManager.getTokenService();
        productService = serviceManager.getProductService();

        guest1 = userService.guestEntry().getValue();
        guest2 = userService.guestEntry().getValue();
        user1 = userService.register(guest1.getSessionToken(), "user1", "WhyWontWork1!","what@walla.com").getValue();
        user2 = userService.register(guest2.getSessionToken(), "user2", "WhyWontWork2!","what1@walla.com").getValue();
        tokenId1 = user1.getSessionToken();
        
        // Configure mockNotificationService to return successful responses by default
        when(mockNotificationService.sendNotification(anyString(), anyString()))
            .thenReturn(Response.success(true));
        

        // Create store
        store1 = storeService.addStore(user1.getSessionToken(), "Store One", "A store for testing").getValue();

        // Add products to store
        product1 = productService.addProduct(user1.getSessionToken(), "prod1", List.of("cat1"),List.of("catDesc1")).getValue();
        itemService.add(user1.getSessionToken(), store1.getId(), product1.getId(), 49.99f, 10, "Item in stock").getValue();
        itemService.add(user1.getSessionToken(), store1.getId(), product1.getId(), 19.99f, 0, "Item out of stock").getValue();


        // Register external services
        marketService.updatePaymentService(tokenId1, mockPaymentService);
        marketService.updateSupplyService(tokenId1, mockSupplyService);
        marketService.updateNotificationService(tokenId1, mockNotificationService);
    }

    public String getUserId(UserDTO user) {
        return tokenService.extractId(user.getSessionToken());
    }

    @Test
    public void givenMarketClosed_whenOpenMarket_thenMarketIsOpened() {
        Response<Void> response = marketService.openMarket(tokenId1);
        assertFalse(response.errorOccurred());
    }

    public void givenValidUsers_whenAppointingStoreManager_thenStoreManagerIsAppointed() {
        String appointerId = getUserId(user1);
        String appointeeId = getUserId(user2);
        Response<Void> response = marketService.appointStoreManager(tokenId1, appointerId, appointeeId, store1.getId());
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenStoreManagerExists_whenRemovingStoreManager_thenManagerIsRemoved() {
        String appointerId = getUserId(user1);
        String appointeeId = getUserId(user2);
        marketService.appointStoreManager(tokenId1, appointerId, appointeeId, store1.getId());
        Response<Void> response = marketService.removeStoreManager(user1.getSessionToken(), appointerId, appointeeId, store1.getId());
        assertFalse(response.errorOccurred());
    }

    //TODO! Refactor test to use services
    // @Test
    // public void testConcurrentAppointmentOfDifferentStoreManagers() throws InterruptedException {
    //     // Create two additional users who will be concurrently appointed as managers
    //     UUID userId3 = UUID.randomUUID();
    //     UUID userId4 = UUID.randomUUID();
    //     addUser(userId3, "Member3", "passpass3", "email3@email.com");
    //     addUser(userId4, "Member4", "passpass4", "email4@email.com");
        
    //     // Get owner username
    //     String ownerUsername = userRepository.getMemberUsername(userId1.toString());
        
    //     // Get usernames of candidates
    //     final String candidate1Username = userRepository.getMemberUsername(userId3.toString());
    //     final String candidate2Username = userRepository.getMemberUsername(userId4.toString());
        
    //     // Track the results of each thread
    //     final boolean[] threadSuccess = new boolean[2];
    //     final String[] threadErrors = new String[2];
        
    //     // Create two threads, each attempting to appoint a different manager
    //     Thread thread1 = new Thread(() -> {
    //         Response<Void> response = marketService.appointStoreManager(
    //             tokenId1, ownerUsername, candidate1Username, storeId1);
    //         threadSuccess[0] = !response.errorOccurred();
    //         if (response.errorOccurred()) {
    //             threadErrors[0] = response.getErrorMessage();
    //         }
    //     });
        
    //     Thread thread2 = new Thread(() -> {
    //         Response<Void> response = marketService.appointStoreManager(
    //             tokenId1, ownerUsername, candidate2Username, storeId1);
    //         threadSuccess[1] = !response.errorOccurred();
    //         if (response.errorOccurred()) {
    //             threadErrors[1] = response.getErrorMessage();
    //         }
    //     });
        
    //     // Start both threads
    //     thread1.start();
    //     thread2.start();
        
    //     // Wait for both threads to complete
    //     thread1.join(5000);  // Wait up to 5 seconds
    //     thread2.join(5000);
        
    //     // Print diagnostic information
    //     System.out.println("Thread 1 success: " + threadSuccess[0]);
    //     if (!threadSuccess[0]) {
    //         System.out.println("Thread 1 error: " + threadErrors[0]);
    //     }
        
    //     System.out.println("Thread 2 success: " + threadSuccess[1]);
    //     if (!threadSuccess[1]) {
    //         System.out.println("Thread 2 error: " + threadErrors[1]);
    //     }
        
    //     // Verify the final state using the new isStoreManager method
    //     if (threadSuccess[0]) {
    //         boolean isManager = marketFacade.isStoreManager(candidate1Username, storeId1);
    //         assertTrue(isManager, "Candidate 1 should be a manager if appointment succeeded");
    //     }
        
    //     if (threadSuccess[1]) {
    //         boolean isManager = marketFacade.isStoreManager(candidate2Username, storeId1);
    //         assertTrue(isManager, "Candidate 2 should be a manager if appointment succeeded");
    //     }
        
    //     // Verify at least one appointment succeeded
    //     assertTrue(threadSuccess[0] || threadSuccess[1], 
    //         "At least one manager appointment should succeed");
    // }
    
    @Test
    public void givenValidUsers_whenAppointingStoreOwner_thenStoreOwnerIsAppointed() {
        Response<Void> response = marketService.appointStoreOwner(tokenId1, getUserId(user1), getUserId(user2), store1.getId());
        assertFalse(response.errorOccurred());
    }

    //TODO! Refactor test to use services
    // @Test
    // public void testConcurrentAppointmentOfDifferentStoreOwners() throws InterruptedException {
    //     // Create two additional users who will be concurrently appointed as owners
    //     UUID userId3 = UUID.randomUUID();
    //     UUID userId4 = UUID.randomUUID();
    //     addUser(userId3, "Member3", "passpass3", "email3@email.com");
    //     addUser(userId4, "Member4", "passpass4", "email4@email.com");
        
    //     // Get owner username
    //     String ownerUsername = userRepository.getMemberUsername(userId1.toString());
        
    //     // Get usernames of candidates
    //     final String candidate1Username = userRepository.getMemberUsername(userId3.toString());
    //     final String candidate2Username = userRepository.getMemberUsername(userId4.toString());
        
    //     // Track the results of each thread
    //     final boolean[] threadSuccess = new boolean[2];
    //     final String[] threadErrors = new String[2];
        
    //     // Create two threads, each attempting to appoint a different owner
    //     Thread thread1 = new Thread(() -> {
    //         Response<Void> response = marketService.appointStoreOwner(
    //             tokenId1, ownerUsername, candidate1Username, storeId1);
    //         threadSuccess[0] = !response.errorOccurred();
    //         if (response.errorOccurred()) {
    //             threadErrors[0] = response.getErrorMessage();
    //         }
    //     });
        
    //     Thread thread2 = new Thread(() -> {
    //         Response<Void> response = marketService.appointStoreOwner(
    //             tokenId1, ownerUsername, candidate2Username, storeId1);
    //         threadSuccess[1] = !response.errorOccurred();
    //         if (response.errorOccurred()) {
    //             threadErrors[1] = response.getErrorMessage();
    //         }
    //     });
        
    //     // Start both threads
    //     thread1.start();
    //     thread2.start();
        
    //     // Wait for both threads to complete
    //     thread1.join(5000);  // Wait up to 5 seconds
    //     thread2.join(5000);
        
    //     // Print diagnostic information
    //     System.out.println("Thread 1 success: " + threadSuccess[0]);
    //     if (!threadSuccess[0]) {
    //         System.out.println("Thread 1 error: " + threadErrors[0]);
    //     }
        
    //     System.out.println("Thread 2 success: " + threadSuccess[1]);
    //     if (!threadSuccess[1]) {
    //         System.out.println("Thread 2 error: " + threadErrors[1]);
    //     }
        
    //     // Verify the final state using the new isStoreOwner method
    //     if (threadSuccess[0]) {
    //         boolean isOwner = marketFacade.isStoreOwner(candidate1Username, storeId1);
    //         assertTrue(isOwner, "Candidate 1 should be an owner if appointment succeeded");
    //     }
        
    //     if (threadSuccess[1]) {
    //         boolean isOwner = marketFacade.isStoreOwner(candidate2Username, storeId1);
    //         assertTrue(isOwner, "Candidate 2 should be an owner if appointment succeeded");
    //     }
        
    //     // Verify at least one appointment succeeded
    //     assertTrue(threadSuccess[0] || threadSuccess[1], 
    //         "At least one owner appointment should succeed");
    // }

    @Test
    public void givenStoreHasPurchases_whenGettingPurchaseHistory_thenReceiptsReturned() {
        Response<List<Receipt>> response = marketService.getStorePurchaseHistory(user1.getSessionToken(), store1.getId());
        assertFalse(response.errorOccurred());
    }

    @Test
    public void givenStoreHasManagers_whenGettingPermissions_thenPermissionsAreReturned() {
        marketService.appointStoreManager(tokenId1, getUserId(user1), getUserId(user2), store1.getId());
        Response<Map<String, List<PermissionType>>> response = marketService.getManagersPermissions(tokenId1, store1.getId());
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().containsKey(getUserId(user2)));
    }

    @Test
    public void givenManagerExists_whenChangingPermissions_thenPermissionsAreUpdated() {
        marketService.appointStoreManager(tokenId1, getUserId(user1), getUserId(user2), store1.getId());
        List<PermissionType> newPermissions = List.of(PermissionType.ADMINISTER_STORE);
        Response<Void> response = marketService.changeManagerPermissions(tokenId1, getUserId(user1), getUserId(user2), store1.getId(), newPermissions);
        assertFalse(response.errorOccurred());
    }


    @Test
    public void givenValidPaymentService_whenUpdatingPaymentService_thenServiceIsUpdated() {
        Response<Void> response = marketService.updatePaymentService(tokenId1, facadeManager.getPaymentService());
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
    public void givenWrongAppointee_whenAppointingStoreManager_thenErrorOccurs() {
        String appointeeUsername = "newManager";
        Response<Void> response = marketService.appointStoreManager(tokenId1, "ownerUser", appointeeUsername, store1.getId());
        assertTrue(response.errorOccurred());
    }

    @Test
    public void givenWrongAppointer_whenRemovingStoreManager_thenErrorOccurs() {
        String managerUsername = "existingManager";
        Response<Void> response = marketService.removeStoreManager(tokenId1, "ownerUser", managerUsername, store1.getId());
        assertTrue(response.errorOccurred());
    }

    @Test
    public void givenWrongApointee_whenAppointingStoreOwner_thenErrorOccurs() {
        String appointeeUsername = "newOwner";
        Response<Void> response = marketService.appointStoreOwner(tokenId1, "ownerUser", appointeeUsername, store1.getId());
        assertTrue(response.errorOccurred());
    }


    @Test
    public void givenInvalidPermissions_whenChangingManagerPermissions_thenErrorOccurs() {
        List<PermissionType> newPermissions = List.of(PermissionType.ADMINISTER_STORE);
        Response<Void> response = marketService.changeManagerPermissions(tokenId1, "ownerUser", "managerUser", store1.getId(), newPermissions);
        assertTrue(response.errorOccurred());
    }


    @Test
    public void givenEmptyPurchaseHistory_whenGettingStoreHistory_thenEmptyListReturned() {
        Response<List<Receipt>> response = marketService.getStorePurchaseHistory(tokenId1, store1.getId());
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().isEmpty());
    }

    @Test
    public void givenNoPermission_whenGettingStorePurchaseHistory_thenErrorOccurs() {
        Response<List<Receipt>> response = marketService.getStorePurchaseHistory(tokenId2, store1.getId());
        assertTrue(response.errorOccurred());
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

    //TODO! Refactor test to use services
    // @Test
    // public void testNotificationService_ReturnsFailure() {
    //     // Create a notification service that returns a failure response
    //     INotificationService badNotificationService = new INotificationService() {
    //         @Override
    //         public Response<Boolean> sendNotification(String name, String content) {
    //             // Return a failure response
    //             return Response.error("Notification service unavailable");
    //         }

    //         @Override
    //         public void initialize() {
    //             // Do nothing
    //         }
    //     };

    //     // Replace the mock notification service with our bad one
    //     Response<Void> updateResponse = marketService.updateNotificationService(tokenId1, badNotificationService);
    //     assertFalse(updateResponse.errorOccurred(), "Updating notification service should succeed");

    //     // Try to close a store, which should trigger notifications
    //     Response<Void> response = marketService.closeStore(tokenId1, storeId1);
        
    //     // Assert that the operation still succeeds even with notification failures
    //     // This assumes notifications are not critical for the operation to complete
    //     assertFalse(response.errorOccurred(), 
    //         "Store closure should succeed even if notifications fail");
    // }

    //TODO! Refactor test to use services
    // @Test
    // public void testNotificationService_ThrowsException() {
    //     // Create a notification service that throws an exception
    //     INotificationService badNotificationService = new INotificationService() {
    //         @Override
    //         public Response<Boolean> sendNotification(String name, String content) {
    //             throw new RuntimeException("Notification service connection error");
    //         }

    //         @Override
    //         public void initialize() {
    //             // Do nothing
    //         }
    //     };

    //     // Replace the mock notification service with our bad one
    //     Response<Void> updateResponse = marketService.updateNotificationService(tokenId1, badNotificationService);
    //     assertFalse(updateResponse.errorOccurred(), "Updating notification service should succeed");

    //     // Try to close a store, which should trigger notifications
    //     Response<Void> response = marketService.closeStore(tokenId1, storeId1);
        
    //     // Assert whether the operation should fail or succeed with notification exceptions
    //     // This depends on how your system is designed to handle notification failures
    //     // Option 1: If notifications are critical and failures should stop operations
    //     assertTrue(response.errorOccurred(), 
    //         "Store closure should fail when notifications throw exceptions");
    //     assertTrue(response.getErrorMessage().contains("notification") || 
    //             response.getErrorMessage().contains("error"), 
    //         "Error message should mention notification failure");
        
    //     // Option 2: If notifications are non-critical and operations should proceed
    //     // assertFalse(response.errorOccurred(), 
    //     //     "Store closure should succeed even if notifications throw exceptions");
    // }

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

    //TODO! Refactor test to use services
    // @Test
    // public void testMarketCloseStore_WithNotificationFailure() {
    //     // Create a notification service that returns failures for some users but not others
    //     INotificationService selectiveFailureService = new INotificationService() {
    //         @Override
    //         public Response<Boolean> sendNotification(String name, String content) {
    //             // Fail for specific usernames (e.g., even-length names)
    //             return Response.error("Failed to notify user: " + name);
                
    //         }

    //         @Override
    //         public void initialize() {
    //             // Initialize successfully
    //         }
    //     };

    //     // Replace the mock notification service with our selective failure service
    //     Response<Void> updateResponse = marketService.updateNotificationService(tokenId1, selectiveFailureService);
    //     assertFalse(updateResponse.errorOccurred(), "Updating notification service should succeed");

    //     // Appoint a store manager to increase the number of users to be notified
    //     String appointerUsername = userRepository.getMemberUsername(userId1.toString());
    //     String appointeeUsername = userRepository.getMemberUsername(userId2.toString());
    //     Response<Void> appointResponse = marketService.appointStoreManager(
    //         tokenId1, appointerUsername, appointeeUsername, storeId1);
    //     assertFalse(appointResponse.errorOccurred(), "Appointing store manager should succeed");

    //     // Try to close the store market-wide, which should trigger notifications to multiple users
    //     Response<Void> response = marketService.marketCloseStore(tokenId1, storeId1);
        
    //     // Depending on how your system handles partial notification failures:
    //     // Option 1: If any notification failure should cause the operation to fail
    //     assertTrue(response.errorOccurred(), 
    //         "Market store closure should fail when some notifications fail");
    //     assertTrue(response.getErrorMessage().contains("notification") || 
    //             response.getErrorMessage().contains("failed"), 
    //         "Error message should mention notification failure");
        
    //     // Option 2: If notification failures should be logged but not stop the operation
    //     // assertFalse(response.errorOccurred(), 
    //     //     "Market store closure should succeed even if some notifications fail");
    // }

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