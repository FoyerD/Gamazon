package StoreTests;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;

import Application.StoreService;
import Domain.TokenService;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IProductRepository;
import Domain.Store.IStoreRepository;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;
import Domain.User.Member;
import Domain.User.User;
import Domain.management.IPermissionRepository;
import Domain.management.PermissionManager;
import Domain.management.RoleType;
import Infrastructure.Repositories.MemoryAuctionRepository;
import Infrastructure.Repositories.MemoryFeedbackRepository;
import Infrastructure.Repositories.MemoryItemRepository;
import Infrastructure.Repositories.MemoryPermissionRepository;
import Infrastructure.Repositories.MemoryProductRepository;
import Infrastructure.Repositories.MemoryStoreRepository;
import Infrastructure.Repositories.MemoryUserRepository;
import Domain.Pair;
import Domain.Store.Item;
import Domain.Store.Product;
import Domain.Store.ProductFacade;
import Application.MarketService;
import Application.StoreService;
import Application.DTOs.AuctionDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;


public class StoreServiceTests {

    private StoreService storeService;
    private StoreFacade storeFacade;
    private IStoreRepository storeRepository;
    private IAuctionRepository auctionRepository;
    private IItemRepository itemRepository;
    private IFeedbackRepository feedbackRepository;
    private IUserRepository userRepository;
    private TokenService tokenService;
    private PermissionManager permissionManager;
    private IPermissionRepository permissionRepository;
    UUID userId;
    String tokenId = null;

    @Before
    public void setUp() {
        userId = UUID.randomUUID();
        this.storeRepository = new MemoryStoreRepository();
        this.auctionRepository = new MemoryAuctionRepository();
        this.itemRepository = new MemoryItemRepository();
        this.feedbackRepository= new MemoryFeedbackRepository();
        this.userRepository = new MemoryUserRepository();
        this.permissionRepository = new MemoryPermissionRepository();
        
        this.tokenService = new TokenService();
        this.permissionManager = new PermissionManager(permissionRepository);
        this.storeFacade = new StoreFacade(storeRepository, feedbackRepository, itemRepository, userRepository, auctionRepository);
        storeService = new StoreService(storeFacade, tokenService, permissionManager);

        
        tokenId = this.tokenService.generateToken(userId.toString());
        User user = new Member(userId, "Member1", "passpass", "email@email.com");
        this.userRepository.add(userId.toString(), user);
    }

    @Test
    public void GivenExistingMemberAndNewStore_WhenAddStore_ThenReturnStore() {
        String storeName = "NewStore";
        Response<StoreDTO> result = storeService.addStore(this.tokenId, storeName, "A new store");
        assertTrue(result.getValue() != null);

    }

    @Test
    public void GivenExistingMemberExistingStoreName_WhenAddStore_ThenReturnError() {
        String storeName = "ExistingStore";
        storeService.addStore(this.tokenId, storeName, "A new store");
        Response<StoreDTO> result = storeService.addStore(this.tokenId, storeName,"I should not exist");
        assertTrue(result.errorOccurred());
    }

    @Test
    public void GivenExistingMemberClosedStore_WhenOpenStore_ThenReturnTrue() {
        String storeName = "ExistingStore";
        Response<StoreDTO> response = storeService.addStore(this.tokenId, storeName, "A new store");
        String storeId = response.getValue().getId();
        Response<Boolean> resultCLose = this.storeService.closeStore(this.tokenId, storeId);
        Response<Boolean> resultOpen = storeService.openStore(this.tokenId, storeId);
        assertTrue(resultOpen.getValue());
    }

