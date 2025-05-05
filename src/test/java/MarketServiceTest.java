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

    private String tokenId;
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
        tokenId = this.tokenService.generateToken(userId.toString());
        User user = new Member(userId, "Member1", "passpass", "email@email.com");
        this.userRepository.add(userId.toString(), user);

        this.itemFacade = new ItemFacade(itemRepository, productRepository, storeRepository);
        this.itemService = new ItemService(itemFacade);

        storeId1 = marketService.addStore(tokenId, "Store One", "A store for testing").getValue().getId();
        storeId2 = marketService.addStore(tokenId, "Store Two", "Another store for testing").getValue().getId();
        addProduct(storeId1, "Store One", userId.toString(), productId1, "In Stock Item", 49.99f, 10, "In Stock Item");
        addProduct(storeId1, "Store One", userId.toString(), productId2, "Out of Stock Item", 19.99f, 0, "Out of Stock Item");
        addProduct(storeId2, "Store Two", userId.toString(), productId3, "Another Stocked Item", 39.99f, 5, "Another Stocked Item");

    }

    private void addProduct(String storeId, String storeName, String founderId, String productId, String productName, float price, int amount, String itemName) {
        if (productRepository.get(productId) == null) {
            productRepository.add(productId, new Product(productId, productName));
        }
        Item item = new Item(storeId, productId, price, amount, itemName);
    
        itemFacade.add(new Pair<>(storeId, productId), item);
    }

    @Test
    public void testOpenMarket(){
        // Open the market and verify that the market is open
        Response<Void> response = marketService.openMarket(tokenId);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void testAddStore() {
        // Open the store and verify that it is open
        Response<StoreDTO> storeResponse = marketService.addStore(tokenId, "storeName", "A new store");
        assertFalse(storeResponse.errorOccurred());
    }

    @Test
    public void testGetStorePurchaseHistory() {
        Response<List<Receipt>> response = marketService.getStorePurchaseHistory(tokenId, storeId1);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void testGetManagersPermissions() {
        Response<Map<String, List<PermissionType>>> response = marketService.getManagersPermissions(tokenId, storeId1);
        assertFalse(response.errorOccurred());
    }


    @Test
    public void testaddProductsToInventory() {
        Map<String, Integer> products = Map.of(productId1, 5);
        Response<Void> response = marketService.addProductsToInventory(tokenId, storeId1, products);
        assertFalse(response.errorOccurred());    
    }

    @Test
    public void testRemoveProductsFromInventory() {
        Map<String, Integer> productsToRemove = Map.of(productId1, 1);
        Response<Void> response = marketService.removeProductsFromInventory(tokenId, storeId1, productsToRemove);
        assertFalse(response.errorOccurred());
    }

    @Test
    public void testUpdatePaymentService() {
        Response<Void> response = marketService.updatePaymentService(tokenId, mockPaymentService);
        assertFalse(response.errorOccurred());
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
        

    
}