import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.ShoppingBasket;
import Domain.MarketFacade;
import Domain.Pair;
import Domain.PermissionType;
import Domain.TokenService;
import Domain.Store.Feedback;
import Domain.Store.IItemRepository;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;
import Domain.User.LoginManager;
import Domain.User.User;
import Infrastructure.MemoryUserRepository;
import ch.qos.logback.core.subst.Token;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.MemoryItemRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Application.ItemService;
import Application.MarketService;
import Application.Response;
import Application.StoreDTO;
import Application.StoreService;
import Application.UserDTO;
import Application.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MarketServiceTest {

    private MarketFacade marketFacade;
    private IPaymentService mockPaymentService;
    private ISupplyService mockSupplyService;
    private INotificationService mockNotificationService;
    private IUserRepository userRepository;
    private IItemRepository itemRepository;
    private StoreFacade storeFacade;

    private UserService userService;
    private TokenService tokenService;
    private MarketService marketService;
    private String guestToken;
    private StoreService storeService;
    private String storeId;
    private ItemService itemService;
    private String productId;

    @BeforeEach
    public void setUp() {
        // Mock external services
        mockPaymentService = mock(IPaymentService.class);
        mockSupplyService = mock(ISupplyService.class);
        mockNotificationService = mock(INotificationService.class);
        
        storeFacade = new StoreFacade();

        marketFacade = MarketFacade.getInstance();
        marketFacade.initFacades(userRepository, itemRepository, storeFacade);
        marketFacade.updatePaymentService(mockPaymentService);
        marketFacade.updateSupplyService(mockSupplyService);
        marketFacade.updateNotificationService(mockNotificationService);

        tokenService = new TokenService();
        marketService = new MarketService(marketFacade, tokenService);
        
        // User Service setup
        userService = new UserService(new LoginManager(new MemoryUserRepository()), tokenService);
        Response<UserDTO> guestResp = userService.guestEntry();
        guestToken = guestResp.getValue().getSessionToken();

        // //Store Service setup
        // storeService = new StoreService();

        // String storeName = "NewStore";
        // Response<StoreDTO> result = storeService.addStore(guestToken, storeName, "A new store");
        // storeId = result.getValue().getId();

        // // Add a product to the store for testing
        // productId = "prodY";
        // MemoryItemRepository repository = new MemoryItemRepository();
        // ItemFacade facade = new ItemFacade(repository);
        // itemService = new ItemService(facade);
        // Item item = new Item(storeId, productId,19.99f , 3, "Cool Product");
        // Pair<String, String> id = new Pair<>(storeId, productId);

        // itemService.add(id, item);

    }

    @Test
    public void testaddProductsToInventory() {
        Map<String, Integer> products = Map.of(productId, 5);
        marketService.addProductsToInventory(guestToken, storeId, products);
        // Verify that the product was added to the inventory
        Response<Item> itemResponse = itemService.getItem(storeId, productId);
        assertFalse(itemResponse.errorOccurred(), "Item retrieval failed: " + itemResponse.getErrorMessage());
        Item item = itemResponse.getValue();
        assertEquals(5, item.getAmount(), "Product quantity mismatch after adding to inventory");
    }

    @Test
    public void testUpdatePaymentService() {
        Response<Void> response = marketService.updatePaymentService(guestToken, mockPaymentService);
        assertFalse(response.errorOccurred(), "Failed to update payment service: " + response.getErrorMessage());
        verify(mockPaymentService, never()).initialize(); // Ensure no unexpected initialization
    }

    @Test
    public void testUpdateNotificationService() {
        Response<Void> response = marketService.updateNotificationService(guestToken, mockNotificationService);
        assertFalse(response.errorOccurred(), "Failed to update notification service: " + response.getErrorMessage());
    }

    @Test
    public void testUpdateSupplyService() {
        Response<Void> response = marketService.updateSupplyService(guestToken, mockSupplyService);
        assertFalse(response.errorOccurred(), "Failed to update supply service: " + response.getErrorMessage());
    }

    @Test
    public void testUpdatePaymentServiceURL() throws IOException {
        String newUrl = "http://new-payment-service.com";
        Response<Void> response = marketService.updatePaymentServiceURL(guestToken, newUrl);
        assertFalse(response.errorOccurred(), "Failed to update payment service URL: " + response.getErrorMessage());
        verify(mockPaymentService).updatePaymentServiceURL(newUrl);
    }

    @Test
    public void testRemoveProductsFromInventory() {
        Map<String, Integer> productsToRemove = Map.of(productId, 1);
        marketService.removeProductsFromInventory(guestToken, storeId, productsToRemove);
        Response<Item> itemResponse = itemService.getItem(storeId, productId);
        assertFalse(itemResponse.errorOccurred(), "Item retrieval failed: " + itemResponse.getErrorMessage());
        Item item = itemResponse.getValue();
        assertEquals(2, item.getAmount(), "Product quantity mismatch after removing from inventory");
    }

    @Test
    public void testAppointStoreManager() {
        String appointeeUsername = "newManager";
        Response<Void> response = marketService.appointStoreManager(guestToken, "ownerUser", appointeeUsername, storeId);
        assertFalse(response.errorOccurred(), "Failed to appoint store manager: " + response.getErrorMessage());
    }

    @Test
    public void testRemoveStoreManager() {
        String managerUsername = "existingManager";
        Response<Void> response = marketService.removeStoreManager(guestToken, "ownerUser", managerUsername, storeId);
        assertFalse(response.errorOccurred(), "Failed to remove store manager: " + response.getErrorMessage());
    }

    @Test
    public void testAppointStoreOwner() {
        String appointeeUsername = "newOwner";
        Response<Void> response = marketService.appointStoreOwner(guestToken, "ownerUser", appointeeUsername, storeId);
        assertFalse(response.errorOccurred(), "Failed to appoint store owner: " + response.getErrorMessage());
    }

    @Test
    public void testCloseStore() {
        Response<Void> response = marketService.closeStore(guestToken, storeId);
        assertFalse(response.errorOccurred(), "Failed to close store: " + response.getErrorMessage());
    }

    @Test
    public void testMarketCloseStore() {
        Response<Void> response = marketService.marketCloseStore(guestToken, storeId);
        assertFalse(response.errorOccurred(), "Failed to close store by market: " + response.getErrorMessage());
    }

    @Test
    public void testGetManagersPermissions() {
        Response<Map<String, List<PermissionType>>> response = marketService.getManagersPermissions(guestToken, storeId);
        assertFalse(response.errorOccurred(), "Failed to fetch manager permissions: " + response.getErrorMessage());
        assertNotNull(response.getValue(), "Permissions map is null");
    }

    @Test
    public void testRespondToUserMessage() {
        String comment = "Thank you for your feedback!";
        Response<Boolean> response = marketService.respondToUserMessage(guestToken, storeId, productId, "userId", comment);
        assertFalse(response.errorOccurred(), "Failed to respond to user message: " + response.getErrorMessage());
        assertTrue(response.getValue(), "Response to user message was not successful");
    }

    @Test
    public void testGetUserMessage() {
        Response<Feedback> response = marketService.getUserMessage(guestToken, storeId, productId, "userId");
        assertFalse(response.errorOccurred(), "Failed to fetch user message: " + response.getErrorMessage());
        assertNotNull(response.getValue(), "Feedback is null");
    }

    @Test
    public void testGetStorePurchaseHistory() {
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();
        Response<List<ShoppingBasket>> response = marketService.getStorePurchaseHistory(guestToken, storeId, from, to);
        assertFalse(response.errorOccurred(), "Failed to fetch store purchase history: " + response.getErrorMessage());
        assertNotNull(response.getValue(), "Purchase history is null");
    }

    @Test
    public void testOpenMarket() {
        Response<Void> response = marketService.openMarket(guestToken);
        assertFalse(response.errorOccurred(), "Failed to open market: " + response.getErrorMessage());
    }
        

    
}