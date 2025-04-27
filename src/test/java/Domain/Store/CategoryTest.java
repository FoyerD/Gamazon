package Domain.Store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CategoryTest {

    @Test
    public void givenSameName_whenIsEquals_thenTrue() {
        Category c1 = new Category("Electronics", "Gadgets");
        Category c2 = new Category("Electronics", "Devices and gadgets");
        assertTrue(c1.isEquals(c2));
    }

    @Test
    public void givenDifferentName_whenIsEquals_thenFalse() {
        Category c1 = new Category("Books", "All kinds of books");
        Category c2 = new Category("Movies", "Films and series");
        assertFalse(c1.isEquals(c2));
    }

    @Test
    public void givenNull_whenIsEquals_thenFalse() {
        Category c1 = new Category("Toys", "Kids’ toys");
        assertFalse(c1.isEquals(null));
    }

    @Test
    public void whenGetName_thenReturnsName() {
        Category c = new Category("Clothing", "Apparel and garments");
        assertEquals("Clothing", c.getName());
    }

    @Test
    public void whenGetDescription_thenReturnsDescription() {
        Category c = new Category("Kitchen", "Utensils and appliances");
        assertEquals("Utensils and appliances", c.getDescription());
    }

    @Test
    public void equalsAndHashCode_considerNameOnly() {
        Category a = new Category("X","d1");
        Category b = new Category("X","d2");
        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
    }
}