    @Test
    public void GivenExistingMemberOpenStore_WhenOpenStore_ThenReturnError() {
        String storeName = "ExistingStore";
        Response<StoreDTO> storeRes = storeService.addStore(this.tokenId, storeName, "A new store");
        String storeId = storeRes.getValue().getId();
        Response<Boolean> result = storeService.openStore(this.tokenId, storeId);
        assertTrue(result.errorOccurred());
    }
    @Test
    public void GivenExistingMemberOpenStore_WhenCloseStore_ThenReturnTrue() {
        String storeName = "StoreToClose";
        Response<StoreDTO> storeRes = storeService.addStore(this.tokenId, storeName, "Temporary store");
        String storeId = storeRes.getValue().getId();
        Response<Boolean> result = storeService.closeStore(this.tokenId, storeId.toString());
        assertTrue(result.getValue());
    }
    @Test
    public void GivenExistingMemberClosedStore_WhenCloseStore_ThenReturnFalse() {
        String storeName = "ExistingStore";
        Response<StoreDTO> response = storeService.addStore(this.tokenId, storeName, "A new store");
        String storeId = response.getValue().getId();
        this.storeService.closeStore(this.tokenId, storeId);
        Response<Boolean> result = storeService.closeStore(this.tokenId, storeId);
        assertTrue(result.errorOccurred());
    }

    
    @Test
    public void GivenExistingMemberAndNewStore_WhenGetStoreByNameNewStore_ThenReturnStore() {
        String storeName = "NewStore";
        Response<StoreDTO> addResult = storeService.addStore(this.tokenId, storeName, "A new store");
        Response<StoreDTO> getResult = storeService.getStoreByName(this.tokenId, storeName);
        assertTrue(getResult.getValue().getName().equals(storeName));
    }

    @Test
    public void GivenExistingMemberAndNewStore_WhenGetStoreByNameNoneExist_ThenReturnError() {
        String storeName = "NewStore";
        Response<StoreDTO> addResult = storeService.addStore(this.tokenId, storeName, "A new store");
        Response<StoreDTO> getResult = storeService.getStoreByName(this.tokenId, "NoneExist");
        assertTrue(getResult.errorOccurred());
    }

    @Test
    public void GivenExistingMemberAndNewStoreAndNewProduct_WhenAddAuction_ThenReturnAuction() {
        String storeName = "NewStore";
        Response<StoreDTO> addResult = storeService.addStore(this.tokenId, storeName, "A new store");
        String storeId = addResult.getValue().getId();
        String productId = UUID.randomUUID().toString();
        this.itemRepository.add(new Pair<>(storeId, productId), new Item(storeId, productId, 10.0, 10, "A new product"));

        String endDate = "2077-01-01";
        Response<AuctionDTO> auctionResult = storeService.addAuction(this.tokenId, storeId, productId, endDate, 5.0);
        assertEquals(auctionResult.getValue().getStoreId(), storeId);
        assertEquals(auctionResult.getValue().getProductId(), productId);
    }

    @Test
    public void GivenExistingMemberAndAndNewProduct_WhenAddAuctionForNoneexistingStore_ThenReturnError() {
        String storeId = "whatwhat";
        String productId = UUID.randomUUID().toString();
        this.itemRepository.add(new Pair<>(storeId, productId), new Item(storeId, productId, 10.0, 10, "A new product"));

        Date endDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
        Response<AuctionDTO> auctionResult = storeService.addAuction(this.tokenId, storeId, productId, endDate.toString(), 5.0);
        assertTrue(auctionResult.errorOccurred());
    }

    @Test
    public void GivenExistingMemberAndNewStore_WhenAddAuctionForNonExistingItem_ThenReturnError() {
        String storeName = "NewStore";
        Response<StoreDTO> addResult = storeService.addStore(this.tokenId, storeName, "A new store");
        String storeId = addResult.getValue().getId();
        String nonExistingProductId = UUID.randomUUID().toString();

        Date endDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        Response<AuctionDTO> auctionResult = storeService.addAuction(this.tokenId, storeId, nonExistingProductId, endDate.toString(), 5.0);
        assertTrue(auctionResult.errorOccurred());
    }

    @Test
    public void GivenExistingStoreWithAuctions_WhenGetAllAuctions_ThenReturnAllAuctions() {
        String storeName = "AuctionStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store with auctions");
        String storeId = storeResponse.getValue().getId();

