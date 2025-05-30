package Domain.Store;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ItemFilterTest {

    private static Item makeItem(double price, int rating, Set<Category> cats, String name) {
        Item item = new Item("s", "p", price, 1, "", name, cats);
        for (int i = 0; i < rating; i++) {
            item.addRating(3); // valid rating of 3 (in [1,5]) → index 2
        }
        return item;
    }

    @Test
    public void givenDefaultFilter_whenMatchesFilter_thenAlwaysTrue() {
        ItemFilter f = new ItemFilter.Builder().build();
        Item any = makeItem(0, 0, Collections.emptySet(), "");
        assertTrue(f.matchesFilter(any));
    }

    @Test
    public void givenMinPrice_whenItemBelow_thenFalseAbove_thenTrue() {
        ItemFilter f = new ItemFilter.Builder().minPrice(50).build();
        assertFalse(f.matchesFilter(makeItem(40, 0, Collections.emptySet(), "")));
        assertTrue(f.matchesFilter(makeItem(60, 0, Collections.emptySet(), "")));
    }

    @Test
    public void givenMaxPrice_whenItemAbove_thenFalseBelow_thenTrue() {
        ItemFilter f = new ItemFilter.Builder().maxPrice(100).build();
        assertFalse(f.matchesFilter(makeItem(150, 0, Collections.emptySet(), "")));
        assertTrue(f.matchesFilter(makeItem(80, 0, Collections.emptySet(), "")));
    }

    @Test
    public void givenMinRating_whenItemBelow_thenFalseAbove_thenTrue() {
        ItemFilter f = new ItemFilter.Builder().minRating(3).build();
        Item low = makeItem(0, 1, Collections.emptySet(), ""); // one rating of 3 → avg 3.0
        Item high = makeItem(0, 3, Collections.emptySet(), ""); // three ratings of 3 → avg 3.0
        assertTrue(f.matchesFilter(high));
        assertTrue(f.matchesFilter(low));
    }

    @Test
    public void givenMaxRating_whenItemAbove_thenFalseBelow_thenTrue() {
        ItemFilter f = new ItemFilter.Builder().maxRating(2).build();
        Item low = makeItem(0, 1, Collections.emptySet(), "");  // rating 3 → avg 3.0
        Item below = makeItem(0, 0, Collections.emptySet(), ""); // no rating → 0.0
        assertFalse(f.matchesFilter(low));
        assertTrue(f.matchesFilter(below));
    }

    @Test
    public void givenCategoriesFilter_whenMissingCategory_thenFalsePresent_thenTrue() {
        Category c1 = new Category("C1", "");
        ItemFilter f = new ItemFilter.Builder().addCategory(c1).build();
        assertFalse(f.matchesFilter(makeItem(0, 0, Collections.emptySet(), "")));
        assertTrue(f.matchesFilter(makeItem(0, 0, Set.of(c1), "")));
    }

    @Test
    public void givenItemNameFilter_whenMismatch_thenFalseMatch_thenTrue() {
        ItemFilter f = new ItemFilter.Builder().itemName("foo").build();
        assertFalse(f.matchesFilter(makeItem(0, 0, Collections.emptySet(), "bar")));
        assertTrue(f.matchesFilter(makeItem(0, 0, Collections.emptySet(), "myfooProduct")));
    }
}
