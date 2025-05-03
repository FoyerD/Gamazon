package Domain.Store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates filtering criteria for searching items by price, rating, name, and categories.
 * Built using the {@link Builder} pattern.
 */
public class ItemFilter {
    private final Set<Category> categories; 
    private final double minPrice;
    private final double maxPrice;
    private final double maxRating;
    private final double minRating;
    private final String itemName;

    private ItemFilter(Builder builder) {
        this.categories = builder.categories;
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.maxRating = builder.maxRating;
        this.minRating = builder.minRating;
        this.itemName = builder.itemName;
    }

    /**
     * Checks if the given item matches all configured filter criteria.
     */
    public boolean matchesFilter(Item item) {
        if (minPrice >= 0 && item.getPrice() < minPrice) return false;
        if (maxPrice >= 0 && item.getPrice() > maxPrice) return false;
        if (minRating >= 0 && item.getRating() < minRating) return false;
        if (maxRating >= 0 && item.getRating() > maxRating) return false;
        if (!categories.isEmpty() && !item.getCategories().containsAll(categories)) return false;
        if (!itemName.equals("") && !item.getProductName().contains(itemName)) return false;
        return true;
    }

    public Set<Category> getCategories() { return categories; }
    public double getMinPrice() { return minPrice; }
    public double getMaxPrice() { return maxPrice; }
    public double getMinRating() { return minRating; }
    public double getMaxRating() { return maxRating; }
    public String getItemName() { return itemName; }

    /**
     * Builder for {@link ItemFilter}.
     */
    public static class Builder {
        private Set<Category> categories = Collections.synchronizedSet(new HashSet<>());
        private double minPrice = -1;
        private double maxPrice = -1;
        private double minRating = -1;
        private double maxRating = -1;
        private String itemName = "";

        public Builder addCategory(Category c) {
            categories.add(c);
            return this;
        }

        public Builder addCategories(Collection<Category> cs) {
            categories.addAll(cs);
            return this;
        }

        public Builder minPrice(double minPrice) {
            this.minPrice = minPrice;
            return this;
        }

        public Builder maxPrice(double maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        public Builder minRating(double minRating) {
            this.minRating = minRating;
            return this;
        }

        public Builder maxRating(double maxRating) {
            this.maxRating = maxRating;
            return this;
        }

        public Builder itemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        /**
         * Constructs an {@link ItemFilter} instance with the configured options.
         */
        public ItemFilter build() {
            return new ItemFilter(this);
        }
    }
}
