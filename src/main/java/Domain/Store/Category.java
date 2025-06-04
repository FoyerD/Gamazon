package Domain.Store;

import java.util.Objects;

/**
 * Represents a category to which a product can belong.
 * Categories are compared by their name.
 */
public class Category {
    private String name;
    private String description;

    /**
     * Constructs a category with the given name and description.
     *
     * @param name        the name of the category
     * @param description the description of the category
     */
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** @return the name of the category */
    public String getName() {
        return name;
    }

    /**
     * Checks if this category is equal to another by name.
     *
     * @param other the other category
     * @return true if names match, false otherwise
     */
    public boolean isEquals(Category other) {
        if (other == null) return false;
        return this.name.equals(other.name);
    }

    /** @return the description of the category */
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category that = (Category) o;
        return name.trim().toLowerCase().equals(that.name.trim().toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
