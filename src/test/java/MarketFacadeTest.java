import Domain.MarketFacade;
import Domain.Permission;
import Domain.PermissionType;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Store.Feedback;
import Domain.Store.IItemRepository;
import Domain.Store.Item;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;
import Domain.User.Member;
import Domain.User.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MarketFacadeTest {

    private MarketFacade marketFacade;
    private IUserRepository userRepository;
    private IItemRepository itemRepository;
    private StoreFacade storeFacade;
    private IPaymentService paymentService;
    private ISupplyService supplyService;
    private INotificationService notificationService;

    @BeforeEach
    void setUp() {
        marketFacade = MarketFacade.getInstance();

        userRepository = mock(IUserRepository.class);
        itemRepository = mock(IItemRepository.class);
        storeFacade = mock(StoreFacade.class);
        paymentService = mock(IPaymentService.class);
        supplyService = mock(ISupplyService.class);
        notificationService = mock(INotificationService.class);

        marketFacade.initFacades(userRepository, itemRepository, storeFacade);
        marketFacade.updatePaymentService(paymentService);
        marketFacade.updateSupplyService(supplyService);
        marketFacade.updateNotificationService(notificationService);
    }

    @Test
    void testOpenMarket_Success() {
        Member marketManager = mock(Member.class);
        when(userRepository.getMemberByUsername(anyString())).thenReturn(marketManager);
        when(marketManager.getName()).thenReturn("managerName");

        marketFacade.openMarket("manager");

        verify(paymentService).initialize();
        verify(supplyService).initialize();
        verify(notificationService).initialize();
    }

    @Test
    void testAddProductsToInventory_AddsNewProduct() {
        User user = mock(User.class);
        when(userRepository.get(anyString())).thenReturn(user);
        when(user.getName()).thenReturn("adminUser");

        Item item = null; // No existing item
        when(itemRepository.getItem(anyString(), anyString())).thenReturn(item);

        // Setting permission manually for testing
        marketFacade.getStorePermissions().put("store1", new HashMap<>());
        marketFacade.getStorePermissions().get("store1").put("adminUser", createPermissionWith(PermissionType.HANDLE_INVENTORY));

        Map<Integer, Integer> productQuantities = new HashMap<>();
        productQuantities.put(1, 5);

        marketFacade.addProductsToInventory("store1", productQuantities, "adminId");

        verify(itemRepository).add(any(), any());
    }

    @Test
    void testCloseStore_Success() {
        User user = mock(User.class);
        Member member = mock(Member.class);

        when(userRepository.get(anyString())).thenReturn(user);
        when(user.getName()).thenReturn("adminUser");
        when(userRepository.getMemberByUsername(anyString())).thenReturn(member);
        when(member.getName()).thenReturn("adminUser");

        marketFacade.getStorePermissions().put("store1", new HashMap<>());
        marketFacade.getStorePermissions().get("store1").put("adminUser", createPermissionWith(PermissionType.DEACTIVATE_STORE));

        marketFacade.closeStore("store1", "userId");

        verify(storeFacade).closeStore("store1");
        verify(notificationService).sendNotification(eq("adminUser"), contains("closed"));
    }

    @Test
    void testUpdatePaymentServiceURL_CallsUnderlyingService() throws IOException {
        marketFacade.updatePaymentServiceURL("http://new-url.com");
        verify(paymentService).updatePaymentServiceURL("http://new-url.com");
    }

    // Helper method
    private Permission createPermissionWith(PermissionType permissionType) {
        Permission permission = mock(Permission.class);
        when(permission.hasPermission(permissionType)).thenReturn(true);
        return permission;
    }
}
