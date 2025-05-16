package StoreTests;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import Application.ItemService;
import Application.ServiceManager;
import Application.StoreService;
import Application.UserService;
import Application.DTOs.ItemDTO;
import Application.DTOs.ProductDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import Domain.FacadeManager;
import Domain.IRepoManager;
import Domain.Pair;
import Domain.ExternalServices.IPaymentService;
import Domain.Store.ItemFilter;
import Infrastructure.MemoryRepoManager;
import Infrastructure.PaymentService;


public class ItemServiceTests {

    private IRepoManager repoManager;
    private IPaymentService paymentService;
    private FacadeManager facadeManager;
    private ServiceManager serviceManager;
    private ItemService itemService;
    private UserService userService;
    private StoreService storeService;

    private String tokenId;
    private UserDTO guest;
    private UserDTO user1;
    private UserDTO user2;
    private StoreDTO store1;
    private StoreDTO store2;
    private ProductDTO product1;
    private ProductDTO product2;


    @Before
    public void setUp() {
        repoManager = new MemoryRepoManager();
        paymentService = new PaymentService("test");
        facadeManager = new FacadeManager(repoManager, paymentService);
        serviceManager = new ServiceManager(facadeManager);
        itemService = serviceManager.getItemService();
        userService = serviceManager.getUserService();
        storeService = serviceManager.getStoreService();


        guest = userService.guestEntry().getValue();
        Response<UserDTO> resigerRes = userService.register(guest.getSessionToken(), "user1", "WhyWontWork1!","what@walla.com");
        tokenId = resigerRes.getValue().getSessionToken();
        

        Response<StoreDTO> storeREs1 = storeService.addStore(tokenId, "Store One", "desc1");
        store1 = storeREs1.getValue();
        store2 = storeService.addStore(tokenId, "Store Two", "desc2").getValue();
        product1 = serviceManager.getProductService().addProduct(tokenId, "prod1", List.of("cat1"), List.of("desc1")).getValue();
        product2 = serviceManager.getProductService().addProduct(tokenId, "prod2", List.of("cat2"), List.of("desc2")).getValue();
        

        itemService.add(tokenId, store1.getId(), product1.getId(), 49.99f, 10, "In Stock Item");
        itemService.add(tokenId, store1.getId(), "out-of-stock-product", 19.99f, 0, "Out of Stock Item");
        itemService.add(tokenId, store1.getId(), product2.getId(), 39.99f, 5, "Another Stocked Item");
    }

    @Test
    public void GivenValidStoreAndProduct_WhenGetItem_ThenReturnItem() {
        Response<ItemDTO> response = itemService.getItem(tokenId, store1.getId(), product1.getId());
        assertFalse(response.errorOccurred());
        assertNotNull(response.getValue());
    }

    @Test
    public void GivenInvalidStoreOrProduct_WhenGetItem_ThenReturnError() {
        Response<ItemDTO> response = itemService.getItem(tokenId, "invalid", "invalid");
        assertTrue(response.errorOccurred());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    public void GivenValidItem_WhenChangePrice_ThenReturnTrue() {
        Response<Boolean> response = itemService.changePrice(tokenId, store1.getId(), product1.getId(), 99.99f);
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue());
    }

    @Test
    public void GivenInvalidPrice_WhenChangePrice_ThenReturnError() {
        Response<Boolean> response = itemService.changePrice(tokenId, store1.getId(), product1.getId(), -10f);
        assertTrue(response.errorOccurred());
    }

    @Test
    public void GivenMatchingFilter_WhenFilterItems_ThenReturnNonEmptyList() {
        ItemFilter filter = new ItemFilter.Builder().minPrice(10).maxPrice(500).build();
        Response<List<ItemDTO>> response = itemService.filterItems(tokenId, filter);
        assertFalse(response.errorOccurred());
        assertFalse(response.getValue().isEmpty());
    }

