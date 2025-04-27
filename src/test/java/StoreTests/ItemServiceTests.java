package StoreTests;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import Application.ItemService;
import Application.Response;
import Domain.Pair;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.ItemFilter;
import Domain.Store.MemoryItemRepository;

public class ItemServiceTests {

    private ItemService itemService;

    @Before
    public void setUp() {
        MemoryItemRepository repository = new MemoryItemRepository();
    
        // Item with stock — should appear in getAvailableItems()
        repository.add(new Pair<>("1", "101"), new Item("1", "101", 49.99f, 10, "In Stock Item"));
    
        // Item without stock — should NOT appear in getAvailableItems()
        repository.add(new Pair<>("1", "out-of-stock-product"), new Item("1", "out-of-stock-product", 19.99f, 0, "Out of Stock Item"));
    
        // Optional: more items
        repository.add(new Pair<>("store1", "prod2"), new Item("store1", "prod2", 39.99f, 5, "Another Stocked Item"));
    
        ItemFacade facade = new ItemFacade(repository);
        itemService = new ItemService(facade);
    }
    
    
    
    // 1. getItem
    @Test
    public void GivenValidStoreAndProduct_WhenGetItem_ThenReturnItem() {
        Response<Item> response = itemService.getItem("1", "101");
        assertFalse(response.errorOccurred());
        assertNotNull(response.getValue());
    }

    @Test
    public void GivenInvalidStoreOrProduct_WhenGetItem_ThenReturnError() {
        Response<Item> response = itemService.getItem("invalid", "invalid");
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
        Response<List<Item>> response = itemService.filterItems(filter);
        assertFalse(response.errorOccurred());
        assertFalse(response.getValue().isEmpty());
    }

    @Test
    public void GivenNonMatchingFilter_WhenFilterItems_ThenReturnEmptyList() {
        ItemFilter filter = new ItemFilter.Builder().minPrice(99999).build();
        Response<List<Item>> response = itemService.filterItems(filter);
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().isEmpty());
    }

    // 4. getItemsByStoreId
    @Test
    public void GivenExistingStoreId_WhenGetItems_ThenReturnItems() {
        Response<List<Item>> response = itemService.getItemsByStoreId("store1");
        assertFalse(response.errorOccurred());
        assertFalse(response.getValue().isEmpty());
    }

    @Test
    public void GivenNonexistentStoreId_WhenGetItems_ThenReturnEmptyList() {
        Response<List<Item>> response = itemService.getItemsByStoreId("invalid-store");
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().isEmpty());
    }

    // 5. getAvailableItems
    @Test
    public void GivenItemsWithStock_WhenGetAvailableItems_ThenReturnNonEmptyList() {
        Response<List<Item>> response = itemService.getAvailableItems();
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().stream().allMatch(item -> item.getAmount() > 0));
    }
    
    @Test
    public void GivenAllItemsOutOfStock_WhenGetAvailableItems_ThenReturnEmptyList() {
        // Simulate a repository with only out-of-stock items
        MemoryItemRepository repo = new MemoryItemRepository();
        repo.add(new Pair<>("storeX", "prodX"), new Item("storeX", "prodX", 19.99f, 0, "Out of Stock"));
    
        itemService = new ItemService(new ItemFacade(repo));
    
        Response<List<Item>> response = itemService.getAvailableItems();
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue().isEmpty());
    }
    
    

    // 6. increaseAmount
    @Test
    public void GivenValidItem_WhenIncreaseAmount_ThenAmountIncreased() {
        Pair<String, String> id = new Pair<>("1", "101");
        Response<Item> before = itemService.getItem("1", "101");
        int oldAmount = before.getValue().getAmount();

        Response<Void> response = itemService.increaseAmount(id, 3);
        assertFalse(response.errorOccurred());

        Response<Item> after = itemService.getItem("1", "101");
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
        Response<Item> before = itemService.getItem("1", "101");
        int oldAmount = before.getValue().getAmount();

        Response<Void> response = itemService.decreaseAmount(id, 2);
        assertFalse(response.errorOccurred());

        Response<Item> after = itemService.getItem("1", "101");
        assertEquals(oldAmount - 2, after.getValue().getAmount());
    }

    @Test
    public void GivenTooLargeDecrease_WhenDecreaseAmount_ThenReturnError() {
        Pair<String, String> id = new Pair<>("1", "101");
        Response<Item> item = itemService.getItem("1", "101");
        Response<Void> response = itemService.decreaseAmount(id, item.getValue().getAmount() + 100);
        assertTrue(response.errorOccurred());
        assertNotNull(response.getErrorMessage());
    }

    // 8. add
    @Test
    public void GivenNewItem_WhenAdd_ThenReturnTrue() {
        Item item = new Item("storeY", "prodY",19.99f , 3, "Cool Product");
        Pair<String, String> id = new Pair<>("storeY", "prodY");

        Response<Boolean> response = itemService.add(id, item);
        assertFalse(response.errorOccurred());
        assertTrue(response.getValue());
    }

    @Test
    public void GivenDuplicateItem_WhenAdd_ThenReturnFalse() {
        Pair<String, String> id = new Pair<>("1", "101");
        Item existingItem = itemService.getItem("1", "101").getValue();

        Response<Boolean> response = itemService.add(id, existingItem);
        assertFalse(response.errorOccurred());
        assertFalse(response.getValue()); // Already exists
    }

    // 9. remove
    @Test
    public void GivenExistingItem_WhenRemove_ThenReturnItem() {
        Pair<String, String> id = new Pair<>("1", "101");
        Response<Item> response = itemService.remove(id);
        assertFalse(response.errorOccurred());
        assertEquals("101", response.getValue().getProductId());
    }

    @Test
    public void GivenNonexistentItem_WhenRemove_ThenReturnError() {
        Pair<String, String> id = new Pair<>("invalid", "invalid");
        Response<Item> response = itemService.remove(id);
        assertTrue(response.errorOccurred());
        assertNotNull(response.getErrorMessage());
    }
}
