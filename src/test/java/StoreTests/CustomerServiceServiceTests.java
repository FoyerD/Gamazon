package StoreTests;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

import Application.CustomerServiceService;
import Application.DTOs.UserDTO;
import Application.ItemService;
import Application.ProductService;
import Application.ServiceManager;
import Application.StoreService;
import Application.UserService;
import Application.utils.Response;
import Domain.FacadeManager;
import Domain.IRepoManager;
import Domain.Pair;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.Store.FeedbackDTO;
import Infrastructure.ExternalPaymentService;
import Infrastructure.MemoryRepoManager;

public class CustomerServiceServiceTests {
    private CustomerServiceService customerServiceService;
    private StoreService storeService;
    private ItemService itemService;
    private ProductService productService;
    private UserService userService;
    private ServiceManager serviceManager;
    private FacadeManager facadeManager;
    private IRepoManager repoManager;

    String storeId;
    Pair<String, String> itemId;
    String productId;
    String userId;
    String tokenId = null;

    @Before
    public void setUp() {
        this.repoManager = new MemoryRepoManager();
        this.facadeManager = new FacadeManager(repoManager, mock(ExternalPaymentService.class), mock(IExternalSupplyService.class));
        this.serviceManager = new ServiceManager(facadeManager);
        
        customerServiceService = serviceManager.getCustomerService();
        storeService = serviceManager.getStoreService();
        itemService = serviceManager.getItemService();
        productService = serviceManager.getProductService();
        userService = serviceManager.getUserService();

        UserDTO guest = userService.guestEntry().getValue();
        tokenId = guest.getSessionToken();
        userId = userService.register(tokenId, "testUser", "Password123!", "a@a.com").getValue().getId();
        
        storeId = this.storeService.addStore(tokenId, "TheAwsomStore", "creepy ahhh store").getValue().getId();
        productId = productService.addProduct(tokenId, "Cool Product", List.of("Prod Cat"), List.of("Cat desc")).getValue().getId();
        itemId = new Pair<>(storeId, productId);
        itemService.add(tokenId, storeId, productId, 10.0, 5, "Cool Item");
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

        Response<List<FeedbackDTO>> getResult = customerServiceService.getAllFeedbacksByUserId(this.tokenId, userId);
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