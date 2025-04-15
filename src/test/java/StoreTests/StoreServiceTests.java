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

public class StoreServiceTests {

    private ItemService itemService;

    @Before
    public void setUp() {
        itemService = new ItemService(); // In real use, you would inject test repositories
    }

    @Test
    public void testFilterItems_positiveCase() {
        ItemFilter filter = new ItemFilter.Builder()
                .minPrice(50f)
                .maxPrice(500f)
                .minRating(3.0f)
                .build();

        Response<List<Item>> response = itemService.filterItems(filter);

        assertFalse("No error should occur", response.errorOccurred());
        assertNotNull(response.getValue());
        assertFalse("Expected non-empty result", response.getValue().isEmpty());
    }

    @Test
    public void testFilterItems_negativeCase_noMatches() {
        ItemFilter filter = new ItemFilter.Builder()
                .minPrice(99999f)
                .build();

        Response<List<Item>> response = itemService.filterItems(filter);

        assertFalse(response.errorOccurred());
        assertNotNull(response.getValue());
        assertTrue("Expected empty result", response.getValue().isEmpty());
    }

    @Test
    public void testGetItemsByStoreId_positiveCase() {
        Response<List<Item>> response = itemService.getItemsByStoreId("store1");

        assertFalse(response.errorOccurred());
        assertNotNull(response.getValue());
        assertFalse("Expected items in store", response.getValue().isEmpty());

        for (Item item : response.getValue()) {
            assertEquals("store1", item.getStoreId());
        }
    }

    @Test
    public void testGetItemsByStoreId_negativeCase_nonexistentStore() {
        Response<List<Item>> response = itemService.getItemsByStoreId("invalid-store");

        assertFalse(response.errorOccurred());
        assertNotNull(response.getValue());
        assertTrue("Expected no items for invalid store", response.getValue().isEmpty());
    }

    @Test
    public void testGetAvailableItems_positiveCase() {
        Response<List<Item>> response = itemService.getAvailableItems("prod1");

        assertFalse(response.errorOccurred());
        assertNotNull(response.getValue());
        assertFalse("Expected available items", response.getValue().isEmpty());

        for (Item item : response.getValue()) {
            assertEquals("prod1", item.getProductId());
            assertTrue("Item should be in stock", item.getAmount() > 0);
        }
    }

    @Test
    public void testGetAvailableItems_negativeCase_noneAvailable() {
        Response<List<Item>> response = itemService.getAvailableItems("out-of-stock-product");

        assertFalse(response.errorOccurred());
        assertNotNull(response.getValue());
        assertTrue("Expected empty result for out-of-stock product", response.getValue().isEmpty());
    }

    @Test
    public void testAddRating_positiveCase() {
        Response<Void> response = itemService.addRating("store1", "prod1", 4.0f);

        assertFalse("Expected rating to succeed", response.errorOccurred());
        assertNull("Void response should have null value", response.getValue());
    }

    @Test
    public void testAddRating_negativeCase_invalidRating() {
        Response<Void> response = itemService.addRating("store1", "prod1", 7.5f);

        assertTrue("Expected error for invalid rating", response.errorOccurred());
        assertNotNull("Should contain error message", response.getErrorMessage());
    }

    @Test
    public void testChangePrice_positiveCase() {
        Response<Boolean> response = itemService.changePrice(1, 101, 199.99f);

        assertFalse("Expected successful price change", response.errorOccurred());
        assertTrue("Price change should return true", Boolean.TRUE.equals(response.getValue()));
    }

    @Test
    public void testChangePrice_negativeCase_invalidPrice() {
        Response<Boolean> response = itemService.changePrice(1, 101, -50.0f);

        assertTrue("Expected error on negative price", response.errorOccurred());
        assertNotNull(response.getErrorMessage());
    }
}
