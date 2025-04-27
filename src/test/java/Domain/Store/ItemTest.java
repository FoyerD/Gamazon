package Domain.Store;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ItemTest {

    private Item item;

    @Before
    public void setUp() {
        item = new Item("store1", "prodA", 100.0, 5, "A fine product");
    }

    @Test
    public void whenConstructed_thenFieldsAreSet() {
        assertEquals("store1", item.getStoreId());
        assertEquals("prodA", item.getProductId());
        assertEquals(100.0, item.getPrice(), 0.0);
        assertEquals(5, item.getAmount());
        assertEquals("A fine product", item.getDescription());
    }

    @Test
    public void givenValidSetAmount_thenAmountIsUpdated() {
        item.setAmount(10);
        assertEquals(10, item.getAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenNegativeSetAmount_thenThrows() {
        item.setAmount(-1);
    }

    @Test
    public void givenValidSetPrice_thenPriceIsUpdated() {
        item.setPrice(50f);
        assertEquals(50f, item.getPrice(), 0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenNegativeSetPrice_thenThrows() {
        item.setPrice(-5f);
    }

    @Test
    public void givenValidIncreaseAmount_thenAmountIncreases() {
        item.increaseAmount(3);
        assertEquals(8, item.getAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenNegativeIncreaseAmount_thenThrows() {
        item.increaseAmount(-2);
    }

    @Test
    public void givenValidDecreaseAmount_thenAmountDecreases() {
        item.decreaseAmount(2);
        assertEquals(3, item.getAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenNegativeDecreaseAmount_thenThrows() {
        item.decreaseAmount(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenTooLargeDecreaseAmount_thenThrows() {
        item.decreaseAmount(10);
    }

    @Test
    public void givenValidAddRating_thenRatesUpdatedAndGetRatingCorrect() {
        item.addRating(3);
        item.addRating(3);
        item.addRating(5);
        assertEquals(11.0/3.0, item.getRating(), 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenInvalidAddRatingLow_thenThrows() {
        item.addRating(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenInvalidAddRatingHigh_thenThrows() {
        item.addRating(6);
    }

    @Test(expected = IllegalStateException.class)
    public void whenGetCategoriesWithoutFetcher_thenThrows() {
        item.getCategories();
    }

    @Test(expected = IllegalStateException.class)
    public void whenGetProductNameWithoutFetcher_thenThrows() {
        item.getProductName();
    }

    @Test
    public void givenCategoryFetcherSet_whenGetCategories_thenReturnsSet() {
        Category c = new Category("Books", "Reading");
        item.setCategoryFetcher(() -> Set.of(c));
        Set<Category> cats = item.getCategories();
        assertEquals(1, cats.size());
        assertTrue(cats.contains(c));
    }

    @Test
    public void givenNameFetcherSet_whenGetProductName_thenReturnsName() {
        item.setNameFetcher(() -> "MyProduct");
        assertEquals("MyProduct", item.getProductName());
    }
}
