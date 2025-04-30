package StoreTests;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import Application.ItemService;
import Application.DTOs.ItemDTO;
import Application.utils.Response;
import Domain.Pair;
import Domain.Store.IItemRepository;
import Domain.Store.IProductRepository;
import Domain.Store.IStoreRepository;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.ItemFilter;
import Domain.Store.Product;
import Domain.Store.Store;
import Infrastructure.StoreRepositoryMemory;
import Infrastructure.Repositories.MemoryItemRepository;
import Infrastructure.Repositories.MemoryProductRepository;

public class ItemServiceTests {

    private ItemService itemService;
    private IItemRepository itemRepository;
    private IProductRepository productRepository;
    private IStoreRepository storeRepository;
    private ItemFacade itemFacade;


    @Before
    public void setUp() {
        itemRepository = new MemoryItemRepository();
        productRepository = new MemoryProductRepository();
        storeRepository = new StoreRepositoryMemory();
    
        itemFacade = new ItemFacade(itemRepository, productRepository, storeRepository);
        itemService = new ItemService(itemFacade);
    
        addStoreAndProduct("1", "Store One", "founder1", "101", "In Stock Item", 49.99f, 10, "In Stock Item");
        addStoreAndProduct("1", "Store One", "founder1", "out-of-stock-product", "Out of Stock Item", 19.99f, 0, "Out of Stock Item");
        addStoreAndProduct("store1", "Store 1", "founder2", "prod2", "Another Stocked Item", 39.99f, 5, "Another Stocked Item");
    }
    

    private void addStoreAndProduct(String storeId, String storeName, String founderId, String productId, String productName, float price, int amount, String itemName) {
        if (storeRepository.get(storeId) == null) {
            storeRepository.add(storeId, new Store(storeId, storeName, "description", founderId));
        }
        if (productRepository.get(productId) == null) {
            productRepository.add(productId, new Product(productId, productName));
        }
        Item item = new Item(storeId, productId, price, amount, itemName);
    
        itemFacade.add(new Pair<>(storeId, productId), item);
    }
    

    // 1. getItem
    @Test
    public void GivenValidStoreAndProduct_WhenGetItem_ThenReturnItem() {
        Response<ItemDTO> response = itemService.getItem("1", "101");
        assertFalse(response.errorOccurred());
        assertNotNull(response.getValue());
    }

    @Test
    public void GivenInvalidStoreOrProduct_WhenGetItem_ThenReturnError() {
        Response<ItemDTO> response = itemService.getItem("invalid", "invalid");
        assertTrue(response.errorOccurred());
        assertNotNull(response.getErrorMessage());
    }

