import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.Receipt;
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

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
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
    public void givenValidUsers_whenAppointingStoreOwner_thenStoreOwnerIsAppointed() {
        Response<Void> response = marketService.appointStoreOwner(tokenId1, getUserId(user1), getUserId(user2), store1.getId());
        assertFalse(response.errorOccurred());
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


        

    
}