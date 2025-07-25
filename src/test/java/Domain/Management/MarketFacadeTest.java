package Domain.Management;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Application.utils.Response;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.ExternalServices.INotificationService;
import Domain.Repos.IStoreRepository;
import Domain.Repos.IUserRepository;
import Domain.Shopping.Receipt;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Store.Store;
import Domain.User.Member;
import Domain.User.User;
import Domain.management.MarketFacade;
import Domain.management.Permission;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;

public class MarketFacadeTest {

    private MarketFacade marketFacade;
    private IUserRepository userRepository;
    private ShoppingCartFacade shoppingCartFacade;
    private IExternalPaymentService paymentService;
    private IExternalSupplyService supplyService;
    private INotificationService notificationService;
    private PermissionManager permissionManager;
    private IStoreRepository storeRepository;

    @Before
    public void setUp() {
        // Create the mock objects
        userRepository = mock(IUserRepository.class);
        storeRepository = mock(IStoreRepository.class);
        paymentService = mock(IExternalPaymentService.class);
        supplyService = mock(IExternalSupplyService.class);
        notificationService = mock(INotificationService.class);
        shoppingCartFacade = mock(ShoppingCartFacade.class);
        permissionManager = mock(PermissionManager.class);
        
        // Create a real map for permissions that we'll use in tests
        Map<String, Map<String, Permission>> permissionsMap = new HashMap<>();
        when(permissionManager.getAllStorePermissions()).thenReturn(permissionsMap);
        
        Store store = mock(Store.class);
        when(store.addManager(anyString())).thenReturn(true);

        when(storeRepository.get(anyString())).thenReturn(store);
        when(storeRepository.update(anyString(), any(Store.class))).thenReturn(store);
        // Get the singleton instance
        marketFacade = MarketFacade.getInstance();
        
        // Initialize with our mocks
        marketFacade.initFacades(userRepository, storeRepository, shoppingCartFacade, permissionManager);
        marketFacade.updatePaymentService(paymentService);
        marketFacade.updateSupplyService(supplyService);
        marketFacade.updateNotificationService(notificationService);
    }

    @Test
    public void givenAdminWithSupervisePermission_whenAppointStoreManager_thenManagerIsAppointed() {
        // Setup: Create the store map directly in the permissions map
        Map<String, Map<String, Permission>> permissionsMap = new HashMap<>();
        Map<String, Permission> storeMap = new HashMap<>();
        storeMap.put("admin", createPermissionWith(PermissionType.SUPERVISE_MANAGERS));
        permissionsMap.put("store1", storeMap);
        
        // Set up the mock to return our map
        when(permissionManager.getAllStorePermissions()).thenReturn(permissionsMap);
        
        // Set up the mock behavior for appointStoreManager
        doAnswer(invocation -> {
            String appointeeId = invocation.getArgument(1);
            String storeId = invocation.getArgument(2);
            
            // Simulate appointing a manager
            permissionsMap.get(storeId).put(appointeeId, createPermissionWith(PermissionType.HANDLE_INVENTORY));
            return null;
        }).when(permissionManager).appointStoreManager(anyString(), anyString(), anyString());
        
        when(userRepository.getMember("newManager")).thenReturn(mock(Member.class));
        // Act
        marketFacade.appointStoreManager("admin", "newManager", "store1");
        
        // Assert: verify that the manager was appointed
        assertTrue(permissionsMap.get("store1").containsKey("newManager"));
    }

    @Test
    public void givenAdminWithSupervisePermission_whenRemoveStoreManager_thenManagerIsRemoved() {
        // Setup
        String storeId = "store1";
        String removerId = "admin";
        String managerId = "managerUser";
        
        // Execute
        marketFacade.removeStoreOwner(removerId, managerId, storeId);
        
        // Verify that permissionManager.removeStoreManager was called with the correct parameters
        verify(permissionManager).removeStoreOwner(removerId, managerId, storeId);
    }

