package StoreTests;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import Application.ItemService;
import Application.Response;
import Domain.Store.Item;
import Domain.Store.ItemFilter;

public class ItemServiceTests {

    private ItemService itemService;

    @Before
    public void setUp() {
        itemService = new ItemService();
        // In real setup, inject mocked repositories here
    }

    @Test
    public void testFilterItems_positiveCase() {
        ItemFilter filter = new ItemFilter.Builder()
                .minPrice(100f)
                .maxPrice(600f)
                .minRating(4.0f)
                .build();

        Response<List<Item>> response = itemService.filterItems(filter);

        assertFalse("No error should occur", response.errorOccurred());
        assertNotNull("Response value should not be null", response.getValue());
        assertFalse("Should return at least one item", response.getValue().isEmpty());
    }

    @Test
    public void testFilterItems_negativeCase_noMatches() {
        ItemFilter filter = new ItemFilter.Builder()
                .minPrice(99999f)
                .build();

        Response<List<Item>> response = itemService.filterItems(filter);

        assertFalse("No error should occur", response.errorOccurred());
        assertNotNull("Should return an empty list", response.getValue());
        assertTrue("Expected empty result list", response.getValue().isEmpty());
    }

    @Test
    public void testGetItemsByStoreId_positiveCase() {
        String storeId = "store1";

        Response<List<Item>> response = itemService.getItemsByStoreId(storeId);

        assertFalse("No error should occur", response.errorOccurred());
        assertNotNull("Returned list should not be null", response.getValue());
        assertFalse("Store should have items", response.getValue().isEmpty());

        for (Item item : response.getValue()) {
            assertEquals("Item should belong to the requested store", storeId, item.getStoreId());
        }
    }

    @Test
    public void testGetItemsByStoreId_negativeCase_invalidStore() {
        String storeId = "nonexistent-store";

        Response<List<Item>> response = itemService.getItemsByStoreId(storeId);

        assertFalse("No error should occur even for empty store", response.errorOccurred());
        assertNotNull("Should return an empty list", response.getValue());
        assertTrue("No items should be returned", response.getValue().isEmpty());
    }

    @Test
    public void testGetAvailableItems_positiveCase() {
        String productId = "prod1";

        Response<List<Item>> response = itemService.getAvailableItems(productId);

        assertFalse("No error should occur", response.errorOccurred());
        assertNotNull("Returned list should not be null", response.getValue());
        assertFalse("At least one available item expected", response.getValue().isEmpty());

        for (Item item : response.getValue()) {
            assertEquals("Should match requested product ID", productId, item.getProductId());
            assertTrue("Item should be available (amount > 0)", item.getAmount() > 0);
        }
    }

    @Test
    public void testGetAvailableItems_negativeCase_noStock() {
        String productId = "out-of-stock-product";

        Response<List<Item>> response = itemService.getAvailableItems(productId);

        assertFalse("No error should occur", response.errorOccurred());
        assertNotNull("Should return an empty list", response.getValue());
        assertTrue("Expected no available items", response.getValue().isEmpty());
    }

    @Test
    public void testAddRating_positiveCase() {
        String storeId = "store1";
        String productId = "prod1";
        float rating = 4.5f;

        Response<Void> response = itemService.addRating(storeId, productId, rating);

        assertFalse("No error should occur", response.errorOccurred());
        assertNull("Void response should have no value", response.getValue());
    }

    @Test
    public void testAddRating_negativeCase_invalidRating() {
        String storeId = "store1";
        String productId = "prod1";
        float rating = 7.0f; // invalid if rating scale is 0–5

        Response<Void> response = itemService.addRating(storeId, productId, rating);

        assertTrue("Error should occur for invalid rating", response.errorOccurred());
        assertNotNull("Should contain error message", response.getErrorMessage());
    }
}