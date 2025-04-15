package Domain.Store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ItemFilter {
    private final Set<Category> categories; 
    private final float minPrice;
    private final float maxPrice;
    private final float maxRating;
    private final float minRating;

    private ItemFilter(Builder builder) {
        this.categories = builder.categories;
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.maxRating = builder.maxRating;
        this.minRating = builder.minRating;
    }

    public boolean matchesFilter(Item item){
        // if (minPrice >= 0 && item.getPrice() < minPrice) return false;
        // if (maxPrice >= 0 && item.getPrice() > maxPrice) return false;
        // if (minRating >= 0 && item.getRating() < minRating) return false;
        // if (maxRating >= 0 && item.getRating() > maxRating) return false;
        // if (!categories.isEmpty() && !categories.contains(item.getCategory())) return false;
        return true;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public float getMinPrice() {
        return minPrice;
    }

    public float getMaxPrice() {
        return maxPrice;
    }

    public float getMinRating() {
        return minRating;
    }

    public float getMaxRating() {
        return maxRating;
    }

    // --- Builder class ---
    public static class Builder {
        private Set<Category> categories = Collections.synchronizedSet(new HashSet<>());
        private float minPrice = -1;
        private float maxPrice = -1;
        private float minRating = -1;
        private float maxRating = -1;

        public Builder addCategory(Category c) {
            categories.add(c);
            return this;
        }

        public Builder addCategories(Collection<Category> cs) {
            categories.addAll(cs);
            return this;
        }

        public Builder minPrice(float minPrice) {
            this.minPrice = minPrice;
            return this;
        }

        public Builder maxPrice(float maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        public Builder minRating(float minRating) {
            this.minRating = minRating;
            return this;
        }

        public Builder maxRating(float maxRating) {
            this.maxRating = maxRating;
            return this;
        }

        public ItemFilter build() {
            return new ItemFilter(this);
        }
    }
}