    @Test
    public void givenAdminWithAssignOwnerPermission_whenAppointStoreOwner_thenOwnerIsAppointed() {
        // Create permissions map with admin having ASSIGN_OR_REMOVE_OWNERS permission
        Map<String, Map<String, Permission>> permissionsMap = new HashMap<>();
        Map<String, Permission> storePermissions = new HashMap<>();
        storePermissions.put("admin", createPermissionWith(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        permissionsMap.put("store1", storePermissions);
        
        // Set up the permissionManager mock to use our map
        when(permissionManager.getAllStorePermissions()).thenReturn(permissionsMap);
        when(userRepository.getMember("newOwner")).thenReturn(mock(Member.class));
        
        // Set up the appointStoreOwner method behavior to modify our map
        doAnswer(invocation -> {
            String appointeeId = invocation.getArgument(1);
            String storeId = invocation.getArgument(2);
            
            // Add the new owner to our permissions map
            permissionsMap.get(storeId).put(appointeeId, createPermissionWith(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
            return null;
        }).when(permissionManager).appointStoreOwner(anyString(), anyString(), anyString());
        
        // Execute the method
        marketFacade.appointStoreOwner("admin", "newOwner", "store1");
        
        // Verify the result
        assertTrue(permissionsMap.get("store1").containsKey("newOwner"), 
                "The new owner should be added to the permissions map");
    }

    @Test
    public void givenAdminWithModifyRightsPermission_whenChangeManagerPermissions_thenPermissionsAreChanged() {
        // Setup
        String storeId = "store1";
        String ownerId = "admin";
        String managerId = "managerUser";
        List<PermissionType> newPermissions = List.of(PermissionType.HANDLE_INVENTORY);
        
        // Create a doAnswer to capture the call to changeManagerPermissions
        doAnswer(invocation -> {
            // This verifies that permissionManager.changeManagerPermissions was called with the expected parameters
            String calledOwnerId = invocation.getArgument(0);
            String calledManagerId = invocation.getArgument(1);
            String calledStoreId = invocation.getArgument(2);
            List<PermissionType> calledNewPermissions = invocation.getArgument(3);
            
            assertEquals(ownerId, calledOwnerId, "Owner ID should match");
            assertEquals(managerId, calledManagerId, "Manager ID should match");
            assertEquals(storeId, calledStoreId, "Store ID should match");
            assertEquals(newPermissions, calledNewPermissions, "New permissions should match");
            
            return null;
        }).when(permissionManager).changeManagerPermissions(anyString(), anyString(), anyString(), anyList());
        
        // Execute
        marketFacade.changeManagerPermissions(ownerId, managerId, storeId, newPermissions);
        
        // Verify that permissionManager.changeManagerPermissions was called
        verify(permissionManager).changeManagerPermissions(ownerId, managerId, storeId, newPermissions);
    }

    @Test
    public void givenAdminWithSupervisePermission_whenGetManagersPermissions_thenReturnManagerPermissions() {
        // Create test data
        Member user = mock(Member.class);
        when(user.getName()).thenReturn("admin");
        when(userRepository.get(anyString())).thenReturn(user);

        Member manager = mock(Member.class);
        when(manager.getName()).thenReturn("managerUser");
        when(userRepository.getMember(anyString())).thenReturn(manager);
        // Create permissions for testing
        Permission adminPermission = createPermissionWith(PermissionType.SUPERVISE_MANAGERS);
        Permission managerPermission = createPermissionWith(PermissionType.HANDLE_INVENTORY);
        when(managerPermission.isStoreManager()).thenReturn(true);
        
        // Mock the permissions structure
        Map<String, Permission> storePermissions = new HashMap<>();
        storePermissions.put("admin", adminPermission);
        storePermissions.put("managerUser", managerPermission);
        
        // Mock the permission manager behavior
        when(permissionManager.getAllPermissionsForStore("store1")).thenReturn(storePermissions);
        
        // Mock the permissions for the manager
        Set<PermissionType> managerPermissions = new HashSet<>();
        managerPermissions.add(PermissionType.HANDLE_INVENTORY);
        when(managerPermission.getPermissions()).thenReturn(managerPermissions);
        
        // Execute the method
        Map<Member, List<PermissionType>> result = marketFacade.getManagersPermissions("store1", "userId");
        
        // Verify the result
        assertTrue(result.containsKey(manager), "Result should contain managerUser");
        assertEquals(1, result.get(manager).size(), "Manager should have 1 permission");
        assertEquals(PermissionType.HANDLE_INVENTORY, result.get(manager).get(0), 
                    "Manager should have HANDLE_INVENTORY permission");
    }

    @Test
    public void givenAdminWithAccessRecordsPermission_whenGetStorePurchaseHistory_thenReturnNull() {
        // Setup user mock
        User user = mock(User.class);
        when(user.getName()).thenReturn("adminUser");
        when(userRepository.get(anyString())).thenReturn(user);
        
        // Setup permission manager mock
        Map<String, Map<String, Permission>> permissionsMap = new HashMap<>();
        Map<String, Permission> storeMap = new HashMap<>();
        storeMap.put("adminUser", createPermissionWith(PermissionType.ACCESS_PURCHASE_RECORDS));
        permissionsMap.put("store1", storeMap);
        when(permissionManager.getAllStorePermissions()).thenReturn(permissionsMap);
        
        // Important: Setup the shoppingCartFacade to return null specifically
        when(shoppingCartFacade.getStorePurchaseHistory("store1")).thenReturn(null);
        
        // Execute
        List<Receipt> result = marketFacade.getStorePurchaseHistory("store1", "userId");
        
        // Assert
        assertNull(result);
    }

    @Test
    public void givenMarketFacade_whenOpenMarket_thenInitializeExternalServices() {
        Member member = mock(Member.class);
        when(member.getName()).thenReturn("managerName");
        when(userRepository.getMember(anyString())).thenReturn(member);

        when(paymentService.handshake()).thenReturn(new Response<>(true));
        when(supplyService.handshake()).thenReturn(new Response<>(true));

        marketFacade.openMarket("manager");

        verify(paymentService).handshake();
        verify(supplyService).handshake();

    }


    @Test
    public void givenUserIsStoreManager_whenCheckIsStoreManager_thenReturnTrue() {
        // Setup
        String storeId = "store1";
        String username = "managerUser";
        
        // Create a mock permission that will return true for isStoreManager
        Permission managerPermission = mock(Permission.class);
        when(managerPermission.isStoreManager()).thenReturn(true);
        
        // Set up the permissionManager to return our mock permission
        when(permissionManager.getPermission(storeId, username)).thenReturn(managerPermission);
        
        // Execute
        boolean isManager = marketFacade.isStoreManager(username, storeId);
        
        // Verify
        assertTrue(isManager, "User should be identified as a store manager");
        verify(managerPermission).isStoreManager();
    }

    @Test
    public void givenUserIsNotStoreManager_whenCheckIsStoreManager_thenReturnFalse() {
        // Setup
        String storeId = "store1";
        String username = "regularUser";
        
        // Create a mock permission that will return false for isStoreManager
        Permission nonManagerPermission = mock(Permission.class);
        when(nonManagerPermission.isStoreManager()).thenReturn(false);
        
        // Set up the permissionManager to return our mock permission
        when(permissionManager.getPermission(storeId, username)).thenReturn(nonManagerPermission);
        
        // Execute
        boolean isManager = marketFacade.isStoreManager(username, storeId);
        
        // Verify
        assertFalse(isManager, "User should not be identified as a store manager");
        verify(nonManagerPermission).isStoreManager();
    }

    @Test
    public void givenUserPermissionDoesNotExist_whenCheckIsStoreManager_thenReturnFalse() {
        // Setup
        marketFacade.getStorePermissions().put("store1", new HashMap<>());
        // No permission entry for "nonExistentUser"
        
        // Execute
        boolean isManager = marketFacade.isStoreManager("nonExistentUser", "store1");
        
        // Verify
        assertFalse(isManager, "Non-existent user should not be identified as a store manager");
    }

    @Test
    public void givenUserIsStoreOwner_whenCheckIsStoreOwner_thenReturnTrue() {
        // Setup
        String storeId = "store1";
        String username = "ownerUser";
        
        // Create a mock permission that will return true for isStoreOwner
        Permission ownerPermission = mock(Permission.class);
        when(ownerPermission.isStoreOwner()).thenReturn(true);
        
        // Set up the permissionManager to return our mock permission
        when(permissionManager.getPermission(storeId, username)).thenReturn(ownerPermission);
        
        // Execute
        boolean isOwner = marketFacade.isStoreOwner(username, storeId);
        
        // Verify
        assertTrue(isOwner, "User should be identified as a store owner");
        verify(ownerPermission).isStoreOwner();
    }

    @Test
    public void givenUserIsNotStoreOwner_whenCheckIsStoreOwner_thenReturnFalse() {
        // Setup
        String storeId = "store1";
        String username = "regularUser";
        
        // Create a mock permission that will return false for isStoreOwner
        Permission nonOwnerPermission = mock(Permission.class);
        when(nonOwnerPermission.isStoreOwner()).thenReturn(false);
        
        // Set up the permissionManager to return our mock permission
        when(permissionManager.getPermission(storeId, username)).thenReturn(nonOwnerPermission);
        
        // Execute
        boolean isOwner = marketFacade.isStoreOwner(username, storeId);
        
        // Verify
        assertFalse(isOwner, "User should not be identified as a store owner");
        verify(nonOwnerPermission).isStoreOwner();
    }

    @Test
    public void givenUserPermissionDoesNotExist_whenCheckIsStoreOwner_thenReturnFalse() {
        // Setup
        marketFacade.getStorePermissions().put("store1", new HashMap<>());
        // No permission entry for "nonExistentUser"
        
        // Execute
        boolean isOwner = marketFacade.isStoreOwner("nonExistentUser", "store1");
        
        // Verify
        assertFalse(isOwner, "Non-existent user should not be identified as a store owner");
    }

    @Test
    public void givenStoreDoesNotExist_whenCheckIsStoreManager_thenReturnFalse() {
        // Setup - Store "nonExistentStore" is not in the permissions map
        
        // Execute
        boolean isManager = marketFacade.isStoreManager("managerUser", "nonExistentStore");
        
        // Verify
        assertFalse(isManager, "User should not be a manager of a non-existent store");
    }

    @Test
    public void givenStoreDoesNotExist_whenCheckIsStoreOwner_thenReturnFalse() {
        // Setup - Store "nonExistentStore" is not in the permissions map
        
        // Execute
        boolean isOwner = marketFacade.isStoreOwner("ownerUser", "nonExistentStore");
        
        // Verify
        assertFalse(isOwner, "User should not be an owner of a non-existent store");
    }

    private Permission createPermissionWith(PermissionType permissionType) {
        Permission permission = mock(Permission.class);
        when(permission.hasPermission(permissionType)).thenReturn(true);
        return permission;
    }
}
