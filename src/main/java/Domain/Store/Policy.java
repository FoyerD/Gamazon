package Domain.Store;

import Domain.Shopping.ShoppingBasket;
import Domain.User.Member;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A reusable Policy supporting various policy types.
 * Uses Builder pattern for flexible construction.
 */

@Entity
@Table(name = "policies")
public class Policy {

    /** Supported policy types */
    public enum Type {
        MIN_QUANTITY_ALL("Minimum quantity for all items"),
        MAX_QUANTITY_ALL("Maximum quantity for all items"),
        MIN_QUANTITY_PRODUCT("Minimum quantity for a product"),
        MAX_QUANTITY_PRODUCT("Maximum quantity for a product"),
        MIN_QUANTITY_CATEGORY("Minimum quantity for a category"),
        MAX_QUANTITY_CATEGORY("Maximum quantity for a category"),
        CATEGORY_DISALLOW("Disallowed category"),
        CATEGORY_AGE("Age-restricted category"),
        ALLOW_OFFERS("Allow members to make price offers");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }


        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Id
    private String policyId;
    private String storeId;

    @Transient
    private transient Function<String, Product> productLookup;
    @Transient
    private transient Function<String, Item> itemLookup;

    @Enumerated(EnumType.STRING)
    private Type type;

    // Common fields
    @OneToMany(cascade = CascadeType.ALL)
    private List<Policy> subPolicies;

    // For quantity policies
    private int minItemsAll;
    private int maxItemsAll;
    private String targetProductId;
    private int minItemsProduct;
    private int maxItemsProduct;
    private String targetCategory;
    private int minItemsCategory;
    private int maxItemsCategory;

    // For category-based policies
    private String disallowedCategory;
    private int minAge;
    private String ageCategory;

    protected Policy() {} // Required by JPA

    private Policy(Builder b) {
        this.policyId            = b.policyId;
        this.storeId             = b.storeId;
        this.productLookup       = b.productLookup;
        this.itemLookup          = b.itemLookup;
        this.type                = b.type;
        this.subPolicies         = b.subPolicies;
        this.minItemsAll         = b.minItemsAll;
        this.maxItemsAll         = b.maxItemsAll;
        this.targetProductId     = b.targetProductId;
        this.minItemsProduct     = b.minItemsProduct;
        this.maxItemsProduct     = b.maxItemsProduct;
        this.targetCategory      = b.targetCategory;
        this.minItemsCategory    = b.minItemsCategory;
        this.maxItemsCategory    = b.maxItemsCategory;
        this.disallowedCategory  = b.disallowedCategory;
        this.minAge              = b.minAge;
        this.ageCategory         = b.ageCategory;
    }

    /**
     * Builder for Policy.
     */
    public static class Builder {
        private final Type type;
        private String policyId = "";
        private String storeId  = "";
        private Function<String, Product> productLookup;
        private Function<String, Item> itemLookup;
        private List<Policy> subPolicies = Collections.emptyList();

        private int minItemsAll      = 0;
        private int maxItemsAll      = Integer.MAX_VALUE;
        private String targetProductId = "";
        private int minItemsProduct  = 0;
        private int maxItemsProduct  = Integer.MAX_VALUE;
        private String targetCategory = "";
        private int minItemsCategory = 0;
        private int maxItemsCategory = Integer.MAX_VALUE;

        private String disallowedCategory = "";
        private int minAge = 0;
        private String ageCategory = "";

        public Builder(Type type) {
            this.type = type;
        }

        public Builder policyId(String id) {
            this.policyId = id;
            return this;
        }

        public Builder storeId(String id) {
            this.storeId = id;
            return this;
        }

        public Builder productLookup(Function<String, Product> lookup) {
            this.productLookup = lookup;
            return this;
        }

        public Builder itemLookup(Function<String, Item> lookup) {
            this.itemLookup = lookup;
            return this;
        }

        public Builder subPolicies(List<Policy> list) {
            this.subPolicies = new ArrayList<>(list);
            return this;
        }

        public Builder minItemsAll(int min) {
            this.minItemsAll = min;
            return this;
        }

        public Builder maxItemsAll(int max) {
            this.maxItemsAll = max;
            return this;
        }

        public Builder targetProductId(String pid) {
            this.targetProductId = pid;
            return this;
        }

        public Builder minItemsProduct(int min) {
            this.minItemsProduct = min;
            return this;
        }

