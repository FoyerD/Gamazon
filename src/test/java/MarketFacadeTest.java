// import Domain.management.MarketFacade;
// import Domain.management.Permission;
// import Domain.management.PermissionManager;
// import Domain.management.PermissionType;
// import Domain.ExternalServices.INotificationService;
// import Domain.ExternalServices.IPaymentService;
// import Domain.ExternalServices.ISupplyService;
// import Domain.Shopping.Receipt;
// import Domain.Shopping.ShoppingBasket;
// import Domain.Shopping.ShoppingCartFacade;
// import Domain.Store.Feedback;
// import Domain.Store.IItemRepository;
// import Domain.Store.Item;
// import Domain.Store.StoreFacade;
// import Domain.User.IUserRepository;
// import Domain.User.Member;
// import Domain.User.User;
// import org.junit.Before;
// import org.junit.Test;

// import java.io.IOException;
// import java.time.LocalDateTime;
// import java.util.*;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.contains;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// public class MarketFacadeTest {

//     private MarketFacade marketFacade;
//     private IUserRepository userRepository;
//     private IItemRepository itemRepository;
//     private ShoppingCartFacade shoppingCartFacade;
//     private StoreFacade storeFacade;
//     private IPaymentService paymentService;
//     private ISupplyService supplyService;
//     private INotificationService notificationService;
//     private PermissionManager permissionManager;

//     @Before
//     public void setUp() {
//         marketFacade = MarketFacade.getInstance();

//         userRepository = mock(IUserRepository.class);
//         itemRepository = mock(IItemRepository.class);
//         storeFacade = mock(StoreFacade.class);
//         paymentService = mock(IPaymentService.class);
//         supplyService = mock(ISupplyService.class);
//         notificationService = mock(INotificationService.class);
//         shoppingCartFacade = mock(ShoppingCartFacade.class);
//         permissionManager = mock(PermissionManager.class);
    
//         marketFacade.initFacades(userRepository, shoppingCartFacade, permissionManager);
//         marketFacade.updatePaymentService(paymentService);
//         marketFacade.updateSupplyService(supplyService);
//         marketFacade.updateNotificationService(notificationService);

//         marketFacade.getStorePermissions().clear(); // Clear previous tests
//     }

//     @Test
//     public void givenAdminWithSupervisePermission_whenAppointStoreManager_thenManagerIsAppointed() {
//         marketFacade.getStorePermissions().put("store1", new HashMap<>());
//         marketFacade.getStorePermissions().get("store1").put("admin", createPermissionWith(PermissionType.SUPERVISE_MANAGERS));

//         marketFacade.appointStoreManager("admin", "newManager", "store1");

//         assertTrue(marketFacade.getStorePermissions().get("store1").containsKey("newManager"));
//     }

//     @Test
//     public void givenAdminWithSupervisePermission_whenRemoveStoreManager_thenManagerIsRemoved() {
//         marketFacade.getStorePermissions().put("store1", new HashMap<>());
//         Permission managerPermission = createPermissionWith(PermissionType.SUPERVISE_MANAGERS);
//         when(managerPermission.isStoreManager()).thenReturn(true);
//         marketFacade.getStorePermissions().get("store1").put("admin", createPermissionWith(PermissionType.SUPERVISE_MANAGERS));
//         marketFacade.getStorePermissions().get("store1").put("managerUser", managerPermission);

//         marketFacade.removeStoreManager("admin", "managerUser", "store1");

//         verify(managerPermission).setPermissions(eq(Collections.emptySet()));
//         verify(managerPermission).setRole(null);
//     }

//     @Test
//     public void givenAdminWithAssignOwnerPermission_whenAppointStoreOwner_thenOwnerIsAppointed() {
//         marketFacade.getStorePermissions().put("store1", new HashMap<>());
//         marketFacade.getStorePermissions().get("store1").put("admin", createPermissionWith(PermissionType.ASSIGN_OR_REMOVE_OWNERS));

//         marketFacade.appointStoreOwner("admin", "newOwner", "store1");

//         assertTrue(marketFacade.getStorePermissions().get("store1").containsKey("newOwner"));
//     }

//     @Test
//     public void givenAdminWithModifyRightsPermission_whenChangeManagerPermissions_thenPermissionsAreChanged() {
//         Permission managerPermission = createPermissionWith(PermissionType.MODIFY_OWNER_RIGHTS);
//         when(managerPermission.isStoreManager()).thenReturn(true);

//         marketFacade.getStorePermissions().put("store1", new HashMap<>());
//         marketFacade.getStorePermissions().get("store1").put("admin", createPermissionWith(PermissionType.MODIFY_OWNER_RIGHTS));
//         marketFacade.getStorePermissions().get("store1").put("managerUser", managerPermission);

//         List<PermissionType> newPermissions = List.of(PermissionType.HANDLE_INVENTORY);

//         marketFacade.changeManagerPermissions("admin", "managerUser", "store1", newPermissions);

//         verify(managerPermission).setPermissions(new HashSet<>(newPermissions));
//     }

//     @Test
//     public void givenAdminWithSupervisePermission_whenGetManagersPermissions_thenReturnManagerPermissions() {
//         User user = mock(User.class);
//         when(user.getName()).thenReturn("admin");
//         when(userRepository.get(anyString())).thenReturn(user);

//         Permission managerPermission = createPermissionWith(PermissionType.SUPERVISE_MANAGERS);
//         when(managerPermission.isStoreManager()).thenReturn(true);

//         marketFacade.getStorePermissions().put("store1", new HashMap<>());
//         marketFacade.getStorePermissions().get("store1").put("admin", createPermissionWith(PermissionType.SUPERVISE_MANAGERS));
//         marketFacade.getStorePermissions().get("store1").put("managerUser", managerPermission);

//         Map<String, List<PermissionType>> result = marketFacade.getManagersPermissions("store1", "userId");

//         assertTrue(result.containsKey("managerUser"));
//     }

//     @Test
//     public void givenAdminWithAccessRecordsPermission_whenGetStorePurchaseHistory_thenReturnNull() {
//         User user = mock(User.class);
//         when(user.getName()).thenReturn("adminUser");
//         when(userRepository.get(anyString())).thenReturn(user);

//         marketFacade.getStorePermissions().put("store1", new HashMap<>());
//         marketFacade.getStorePermissions().get("store1").put("adminUser", createPermissionWith(PermissionType.ACCESS_PURCHASE_RECORDS));

//         List<Receipt> result = marketFacade.getStorePurchaseHistory("store1", "userId");

//         assertNull(result);  // As per original behavior
//     }

//     @Test
//     public void givenMarketFacade_whenOpenMarket_thenInitializeExternalServices() {
//         Member member = mock(Member.class);
//         when(member.getName()).thenReturn("managerName");
//         when(userRepository.getMember(anyString())).thenReturn(member);

//         marketFacade.openMarket("manager");

//         verify(paymentService).initialize();
//         verify(supplyService).initialize();
//         verify(notificationService).initialize();
//     }


//     // === Helper method ===

//     private Permission createPermissionWith(PermissionType permissionType) {
//         Permission permission = mock(Permission.class);
//         when(permission.hasPermission(permissionType)).thenReturn(true);
//         return permission;
//     }
// }
