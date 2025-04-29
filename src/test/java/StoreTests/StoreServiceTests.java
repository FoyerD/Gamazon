package StoreTests;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import Application.AuctionDTO;
import Application.MarketService;
import Application.Response;
import Application.StoreDTO;
import Application.StoreService;
import Domain.TokenService;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IStoreRepository;
import Domain.Store.MemoryItemRepository;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;
import Domain.User.Member;
import Infrastructure.MemoryAuctionRepository;
import Infrastructure.MemoryFeedbackRepository;
import Infrastructure.MemoryUserRepository;
import Infrastructure.StoreRepositoryMemory;
import Domain.User.User;
import Domain.Pair;
import Domain.Store.Auction;
import Domain.Store.Store;
import Domain.Store.Product;
import Domain.Store.Item;


public class StoreServiceTests {

    private StoreService storeService;
    private StoreFacade storeFacade;
    private IStoreRepository storeRepository;
    private IAuctionRepository auctionRepository;
    private IItemRepository itemRepository;
    private IFeedbackRepository feedbackRepository;
    private IUserRepository userRepository;
    private TokenService tokenService;
    UUID userId = UUID.randomUUID();
    String tokenId = null;

    @Before
    public void setUp() {
        this.storeRepository = new StoreRepositoryMemory();
        this.auctionRepository = new MemoryAuctionRepository();
        this.itemRepository = new MemoryItemRepository();
        this.feedbackRepository= new MemoryFeedbackRepository();
        this.userRepository = new MemoryUserRepository();

        this.tokenService = new TokenService();
        this.storeFacade = new StoreFacade(storeRepository, feedbackRepository, itemRepository, userRepository, auctionRepository);
        storeService = new StoreService(storeFacade, tokenService);

        
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
        this.storeService.closeStore(this.tokenId, storeId);
        Response<Boolean> result = storeService.openStore(this.tokenId, storeId);
        assertTrue(result.getValue());
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

        Response<AuctionDTO> auctionResult = storeService.addAuction(this.tokenId, storeId, productId, "2023-12-31", 5.0);
        assertEquals(auctionResult.getValue().getStoreId(), storeId);
        assertEquals(auctionResult.getValue().getProductId(), productId);
    }
}