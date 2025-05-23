package StoreTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import Application.CustomerServiceService;
import Application.ServiceManager;
import Application.StoreService;
import Application.TokenService;
import Domain.FacadeManager;
import Domain.IRepoManager;
import Domain.Pair;
import Domain.Store.FeedbackDTO;
import Domain.Store.IItemRepository;
import Domain.Store.Item;
import Domain.User.IUserRepository;
import Domain.User.Member;
import Domain.User.User;
import Infrastructure.MemoryRepoManager;
import Infrastructure.ExternalPaymentService;
import Application.utils.Response;


public class CustomerServiceServiceTests {
    private CustomerServiceService customerServiceService;
    private StoreService storeService;
    private IItemRepository itemRepository;
    private IUserRepository userRepository;
    private TokenService tokenService;
    private ServiceManager serviceManager;
    private FacadeManager facadeManager;
    private IRepoManager repoManager;

    UUID userId = UUID.randomUUID();
    String storeId;
    Pair<String, String> itemId;
    String productId;


    String tokenId = null;

    @Before
    public void setUp() {

        this.repoManager = new MemoryRepoManager();
        this.facadeManager = new FacadeManager(repoManager, mock(ExternalPaymentService.class));
        this.serviceManager = new ServiceManager(facadeManager);
        userRepository = repoManager.getUserRepository();
        itemRepository = repoManager.getItemRepository();
        
        
        customerServiceService = serviceManager.getCustomerService();
        this.tokenService = serviceManager.getTokenService();
        storeService = serviceManager.getStoreService();

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
    public void GivenExistingMemberAndExistingStoreAndExistingItemAndNonemptyFeedbacks_WhenGettingFeedbackByStore_ThenReturnFeedbacks() {
        String feedback = "Ayo those test cases are fire";
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback);
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback+"2");

        Response<List<FeedbackDTO>> getResult = customerServiceService.getAllFeedbacksByStoreId(this.tokenId, storeId);
        assertEquals(getResult.getValue().size(), 2);
    }









    @Test
    public void GivenExistingMemberAndExistingStoreAndExistingItemAndNonemptyFeedbacks_WhenGettingFeedbackByProduct_ThenReturnFeedbacks() {
        String feedback = "Ayo those test cases are fire";
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback);
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback+"2");

        Response<List<FeedbackDTO>> getResult = customerServiceService.getAllFeedbacksByProductId(this.tokenId, productId);
        assertEquals(getResult.getValue().size(), 2);
    }

    @Test   
    public void GivenExistingMemberAndExistingStoreAndExistingItemAndNonemptyFeedbacks_WhenGettingFeedbackByUser_ThenReturnFeedbacks() {
        String feedback = "Ayo those test cases are fire";
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback);
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback+"2");

        Response<List<FeedbackDTO>> getResult = customerServiceService.getAllFeedbacksByUserId(this.tokenId, userId.toString());
        assertEquals(getResult.getValue().size(), 2);
    }

    @Test
    public void GivenExistingMemberAndExistingStoreAndExistingItemAndNonemptyFeedbacks_WhenGettingFeedbackByUser_ThenReturnEmptyList() {
        String feedback = "Ayo those test cases are fire";
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback);
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback+"2");

        Response<List<FeedbackDTO>> getResult = customerServiceService.getAllFeedbacksByUserId(this.tokenId, "nonexistinguser");
        assertTrue(getResult.getValue().isEmpty());
    }

    @Test
    public void GivenExistingMemberAndExistingStoreAndExistingItemAndNonemptyFeedbacks_WhenGettingFeedbackByStore_ThenReturnEmptyList() {
        String feedback = "Ayo those test cases are fire";
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback);
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback+"2");

        Response<List<FeedbackDTO>> getResult = customerServiceService.getAllFeedbacksByStoreId(this.tokenId, "nonexistingstore");
        assertTrue(getResult.getValue().isEmpty());
    }

    @Test
    public void GivenExistingMemberAndExistingStoreAndExistingItemAndNonemptyFeedbacks_WhenGettingFeedbackByProduct_ThenReturnEmptyList() {
        String feedback = "Ayo those test cases are fire";
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback);
        customerServiceService.addFeedback(tokenId, storeId, productId, feedback+"2");

        Response<List<FeedbackDTO>> getResult = customerServiceService.getAllFeedbacksByProductId(this.tokenId, "nonexistingproduct");
        assertTrue(getResult.getValue().isEmpty());
    }
}