        public Builder maxItemsProduct(int max) {
            this.maxItemsProduct = max;
            return this;
        }

        public Builder targetCategory(String cat) {
            this.targetCategory = cat;
            return this;
        }

        public Builder minItemsCategory(int min) {
            this.minItemsCategory = min;
            return this;
        }

        public Builder maxItemsCategory(int max) {
            this.maxItemsCategory = max;
            return this;
        }

        public Builder disallowedCategory(String cat) {
            this.disallowedCategory = cat;
            return this;
        }

        public Builder minAge(int age) {
            this.minAge = age;
            return this;
        }

        public Builder ageCategory(String cat) {
            this.ageCategory = cat;
            return this;
        }

        public Policy build() {
            if (productLookup == null || itemLookup == null) {
                throw new IllegalStateException("Lookup functions required");
            }
            return new Policy(this);
        }
    }

    /**
     * Evaluate the policy against basket and member.
     */
    public boolean isApplicable(ShoppingBasket basket, Member member) {
        switch (type) {

            case MIN_QUANTITY_ALL:
                return basket.getOrders().values().stream()
                             .allMatch(q -> q >= minItemsAll);

            case MAX_QUANTITY_ALL:
                return basket.getOrders().values().stream()
                             .allMatch(q -> q <= maxItemsAll);

            case MIN_QUANTITY_PRODUCT:
                return basket.getOrders().getOrDefault(targetProductId, 0) >= minItemsProduct;

            case MAX_QUANTITY_PRODUCT:
                return basket.getOrders().getOrDefault(targetProductId, 0) <= maxItemsProduct;

            case MIN_QUANTITY_CATEGORY:
                return basket.getOrders().entrySet().stream()
                    .filter(e -> productLookup.apply(e.getKey())
                                           .getCategories().stream()
                                           .anyMatch(c -> c.getName().equalsIgnoreCase(targetCategory)))
                    .mapToInt(Map.Entry::getValue)
                    .allMatch(q -> q >= minItemsCategory);

            case MAX_QUANTITY_CATEGORY:
                return basket.getOrders().entrySet().stream()
                    .filter(e -> productLookup.apply(e.getKey())
                                           .getCategories().stream()
                                           .anyMatch(c -> c.getName().equalsIgnoreCase(targetCategory)))
                    .mapToInt(Map.Entry::getValue)
                    .allMatch(q -> q <= maxItemsCategory);

            case CATEGORY_DISALLOW:
                return basket.getOrders().keySet().stream()
                    .map(productLookup)
                    .flatMap(p -> p.getCategories().stream())
                    .noneMatch(c -> c.getName().equalsIgnoreCase(disallowedCategory));

            case CATEGORY_AGE:
                if (member.getAge() < minAge) return false;
                return basket.getOrders().keySet().stream()
                    .map(productLookup)
                    .flatMap(p -> p.getCategories().stream())
                    .anyMatch(c -> c.getName().equalsIgnoreCase(ageCategory));
            default:
                throw new IllegalStateException("Unhandled policy type: " + type);
        }
    }

    // Getters
    public String getPolicyId() { return policyId; }
    public String getStoreId()  { return storeId;  }
    public Type getType()       { return type;     }
    public List<Policy> getSubPolicies() { return Collections.unmodifiableList(subPolicies); }
    public int getMinItemsAll() { return minItemsAll; }
    public int getMaxItemsAll() { return maxItemsAll; }
    public String getTargetProductId() { return targetProductId; }
    public int getMinItemsProduct() { return minItemsProduct; }
    public int getMaxItemsProduct() { return maxItemsProduct; }
    public String getTargetCategory() { return targetCategory; }
    public int getMinItemsCategory() { return minItemsCategory; }
    public int getMaxItemsCategory() { return maxItemsCategory; }
    public String getDisallowedCategory() { return disallowedCategory; }
    public int getMinAge() { return minAge; }
    public String getAgeCategory() { return ageCategory; }
    public Function<String, Product> getProductLookup() { return productLookup; }

    public Function<String, Item> getItemLookup() { return itemLookup; }
    
    // Re-inject lookups after loading from JPA
    public void injectLookups(Function<String, Product> productLookup, Function<String, Item> itemLookup) {
        this.productLookup = productLookup;
        this.itemLookup = itemLookup;
        for (Policy p : subPolicies) {
            p.injectLookups(productLookup, itemLookup);
        }
    }
}