        String productId1 = "1";
        String productId2 = "2";
        String productId3 = "3";

        itemRepository.add(new Pair<>(storeId, productId1), new Item(storeId, productId1, 10.0, 10, "Product 1"));
        itemRepository.add(new Pair<>(storeId, productId2), new Item(storeId, productId2, 20.0, 5, "Product 2"));
        itemRepository.add(new Pair<>(storeId, productId3), new Item(storeId, productId3, 30.0, 15, "Product 3"));

        String endDate = "2077-01-01";
        storeService.addAuction(this.tokenId, storeId, productId1, endDate, 5.0);
        storeService.addAuction(this.tokenId, storeId, productId2, endDate, 10.0);
        storeService.addAuction(this.tokenId, storeId, productId3, endDate, 15.0);

        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllStoreAuctions(this.tokenId, storeId);
        assertTrue(auctionsResponse.getValue() != null);
        assertEquals(3, auctionsResponse.getValue().size());
    }

    @Test
    public void GivenStoreWithNoAuctions_WhenGetAllAuctions_ThenReturnEmptyList() {
        String storeName = "EmptyAuctionStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store with no auctions");
        String storeId = storeResponse.getValue().getId();

        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllStoreAuctions(this.tokenId, storeId);
        assertTrue(auctionsResponse.getValue() != null);
        assertEquals(0, auctionsResponse.getValue().size());
    }

    @Test
    public void GivenNonExistingStore_WhenGetAllAuctions_ThenReturnError() {
        String nonExistingStoreId = UUID.randomUUID().toString();

        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllStoreAuctions(this.tokenId, nonExistingStoreId);
        assertTrue(auctionsResponse.errorOccurred());
    }

    @Test
    public void GivenStoreWithAuctions_WhenGetAllProductAuctions_ThenReturnAllAuctions() {
        String productId1 = UUID.randomUUID().toString();
        Response<StoreDTO> storeRes = storeService.addStore(this.tokenId, "storestore", "Store with auctions");
        Response<StoreDTO> storeRes2 = storeService.addStore(this.tokenId, "storestore2", "Store with auctions");

        String storeId1 = storeRes.getValue().getId();
        String storeId2 = storeRes2.getValue().getId();
        itemRepository.add(new Pair<>(storeId1, productId1), new Item(storeId1, productId1, 10.0, 10, "Product 1"));
        itemRepository.add(new Pair<>(storeId2, productId1), new Item(storeId2, productId1, 20.0, 5, "Product 2"));

        String endDate = "2077-01-01";
        Response<AuctionDTO> aucRes1 = storeService.addAuction(tokenId, storeId1, productId1, endDate, 5.0);
        Response<AuctionDTO> aucRes2 = storeService.addAuction(tokenId, storeId2, productId1, endDate, 10.0);

        assertFalse(aucRes1.errorOccurred());
        assertFalse(aucRes2.errorOccurred());

        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllProductAuctions(tokenId, productId1);
        assertTrue(auctionsResponse.getValue() != null);
        assertEquals(2, auctionsResponse.getValue().size());
    }

    @Test
    public void GivenStoreWithNoAuctions_WhenGetAllProductAuctions_ThenReturnEmptyList() {
        String storeName = "EmptyAuctionStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store with no auctions");
        String storeId = storeResponse.getValue().getId();

        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllStoreAuctions(this.tokenId, storeId);
        assertTrue(auctionsResponse.getValue() != null);
        assertEquals(0, auctionsResponse.getValue().size());
    }

    @Test
    public void GivenNonExistingStore_WhenGetAllProductAuctions_ThenReturnError() {
        String nonExistingStoreId = UUID.randomUUID().toString();

        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllStoreAuctions(this.tokenId, nonExistingStoreId);
        assertTrue(auctionsResponse.errorOccurred());
    }
}