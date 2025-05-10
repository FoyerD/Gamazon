package Domain.Store;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.vaadin.flow.component.template.Id;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;

/**
 * Represents a product in the system.
 * A product has a unique ID, a name, and is associated with a set of categories.
 */
@Entity
public class Product {

    @Id
    private String productId;
    private String name;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Category> categories = new HashSet<>();

    /**
     * Constructs a product with the given ID, name, and categories.
     *
     * @param productId   the unique identifier of the product
     * @param name        the product's name
     * @param categories  the set of categories the product belongs to
     */
    public Product(String productId, String name, Set<Category> categories) {
        this.productId = productId;
        this.name = name;
        this.categories = categories;
    }

    /**
     * Constructs a product with the given ID and name, initializing with no categories.
     *
     * @param productId the unique identifier of the product
     * @param name      the product's name
     */
    public Product(String productId, String name) {
        this.productId = productId;
        this.name = name;
        this.categories = new LinkedHashSet<>();
    }

    /**
     * Copy constructor for Product.
     *
     * @param other the product to copy
     */
    public Product(Product other) {
        this.productId = other.productId;
        this.name = other.name;
        this.categories = new LinkedHashSet<>(other.categories);
    }

    /**
     * Adds a category to this product.
     *
     * @param c the category to add
     * @return true if the category was added, false if it was already present
     */
    public boolean addCategory(Category c) {
        return categories.add(c);
    }

    /**
     * Removes a category from this product.
     *
     * @param c the category to remove
     * @return true if the category was removed, false if it was not present
     */
    public boolean removeCategory(Category c) {
        return categories.remove(c);
    }

    /** @return the unique ID of the product */
    public String getProductId() {
        return productId;
    }

    /** @return the name of the product */
    public String getName() {
        return name;
    }

    /** @return the set of categories this product belongs to */
    public Set<Category> getCategories() {
        return categories;
    }
}
