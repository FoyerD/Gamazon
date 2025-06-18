package Infrastructure;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import Application.ItemService;
import Application.MarketService;
import Application.ProductService;
import Application.ServiceManager;
import Application.ShoppingService;
import Application.StoreService;
import Application.TokenService;
import Application.UserService;
import Application.DTOs.AuctionDTO;
import Application.DTOs.ProductDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import Domain.FacadeManager;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.ExternalServices.INotificationService;

public class ExternalServiceTest {

    private IExternalPaymentService mockPaymentService;
    private IExternalSupplyService mockSupplyService;
    private INotificationService mockNotificationService;

    private ServiceManager serviceManager;
    private StoreService storeService;
    private ShoppingService shoppingService;
    private MarketService marketService;
    private UserService userService;
    private ProductService productService;
    private ItemService itemService;
    private TokenService tokenService;

    private String userToken;
    private String userId;
    private StoreDTO store;
    private ProductDTO product;

    @Before
    public void setup() {
        mockPaymentService = mock(IExternalPaymentService.class);
        mockSupplyService = mock(IExternalSupplyService.class);
        mockNotificationService = mock(INotificationService.class);

        when(mockPaymentService.processPayment(any(), any(), any(), any(), any(), anyDouble()))
            .thenReturn(Response.success(1));
        when(mockNotificationService.sendNotification(any(), any()))
            .thenReturn(Response.success(true));

        MemoryRepoManager repoManager = new MemoryRepoManager();
        FacadeManager facadeManager = new FacadeManager(repoManager, mockPaymentService, mockSupplyService);
        serviceManager = new ServiceManager(facadeManager);

        storeService = serviceManager.getStoreService();
        shoppingService = serviceManager.getShoppingService();
        marketService = serviceManager.getMarketService();
        userService = serviceManager.getUserService();
        productService = serviceManager.getProductService();
        itemService = serviceManager.getItemService();
        tokenService = serviceManager.getTokenService();

        // Inject mocks into StoreFacade
        facadeManager.getStoreFacade().setNotificationService(mockNotificationService);

        // Register external services
        UserDTO guest = userService.guestEntry().getValue();
        UserDTO user = userService.register(guest.getSessionToken(), "User", "Password1!", "email@example.com").getValue();
        userToken = user.getSessionToken();
        userId = tokenService.extractId(userToken);

        marketService.updateNotificationService(userToken, mockNotificationService);
        marketService.updatePaymentService(userToken, mockPaymentService);
        marketService.updateSupplyService(userToken, mockSupplyService);

        when(mockPaymentService.handshake()).thenReturn(Response.success(true));
        when(mockSupplyService.handshake()).thenReturn(Response.success(true));
        marketService.openMarket(userToken);

        store = storeService.addStore(userToken, "ExternalTestStore", "store for testing").getValue();
        product = productService.addProduct(userToken, "productName", List.of("cat"), List.of("desc")).getValue();
        itemService.add(userToken, store.getId(), product.getId(), 10.0f, 5, "first item");
    }

    @Test
    public void testAcceptBid_CallsPaymentService() {
        String storeId = store.getId();
        String productId = product.getId();

        storeService.openStore(userToken, storeId);
        String dateStr = "2099-12-31 23:59";
        AuctionDTO auctionDTO = storeService.addAuction(userToken, storeId, productId, dateStr, 5.0).getValue();
        String auctionId = auctionDTO.getAuctionId();

        shoppingService.makeBid(auctionId, userToken, 6.0f, "1234567812345678", new Date(), "123", 1L, "Buyer", "Address", "City", "Country", "12345");
        storeService.acceptBid(userToken, storeId, productId, auctionId);

        verify(mockPaymentService, times(1))
            .processPayment(anyString(), anyString(), any(Date.class), anyString(), anyString(), anyDouble());

        verify(mockNotificationService, atLeastOnce())
            .sendNotification(anyString(), contains("won the bid"));
    }

    @Test
    public void testAcceptBid_CallsNotificationService() {
        String storeId = store.getId();
        String productId = product.getId();

        storeService.openStore(userToken, storeId);
        String dateStr = "2099-12-31 23:59";
        AuctionDTO auctionDTO = storeService.addAuction(userToken, storeId, productId, dateStr, 5.0).getValue();
        String auctionId = auctionDTO.getAuctionId();

        shoppingService.makeBid(auctionId, userToken, 6.0f, "1234567812345678", new Date(), "123", 1L, "Buyer", "Address", "City", "Country", "12345");
        storeService.acceptBid(userToken, storeId, productId, auctionId);

        verify(mockNotificationService, atLeastOnce())
            .sendNotification(anyString(), contains("won the bid"));
    }

    @Test
    public void testCloseStore_CallsNotificationService() {
        String storeId = store.getId();

        storeService.openStore(userToken, storeId);
        Response<Boolean> res = storeService.closeStore(userToken, storeId);

        assertFalse(res.errorOccurred());
        verify(mockNotificationService, atLeastOnce())
            .sendNotification(anyString(), contains("permanently"));
    }

    @Test
    public void testMakeBid_CallsNotificationService_WhenOutbid() {
        String storeId = store.getId();
        String productId = product.getId();

        storeService.openStore(userToken, storeId);
        AuctionDTO auctionDTO = storeService.addAuction(userToken, storeId, productId, "2099-12-31 23:59", 5.0).getValue();
        String auctionId = auctionDTO.getAuctionId();

        // First bid – no outbid notification expected
        shoppingService.makeBid(auctionId, userToken, 6.0f, "1234567812345678", new Date(), "123", 1L, "FirstBuyer", "Address", "City", "Country", "12345");

        // Create a second user and bid higher
        UserDTO guest2 = userService.guestEntry().getValue();
        UserDTO user2 = userService.register(guest2.getSessionToken(), "User2", "Password2!", "user2@example.com").getValue();
        String user2Token = user2.getSessionToken();

        shoppingService.makeBid(auctionId, user2Token, 7.0f, "8765432187654321", new Date(), "321", 1L, "SecondBuyer", "AnotherAddress", "AnotherCity", "AnotherCountry", "54321");

        verify(mockNotificationService, atLeastOnce())
            .sendNotification(anyString(), contains("outbid"));
    }

    @Test
    public void testOpenMarket_CallsHandshake() {
        verify(mockPaymentService, atLeastOnce()).handshake();
        verify(mockSupplyService, atLeastOnce()).handshake();
    }

    // Optional test - supply service use not active yet
    // @Test
    // public void testSupplyService_CalledDuringShipping() {
    //     // Implement when supplyService is integrated in checkout
    //     verify(mockSupplyService, atLeastOnce()).supplyOrder(any(), any(), any(), any(), any(), any());
    // }
}