    @Test
    public void GivenNonMatchingFilter_WhenFilterItems_ThenReturnEmptyList() {
        ItemFilter filter = new ItemFilter.Builder().minPrice(99999).build();
        Response<List<ItemDTO>> response = itemService.filterItems(tokenId, filter);
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().isEmpty());
    }

    @Test
    public void GivenExistingStoreId_WhenGetItems_ThenReturnItems() {
        Response<List<ItemDTO>> response = itemService.getItemsByStoreId(tokenId, store1.getId());
        assertFalse(response.errorOccurred());
        assertFalse(response.getValue().isEmpty());
    }

    @Test
    public void GivenNonexistentStoreId_WhenGetItems_ThenReturnError() {
        Response<List<ItemDTO>> response = itemService.getItemsByStoreId(tokenId, "invalid-store");
        assertTrue(response.errorOccurred());
    }

    @Test
    public void GivenItemsWithStock_WhenGetAvailableItems_ThenReturnNonEmptyList() {
        Response<List<ItemDTO>> response = itemService.getAvailableItems(tokenId);
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().stream().allMatch(item -> item.getAmount() > 0));
    }

    @Test
    public void GivenValidItem_WhenIncreaseAmount_ThenAmountIncreased() {
        Pair<String, String> id = new Pair<>(store1.getId(), product1.getId());
        int oldAmount = itemService.getItem(tokenId, id.getFirst(), id.getSecond()).getValue().getAmount();

        itemService.increaseAmount(tokenId, id, 3);

        int newAmount = itemService.getItem(tokenId, id.getFirst(), id.getSecond()).getValue().getAmount();
        assertEquals(oldAmount + 3, newAmount);
    }

    @Test
    public void GivenValidItem_WhenDecreaseAmount_ThenAmountDecreased() {
        Pair<String, String> id = new Pair<>(store1.getId(), product1.getId());
        int oldAmount = itemService.getItem(tokenId, id.getFirst(), id.getSecond()).getValue().getAmount();

        itemService.decreaseAmount(tokenId, id, 2);

        int newAmount = itemService.getItem(tokenId, id.getFirst(), id.getSecond()).getValue().getAmount();
        assertEquals(oldAmount - 2, newAmount);
    }

    @Test
    public void GivenTooLargeDecrease_WhenDecreaseAmount_ThenReturnError() {
        Pair<String, String> id = new Pair<>(store1.getId(), product1.getId());
        int amount = itemService.getItem(tokenId, id.getFirst(), id.getSecond()).getValue().getAmount();
        Response<Void> response = itemService.decreaseAmount(tokenId, id, amount + 100);
        assertTrue(response.errorOccurred());
    }

    @Test
    public void GivenNewItem_WhenAdd_ThenReturnTrue() {
        StoreDTO newStore = storeService.addStore(tokenId, "StoreY", "desc").getValue();
        ProductDTO newProduct = serviceManager.getProductService().addProduct(tokenId, "prodY", List.of("c"), List.of("d")).getValue();

        Response<ItemDTO> response = itemService.add(tokenId, newStore.getId(), newProduct.getId(), 19.99f, 3, "Cool Product");
        assertFalse(response.errorOccurred());
    }

    @Test
    public void GivenDuplicateItem_WhenAdd_ThenReturnFalse() {
        Response<ItemDTO> response = itemService.add(tokenId, store1.getId(), product1.getId(), 49.99f, 10, "Duplicate");
        assertTrue(response.errorOccurred());
    }

    @Test
    public void GivenExistingItem_WhenRemove_ThenReturnItem() {
        Pair<String, String> id = new Pair<>(store1.getId(), product1.getId());
        Response<ItemDTO> response = itemService.remove(tokenId, id);
        assertFalse(response.errorOccurred());
        assertEquals(product1.getId(), response.getValue().getProductId());
    }

    @Test
    public void GivenNonexistentItem_WhenRemove_ThenReturnError() {
        Pair<String, String> id = new Pair<>("invalid", "invalid");
        Response<ItemDTO> response = itemService.remove(tokenId, id);
        assertTrue(response.errorOccurred());
    }

    @Test
    public void WhenConcurrentIncreaseAndDecrease_ThenAmountIsStable() throws InterruptedException {
        Pair<String, String> id = new Pair<>(store1.getId(), product1.getId());
        int initialAmount = itemService.getItem(tokenId, id.getFirst(), id.getSecond()).getValue().getAmount();

        Thread[] threads = new Thread[20];
        for (int i = 0; i < 20; i++) {
            int idx = i;
            threads[i] = new Thread(() -> {
                if (idx % 2 == 0)
                    itemService.increaseAmount(tokenId, id, 1);
                else
                    itemService.decreaseAmount(tokenId, id, 1);
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        int finalAmount = itemService.getItem(tokenId, id.getFirst(), id.getSecond()).getValue().getAmount();
        assertEquals(initialAmount, finalAmount);
    }

    @Test
    public void WhenManyThreadsIncreaseAmount_ThenAmountIncreasesCorrectly() throws InterruptedException {
        Pair<String, String> id = new Pair<>(store1.getId(), product1.getId());
        int initialAmount = itemService.getItem(tokenId, id.getFirst(), id.getSecond()).getValue().getAmount();

        Thread[] threads = new Thread[50];
        for (int i = 0; i < 50; i++) {
            threads[i] = new Thread(() -> itemService.increaseAmount(tokenId, id, 1));
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        int finalAmount = itemService.getItem(tokenId, id.getFirst(), id.getSecond()).getValue().getAmount();
        assertEquals(initialAmount + 50, finalAmount);
    }

    @Test
    public void WhenConcurrentUpdatesOnTwoItems_ThenEachIsCorrectlyUpdated() throws InterruptedException {
        StoreDTO storeX = storeService.addStore(tokenId, "StoreX", "desc").getValue();
        ProductDTO productX = serviceManager.getProductService().addProduct(tokenId, "prodX", List.of("cat"), List.of("desc")).getValue();
        itemService.add(tokenId, storeX.getId(), productX.getId(), 19.99f, 30, "Extra Item");

        Pair<String, String> id1 = new Pair<>(store1.getId(), product1.getId());
        Pair<String, String> id2 = new Pair<>(storeX.getId(), productX.getId());

        int initial1 = itemService.getItem(tokenId, id1.getFirst(), id1.getSecond()).getValue().getAmount();
        int initial2 = itemService.getItem(tokenId, id2.getFirst(), id2.getSecond()).getValue().getAmount();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 25; i++) itemService.increaseAmount(tokenId, id1, 1);
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 40; i++) itemService.increaseAmount(tokenId, id2, 1);
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        int final1 = itemService.getItem(tokenId, id1.getFirst(), id1.getSecond()).getValue().getAmount();
        int final2 = itemService.getItem(tokenId, id2.getFirst(), id2.getSecond()).getValue().getAmount();

        assertEquals(initial1 + 25, final1);
        assertEquals(initial2 + 40, final2);
    }
} 
