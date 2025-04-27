package Domain.Store;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ProductTest {

    private Product productWithCats;
    private Product productWithoutCats;
    private Category cat1;
    private Category cat2;

    @Before
    public void setUp() {
        cat1 = new Category("Electronics", "Gadgets");
        cat2 = new Category("Home", "Household items");

        Set<Category> initialCats = new LinkedHashSet<>();
        initialCats.add(cat1);
        productWithCats = new Product("P123", "Widget", initialCats);

        productWithoutCats = new Product("P456", "Gizmo");
    }

    @Test
    public void givenCategories_whenConstructed_thenCategoriesSet() {
        Set<Category> cats = productWithCats.getCategories();
        assertEquals(1, cats.size());
        assertTrue(cats.contains(cat1));
    }

    @Test
    public void givenNoCategories_whenConstructed_thenCategoriesEmpty() {
        Set<Category> cats = productWithoutCats.getCategories();
        assertNotNull(cats);
        assertTrue(cats.isEmpty());
    }

    @Test
    public void whenGetProductId_thenReturnsId() {
        assertEquals("P123", productWithCats.getProductId());
        assertEquals("P456", productWithoutCats.getProductId());
    }

    @Test
    public void whenGetName_thenReturnsName() {
        assertEquals("Widget", productWithCats.getName());
        assertEquals("Gizmo", productWithoutCats.getName());
    }

    @Test
    public void givenNewCategory_whenAddCategory_thenReturnsTrueAndContains() {
        boolean added = productWithoutCats.addCategory(cat2);
        assertTrue(added);
        assertTrue(productWithoutCats.getCategories().contains(cat2));
    }

    @Test
    public void givenExistingCategory_whenAddCategory_thenReturnsFalse() {
        boolean added = productWithCats.addCategory(cat1);
        assertFalse(added);
        assertEquals(1, productWithCats.getCategories().size());
    }

    @Test
    public void givenExistingCategory_whenRemoveCategory_thenReturnsTrueAndRemoved() {
        boolean removed = productWithCats.removeCategory(cat1);
        assertTrue(removed);
        assertFalse(productWithCats.getCategories().contains(cat1));
    }

    @Test
    public void givenMissingCategory_whenRemoveCategory_thenReturnsFalse() {
        boolean removed = productWithoutCats.removeCategory(cat1);
        assertFalse(removed);
        assertTrue(productWithoutCats.getCategories().isEmpty());
    }
}
