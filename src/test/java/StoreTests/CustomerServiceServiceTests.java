package StoreTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import Application.CustomerServiceService;
import Application.Response;
import Application.StoreService;
import Domain.Pair;
import Domain.TokenService;
import Domain.Store.FeedbackDTO;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IStoreRepository;
import Domain.Store.Item;
import Domain.Store.MemoryItemRepository;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;
import Domain.User.Member;
import Domain.User.User;
import Infrastructure.MemoryAuctionRepository;
import Infrastructure.MemoryFeedbackRepository;
import Infrastructure.MemoryUserRepository;
import Infrastructure.StoreRepositoryMemory;


public class CustomerServiceServiceTests {
    private CustomerServiceService customerServiceService;
    private StoreService storeService;
    private StoreFacade storeFacade;
    private IStoreRepository storeRepository;
    private IAuctionRepository auctionRepository;
    private IItemRepository itemRepository;
    private IFeedbackRepository feedbackRepository;
    private IUserRepository userRepository;
    private TokenService tokenService;

    UUID userId = UUID.randomUUID();
    String storeId;
    Pair<String, String> itemId;
    String productId;


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
        customerServiceService = new CustomerServiceService(this.storeFacade, this.tokenService);
        storeService = new StoreService(this.storeFacade, this.tokenService);

        tokenId = this.tokenService.generateToken(userId.toString());
        User user = new Member(userId, "Member1", "passpass", "email@email.com");
        this.userRepository.add(userId.toString(), user);

        storeId = this.storeService.addStore(tokenId, "TheAwsomStore", "creepy ahhh store").getValue().getId();
        productId = "productwhatwhat";
        itemId = new Pair<>(storeId, productId);
        Item item = new Item(storeId, productId, 10.0, 100, "an items");
        this.itemRepository.add(itemId, item);
        if (this.itemRepository.get(new Pair<>(storeId, productId)) == null){
            throw new RuntimeException("Item not found in repository");
        }
        
    }

    @Test
    public void GivenExistingMemberAndExistingStoreAndExistingItem_WhenSendingNoneEmptyFeedback_ThenReturnTrue() {
        String feedback = "Ayo those test cases are fire";
        Response<Boolean> result = customerServiceService.addFeedback(tokenId, storeId, productId, feedback);
        assertTrue(result.getValue());
    }


    @Test
    public void GivenExistingMemberAndExistingStoreAndExistingItem_WhenSendingEmptyFeedback_ThenReturnError() {
        String feedback = "";
        Response<Boolean> result = customerServiceService.addFeedback(tokenId, storeId, productId, feedback);
        assertTrue(result.errorOccurred());
    }

    @Test
    public void GivenExistingMemberAndExistingStoreAndExistingItemAndNonemptyFeedback_WhenGettingFeedback_ThenReturnFeedback() {
        String feedback = "Ayo those test cases are fire";
        Response<Boolean> addResult = customerServiceService.addFeedback(tokenId, storeId, productId, feedback);
        Response<FeedbackDTO> getResult = customerServiceService.getFeedback(this.tokenId, storeId, productId);
        assertEquals(getResult.getValue().getComment(), feedback);
    }
}