    // 2. changePrice
    @Test
    public void GivenValidItem_WhenChangePrice_ThenReturnTrue() {
        Response<Boolean> response = itemService.changePrice("1", "101", 99.99f);
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue());
    }

    @Test
    public void GivenInvalidPrice_WhenChangePrice_ThenReturnError() {
        Response<Boolean> response = itemService.changePrice("1", "101", -10f);
        assertTrue(response.errorOccurred());
        assertNotNull(response.getErrorMessage());
    }

    // 3. filterItems
    @Test
    public void GivenMatchingFilter_WhenFilterItems_ThenReturnNonEmptyList() {
        ItemFilter filter = new ItemFilter.Builder().minPrice(10).maxPrice(500).build();
        Response<List<ItemDTO>> response = itemService.filterItems(filter);
        assertFalse(response.errorOccurred());
        assertFalse(response.getValue().isEmpty());
    }

    @Test
    public void GivenNonMatchingFilter_WhenFilterItems_ThenReturnEmptyList() {
        ItemFilter filter = new ItemFilter.Builder().minPrice(99999).build();
        Response<List<ItemDTO>> response = itemService.filterItems(filter);
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().isEmpty());
    }

    // 4. getItemsByStoreId
    @Test
    public void GivenExistingStoreId_WhenGetItems_ThenReturnItems() {
        Response<List<ItemDTO>> response = itemService.getItemsByStoreId("store1");
        assertFalse(response.errorOccurred());
        assertFalse(response.getValue().isEmpty());
    }

    @Test
    public void GivenNonexistentStoreId_WhenGetItems_ThenReturnError() {
        Response<List<ItemDTO>> response = itemService.getItemsByStoreId("invalid-store");
        assertTrue(response.errorOccurred());
        assertNotNull(response.getErrorMessage());
    }

    // 5. getAvailableItems
    @Test
    public void GivenItemsWithStock_WhenGetAvailableItems_ThenReturnNonEmptyList() {
        Response<List<ItemDTO>> response = itemService.getAvailableItems();
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().stream().allMatch(item -> item.getAmount() > 0));
    }

    @Test
    public void GivenAllItemsOutOfStock_WhenGetAvailableItems_ThenReturnEmptyList() {
        itemRepository = new MemoryItemRepository();
        productRepository = new MemoryProductRepository();
        storeRepository = new StoreRepositoryMemory();

        storeRepository.add("storeX", new Store("storeX", "Store X", "Description", "founderX"));
        productRepository.add("prodX", new Product("prodX", "Out of Stock Product"));

        itemRepository.add(new Pair<>("storeX", "prodX"), new Item("storeX", "prodX", 19.99f, 0, "Out of Stock"));

        itemService = new ItemService(new ItemFacade(itemRepository, productRepository, storeRepository));

        Response<List<ItemDTO>> response = itemService.getAvailableItems();
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().isEmpty());
    }

    // 6. increaseAmount
    @Test
    public void GivenValidItem_WhenIncreaseAmount_ThenAmountIncreased() {
        Pair<String, String> id = new Pair<>("1", "101");
        Response<ItemDTO> before = itemService.getItem("1", "101");
        int oldAmount = before.getValue().getAmount();

        Response<Void> response = itemService.increaseAmount(id, 3);
        assertFalse(response.errorOccurred());

        Response<ItemDTO> after = itemService.getItem("1", "101");
        assertEquals(oldAmount + 3, after.getValue().getAmount());
    }

    @Test
    public void GivenInvalidItem_WhenIncreaseAmount_ThenReturnError() {
        Pair<String, String> id = new Pair<>("bad", "bad");
        Response<Void> response = itemService.increaseAmount(id, 3);
        assertTrue(response.errorOccurred());
    }

    // 7. decreaseAmount
    @Test
    public void GivenValidItem_WhenDecreaseAmount_ThenAmountDecreased() {
        Pair<String, String> id = new Pair<>("1", "101");
        Response<ItemDTO> before = itemService.getItem("1", "101");
        int oldAmount = before.getValue().getAmount();

        Response<Void> response = itemService.decreaseAmount(id, 2);
        assertFalse(response.errorOccurred());

        Response<ItemDTO> after = itemService.getItem("1", "101");
        assertEquals(oldAmount - 2, after.getValue().getAmount());
    }

    @Test
    public void GivenTooLargeDecrease_WhenDecreaseAmount_ThenReturnError() {
        Pair<String, String> id = new Pair<>("1", "101");
        Response<ItemDTO> item = itemService.getItem("1", "101");
        Response<Void> response = itemService.decreaseAmount(id, item.getValue().getAmount() + 100);
        assertTrue(response.errorOccurred());
        assertNotNull(response.getErrorMessage());
    }

    // 8. add
    @Test
    public void GivenNewItem_WhenAdd_ThenReturnTrue() {
        storeRepository.add("storeY", new Store("storeY", "Store Y", "Description", "founderY"));
        productRepository.add("prodY", new Product("prodY", "Cool Product"));

        Item item = new Item("storeY", "prodY", 19.99f, 3, "Cool Product");
        Pair<String, String> id = new Pair<>("storeY", "prodY");

        Response<Boolean> response = itemService.add(id, item);
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue());
    }

    @Test
    public void GivenDuplicateItem_WhenAdd_ThenReturnFalse() {
        Pair<String, String> id = new Pair<>("1", "101");
    
        // Fetch real domain Item from the repository, not through the service (which returns DTO)
        Item existingItem = itemRepository.get(id);
    
        Response<Boolean> response = itemService.add(id, existingItem);
        assertFalse(response.errorOccurred());
        assertFalse(response.getValue());
    }
    

    // 9. remove
    @Test
    public void GivenExistingItem_WhenRemove_ThenReturnItem() {
        Pair<String, String> id = new Pair<>("1", "101");
        Response<ItemDTO> response = itemService.remove(id);
        assertFalse(response.errorOccurred());
        assertEquals("101", response.getValue().getProductId());
    }

    @Test
    public void GivenNonexistentItem_WhenRemove_ThenReturnError() {
        Pair<String, String> id = new Pair<>("invalid", "invalid");
        Response<ItemDTO> response = itemService.remove(id);
        assertTrue(response.errorOccurred());
        assertNotNull(response.getErrorMessage());
    }
}
