import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.Receipt;
import Domain.Store.IItemRepository;
import Domain.Store.IStoreRepository;
import Domain.User.IUserRepository;
import Domain.management.IMarketFacade;
import Domain.management.IPermissionRepository;
import Domain.management.Permission;
import Domain.management.PermissionManager;
import Domain.FacadeManager;
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
    // Use concrete implementations instead of interfaces where appropriate
    private ServiceManager serviceManager;
    private FacadeManager facadeManager;
    private MemoryRepoManager repositoryManager;
    private IMarketFacade marketFacade;  // Added concrete MarketFacade

    private MarketService marketService;
    private UserService userService;
    private StoreService storeService;
    private ItemService itemService;
    private ProductService productService;
    private TokenService tokenService;

    // User related fields
    private String tokenId1;
    private String tokenId2;
    private UserDTO guest1;
    private UserDTO guest2;
    private UserDTO user1;
    private UserDTO user2;
    private ProductDTO product1;
    private StoreDTO store1;
    
    // Repository access through repoManager
    private IUserRepository userRepository;
    private IItemRepository itemRepository;
    private IStoreRepository storeRepository;
    private IPermissionRepository permissionRepository;
    
    // Services that might be mocked for testing
    private IPaymentService mockPaymentService;
    private ISupplyService mockSupplyService;
    private INotificationService mockNotificationService;
    
    @Before
    public void setUp() {
        // Create mocks for external services
        mockPaymentService = mock(IPaymentService.class);
        mockSupplyService = mock(ISupplyService.class);
        mockNotificationService = mock(INotificationService.class);

        // Initialize repository manager
        repositoryManager = new MemoryRepoManager();
        
        // Get repositories through the manager
        userRepository = repositoryManager.getUserRepository();
        itemRepository = repositoryManager.getItemRepository();
        storeRepository = repositoryManager.getStoreRepository();
        permissionRepository = repositoryManager.getPermissionRepository();
        
        // Create facade manager with repositories
        facadeManager = new FacadeManager(repositoryManager, mockPaymentService);
        
        // Get direct access to marketFacade
        marketFacade = facadeManager.getMarketFacade();
        
        // Create service manager
        serviceManager = new ServiceManager(facadeManager);

        // Get the services
        marketService = serviceManager.getMarketService();
        userService = serviceManager.getUserService();
        storeService = serviceManager.getStoreService();
        itemService = serviceManager.getItemService();
        tokenService = serviceManager.getTokenService();
        productService = serviceManager.getProductService();

        // Configure mockNotificationService to return successful responses by default
        when(mockNotificationService.sendNotification(anyString(), anyString()))
            .thenReturn(Response.success(true));
        
        // Create test users
        guest1 = userService.guestEntry().getValue();
        guest2 = userService.guestEntry().getValue();
        user1 = userService.register(guest1.getSessionToken(), "user1", "WhyWontWork1!", "what@walla.com").getValue();
        user2 = userService.register(guest2.getSessionToken(), "user2", "WhyWontWork2!", "what1@walla.com").getValue();
        tokenId1 = user1.getSessionToken();
        tokenId2 = user2.getSessionToken();

        // Create store
        store1 = storeService.addStore(user1.getSessionToken(), "Store One", "A store for testing").getValue();

        // Add products to store
        product1 = productService.addProduct(user1.getSessionToken(), "prod1", List.of("cat1"), List.of("catDesc1")).getValue();
        itemService.add(user1.getSessionToken(), store1.getId(), product1.getId(), 49.99f, 10, "Item in stock").getValue();
        itemService.add(user1.getSessionToken(), store1.getId(), product1.getId(), 19.99f, 0, "Item out of stock").getValue();

        // Register external services
        marketService.updatePaymentService(tokenId1, mockPaymentService);
        marketService.updateSupplyService(tokenId1, mockSupplyService);
        marketService.updateNotificationService(tokenId1, mockNotificationService);
    }
    
    // Helper method
    private String getUserId(UserDTO user) {
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

    @Test
    public void testConcurrentAppointmentOfDifferentStoreManagers() throws InterruptedException {
        // Create two additional guests and register them as users using the UserService
        Response<UserDTO> guest3Response = userService.guestEntry();
        Response<UserDTO> guest4Response = userService.guestEntry();
        
        Response<UserDTO> user3Response = userService.register(
            guest3Response.getValue().getSessionToken(), 
            "Member3", 
            "WhyWontWork3!", // Using the password format from your other users
            "email3@email.com"
        );
        
        Response<UserDTO> user4Response = userService.register(
            guest4Response.getValue().getSessionToken(), 
            "Member4", 
            "WhyWontWork4!", 
            "email4@email.com"
        );
        
        UserDTO user3 = user3Response.getValue();
        UserDTO user4 = user4Response.getValue();

        // Get token and user IDs through TokenService
        String appointerId = getUserId(user1);
        String appointee1Id = getUserId(user3);
        String appointee2Id = getUserId(user4);
        
        // Need the store ID from the store created in setup
        String storeId = store1.getId();
        
        // Track the results of each thread
        final boolean[] threadSuccess = new boolean[2];
        final String[] threadErrors = new String[2];
        
        // Create two threads, each attempting to appoint a different manager
        Thread thread1 = new Thread(() -> {
            Response<Void> response = marketService.appointStoreManager(
                tokenId1, appointerId, appointee1Id, storeId);
            threadSuccess[0] = !response.errorOccurred();
            if (response.errorOccurred()) {
                threadErrors[0] = response.getErrorMessage();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            Response<Void> response = marketService.appointStoreManager(
                tokenId1, appointerId, appointee2Id, storeId);
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
        
        // For verification, we need to use the marketService to check manager status
        if (threadSuccess[0]) {
            // We should have a service method to check if a user is a manager
            // Since we don't have it in the provided code, we'll use marketFacade here
            // In a real implementation, you'd want to use a service method
            Response<Map<String, List<PermissionType>>> response = marketService.getManagersPermissions(tokenId1, storeId);
            assertFalse(response.errorOccurred());
            assertTrue(response.getValue().containsKey(appointee1Id), 
                "Candidate 1 should be a manager if appointment succeeded");
        }
        
        if (threadSuccess[1]) {
            Response<Map<String, List<PermissionType>>> response = marketService.getManagersPermissions(tokenId1, storeId);
            assertFalse(response.errorOccurred());
            assertTrue(response.getValue().containsKey(appointee2Id), 
                "Candidate 2 should be a manager if appointment succeeded");
        }
        
        // Verify at least one appointment succeeded
        assertTrue(threadSuccess[0] || threadSuccess[1], 
            "At least one manager appointment should succeed");
    }
    
    @Test
    public void givenValidUsers_whenAppointingStoreOwner_thenStoreOwnerIsAppointed() {
        Response<Void> response = marketService.appointStoreOwner(tokenId1, getUserId(user1), getUserId(user2), store1.getId());
        assertFalse(response.errorOccurred());
    }

    @Test
    public void testConcurrentAppointmentOfDifferentStoreOwners() throws InterruptedException {
        // Create two additional guests and register them as users using the UserService
        Response<UserDTO> guest3Response = userService.guestEntry();
        Response<UserDTO> guest4Response = userService.guestEntry();
        
        Response<UserDTO> user3Response = userService.register(
            guest3Response.getValue().getSessionToken(), 
            "Member3", 
            "WhyWontWork3!", 
            "email3@email.com"
        );
        
        Response<UserDTO> user4Response = userService.register(
            guest4Response.getValue().getSessionToken(), 
            "Member4", 
            "WhyWontWork4!", 
            "email4@email.com"
        );
        
        UserDTO user3 = user3Response.getValue();
        UserDTO user4 = user4Response.getValue();

        // Get IDs through TokenService
        String appointerId = getUserId(user1);
        String appointee1Id = getUserId(user3);
        String appointee2Id = getUserId(user4);
        
        // Use the store ID from the store created in setup
        String storeId = store1.getId();
        
        // Track the results of each thread
        final boolean[] threadSuccess = new boolean[2];
        final String[] threadErrors = new String[2];
        
        // Create two threads, each attempting to appoint a different owner
        Thread thread1 = new Thread(() -> {
            Response<Void> response = marketService.appointStoreOwner(
                tokenId1, appointerId, appointee1Id, storeId);
            threadSuccess[0] = !response.errorOccurred();
            if (response.errorOccurred()) {
                threadErrors[0] = response.getErrorMessage();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            Response<Void> response = marketService.appointStoreOwner(
                tokenId1, appointerId, appointee2Id, storeId);
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
        
        // Both appointments should succeed since multiple users can be owners
        assertTrue(threadSuccess[0], "First owner appointment should succeed");
        assertTrue(threadSuccess[1], "Second owner appointment should succeed");
        
        // Get the permission manager to check permissions properly
        PermissionManager permissionManager = facadeManager.getPermissionManager();
        
        // Verify the first user is a store owner
        Permission appointee1Permission = permissionManager.getPermission(storeId, appointee1Id);
        assertNotNull(appointee1Permission, "Permission for appointee 1 should exist");
        assertTrue(appointee1Permission.isStoreOwner(), "Appointee 1 should be a store owner");
        
        // Verify the second user is a store owner
        Permission appointee2Permission = permissionManager.getPermission(storeId, appointee2Id);
        assertNotNull(appointee2Permission, "Permission for appointee 2 should exist");
        assertTrue(appointee2Permission.isStoreOwner(), "Appointee 2 should be a store owner");
        
        // Additional verification through the permissions API
        // Print out the permissions for debugging
        System.out.println("Appointee 1 role: " + appointee1Permission.getRoleType());
        System.out.println("Appointee 1 permissions: " + appointee1Permission.getPermissions());
        System.out.println("Appointee 2 role: " + appointee2Permission.getRoleType());
        System.out.println("Appointee 2 permissions: " + appointee2Permission.getPermissions());
    }

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

    @Test
    public void testNotificationService_ReturnsFailure() {
        // Create a notification service that returns a failure response
        INotificationService badNotificationService = new INotificationService() {
            @Override
            public Response<Boolean> sendNotification(String name, String content) {
                // Return a failure response
                return Response.error("Notification service unavailable");
            }

        };

        // Replace the mock notification service with our bad one
        Response<Void> updateResponse = marketService.updateNotificationService(tokenId1, badNotificationService);
        assertFalse(updateResponse.errorOccurred(), "Updating notification service should succeed");

        // Open the market which should succeed even with a notification service that might fail
        Response<Void> openResponse = marketService.openMarket(tokenId1);
        assertFalse(openResponse.errorOccurred(), "Market opening should succeed even with problematic notification service");
        
        // Verify that we can get the notification service
        Response<INotificationService> serviceResponse = marketService.getNotificationService(tokenId1);
        assertFalse(serviceResponse.errorOccurred(), "Getting notification service should succeed");
        
        // Verify that sending a notification through this service returns an error
        INotificationService retrievedService = serviceResponse.getValue();
        Response<Boolean> notifyResponse = retrievedService.sendNotification("testUser", "Test notification");
        assertTrue(notifyResponse.errorOccurred(), "Notification should fail with our bad service");
        assertEquals("Notification service unavailable", notifyResponse.getErrorMessage());
    }


    @Test
    public void testAppointStoreManager_WithNotificationFailure() {
        // Create a notification service that always returns failures
        INotificationService failingNotificationService = new INotificationService() {
            @Override
            public Response<Boolean> sendNotification(String name, String content) {
                // Always fail with a specific error message
                return Response.error("Failed to notify user: " + name);
            }

        };

        // Replace the mock notification service with our failing service
        Response<Void> updateResponse = marketService.updateNotificationService(tokenId1, failingNotificationService);
        assertFalse(updateResponse.errorOccurred(), "Updating notification service should succeed");

        // Create test users through UserService
        Response<UserDTO> guest3Response = userService.guestEntry();
        Response<UserDTO> user3Response = userService.register(
            guest3Response.getValue().getSessionToken(), 
            "TestManager",
            "WhyWontWork3!", 
            "testmanager@example.com"
        );
        
        UserDTO user3 = user3Response.getValue();
        
        // Get user IDs from tokens through TokenService
        String appointerId = getUserId(user1);
        String appointeeId = getUserId(user3);
        
        // Try to appoint a store manager, which should trigger notifications
        Response<Void> appointResponse = marketService.appointStoreManager(
            tokenId1, appointerId, appointeeId, store1.getId());
        
        // Test the system's behavior with notification failures
        // The system uses a lenient approach where notification failures don't block the main operation
        assertFalse(appointResponse.errorOccurred(), 
            "Store manager appointment should succeed even if notifications fail");
        
        // Verify the appointment was successful by checking if the user has manager permissions
        Response<Map<String, List<PermissionType>>> permissionsResponse = 
            marketService.getManagersPermissions(tokenId1, store1.getId());
        assertFalse(permissionsResponse.errorOccurred(), "Getting manager permissions should succeed");
        assertTrue(permissionsResponse.getValue().containsKey(appointeeId), 
            "Appointee should be in the list of managers");
        
        // Verify we can still get the notification service
        Response<INotificationService> serviceResponse = marketService.getNotificationService(tokenId1);
        assertFalse(serviceResponse.errorOccurred(), "Getting notification service should succeed");
        
        // Verify that the notification service is indeed our failing one
        INotificationService retrievedService = serviceResponse.getValue();
        Response<Boolean> notifyResponse = retrievedService.sendNotification("testUser", "Test notification");
        assertTrue(notifyResponse.errorOccurred(), "Notification should fail with our service");
        assertEquals("Failed to notify user: testUser", notifyResponse.getErrorMessage());
        
        // Verify that the notification service was actually used during the appointment
        // This is a more advanced check and might require additional mocking or logging checks
        // to verify that the notification service was called with the appropriate parameters
    }

    @Test
    public void testGetNotificationService_AfterFailure() {
        // Create a notification service that will fail
        INotificationService badNotificationService = new INotificationService() {
            @Override
            public Response<Boolean> sendNotification(String name, String content) {
                return Response.error("Notification service unavailable");
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