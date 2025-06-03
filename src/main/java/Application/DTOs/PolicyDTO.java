package Application.DTOs;

import Domain.Store.Policy;
import java.util.List;


/**
 * Data Transfer Object for Policy.
 * Encapsulates all relevant fields for API interactions.
 */
public class PolicyDTO {
    private final String policyId;
    private final String storeId;
    private final Policy.Type type;

    // TODO: Deprecated
    // For AND policies
    // private final List<PolicyDTO> subPolicies;

    // For quantity policies on all items
    private final Integer minItemsAll;
    private final Integer maxItemsAll;

    // For quantity policies on a specific product
    private final ItemDTO targetProduct;
    private final Integer minItemsProduct;
    private final Integer maxItemsProduct;

    // For quantity policies on a specific category
    private final CategoryDTO targetCategory;
    private final Integer minItemsCategory;
    private final Integer maxItemsCategory;

    // For category disallow policies
    private final CategoryDTO disallowedCategory;

    // For age-restriction policies
    private final Integer minAge;
    private final CategoryDTO ageCategory;


    public PolicyDTO(String policyId, Builder builder) {
        this.policyId = policyId;
        this.type = builder.type;
        this.storeId = builder.storeId;
        
        // TODO: Deprecated
        // For AND policies
        //this.subPolicies = builder.subPolicies;

        // For quantity policies on all items
        this.minItemsAll = builder.minItemsAll;
        this.maxItemsAll = builder.maxItemsAll;

        // For quantity policies on a specific product
        this.targetProduct = builder.targetProduct;
        this.minItemsProduct = builder.minItemsProduct;
        this.maxItemsProduct = builder.maxItemsProduct;

        // For quantity policies on a specific category
        this.targetCategory = builder.targetCategory;
        this.minItemsCategory = builder.minItemsCategory;
        this.maxItemsCategory = builder.maxItemsCategory;

        // For category disallow policies
        this.disallowedCategory = builder.disallowedCategory;

        // For age-restriction policies
        this.minAge = builder.minAge;
        this.ageCategory = builder.ageCategory;

    }

    public String getStoreId()  { return storeId;  }
    public Policy.Type getType() { return type; }
    // public List<PolicyDTO> getSubPolicies() { return subPolicies; }
    public Integer getMinItemsAll() { return minItemsAll; }
    public Integer getMaxItemsAll() { return maxItemsAll; }
    public ItemDTO getTargetProduct() { return targetProduct; }
    public Integer getMinItemsProduct() { return minItemsProduct; }
    public Integer getMaxItemsProduct() { return maxItemsProduct; }
    public CategoryDTO getTargetCategory() { return targetCategory; }
    public Integer getMinItemsCategory() { return minItemsCategory; }
    public Integer getMaxItemsCategory() { return maxItemsCategory; }
    public CategoryDTO getDisallowedCategory() { return disallowedCategory; }
    public Integer getMinAge() { return minAge; }
    public CategoryDTO getAgeCategory() { return ageCategory; }

    public Policy toPolicy() {
        Policy.Builder policyBuilder = new Policy.Builder(type);
        return policyBuilder.policyId(policyId)
        .storeId(storeId)
        // .subPolicies(this.subPolicies.stream().map(PolicyDTO::toPolicy).toList())
        .minItemsAll(this.minItemsAll)
        .maxItemsAll(this.maxItemsAll)
        .targetProductId(this.targetProduct.getProductId())
        .minItemsProduct(this.minItemsProduct)
        .maxItemsProduct(this.maxItemsProduct)
        .targetCategory(this.targetCategory.getName())
        .minItemsCategory(this.minItemsCategory)
        .maxItemsCategory(this.maxItemsCategory)
        .disallowedCategory(this.disallowedCategory.getName())
        .ageCategory(this.ageCategory.getName())
        .minAge(this.minAge).build();

    }

    public static class Builder {
        public Builder(String storeId, Policy.Type type) {
            this.storeId = storeId;
            this.type = type;
        }

        private String storeId;
        private Policy.Type type;

        // TODO: Deprecated
        // For AND policies
        // private List<PolicyDTO> subPolicies = List.of();

        // For quantity policies on all items
        private int minItemsAll = -1;
        private int maxItemsAll = -1;

        // For quantity policies on a specific product
        private ItemDTO targetProduct = null;
        private int minItemsProduct = -1;
        private int maxItemsProduct = -1;
        
        // For quantity policies on a specific category
        private CategoryDTO targetCategory = null;
        private int minItemsCategory = -1;
        private int maxItemsCategory = -1;

        // For category disallow policies
        private CategoryDTO disallowedCategory = null;

        // For age-restriction policies
        private int minAge = -1;
        private CategoryDTO ageCategory = null;

        private void commonSetup(Policy.Type target) {
            if (this.type != target){
                throw new IllegalStateException("Should be called only with " + target.name());
            }
        }

        // TODO: Deprecated

        // public Builder createAND(List<PolicyDTO> policies) {
        //     commonSetup(Policy.Type.AND);
        //     this.subPolicies = policies;
        //     return this;
        // }

        public Builder createMaxAll(int minQuantity) {
            commonSetup(Policy.Type.MAX_QUANTITY_ALL);
            if (minQuantity < 1) {
                throw new IllegalArgumentException("minimum quantity must be ≥ 1");
            }

            this.minItemsAll = minQuantity;

            return this;
        }

        public Builder createMinQuantityAllPolicy(int minQuantity) {
            commonSetup(Policy.Type.MIN_QUANTITY_ALL);
            if (minQuantity < 1) {
                throw new IllegalArgumentException("Minimum Quantity must be ≥ 1");
            }

            this.minItemsAll = minQuantity;
            return this;
        }

        public Builder createMaxQuantityAllPolicy(int maxQuantity) {
            commonSetup(Policy.Type.MAX_QUANTITY_ALL);
            if (maxQuantity < 1) {
                throw new IllegalArgumentException("Maximum Quantity must be ≥ 1");
            }

            this.maxItemsAll = maxQuantity;

            return this;
        }

        public Builder createMinQuantityProductPolicy(ItemDTO product, int minQuantity) {
            commonSetup(Policy.Type.MIN_QUANTITY_PRODUCT);

            if (minQuantity < 1) {
                throw new IllegalArgumentException("minQuantity must be ≥ 1");
            }

            this.targetProduct = product;
            this.minItemsProduct = minQuantity;


            return this;
        }

        public Builder createMaxQuantityProductPolicy(ItemDTO product, int maxQuantity) {
            commonSetup(Policy.Type.MAX_QUANTITY_PRODUCT);
            if (maxQuantity < 1) {
                throw new IllegalArgumentException("Maximum Quantity must be ≥ 1");
            }

            this.targetProduct = product;
            this.maxItemsProduct = maxQuantity;

            return this;
        }

        public Builder createMinQuantityCategoryPolicy(CategoryDTO category, int minQuantity) {
            commonSetup(Policy.Type.MIN_QUANTITY_CATEGORY);

            if (minQuantity < 1) {
                throw new IllegalArgumentException("Minimum Quantity must be ≥ 1");
            }

            this.targetCategory = category;
            this.minItemsCategory = minQuantity;
            return this;
        }

        public Builder createMaxQuantityCategoryPolicy(CategoryDTO category, int maxQuantity) {
            commonSetup(Policy.Type.MAX_QUANTITY_CATEGORY);
            if (maxQuantity < 1) {
                throw new IllegalArgumentException("Maximum Quantity must be ≥ 1");
            }

            this.targetCategory = category;
            this.maxItemsCategory = maxQuantity;
            return this;
        }

        public Builder createCategoryDisallowPolicy(CategoryDTO category) {
            commonSetup(Policy.Type.CATEGORY_DISALLOW);

            this.disallowedCategory = category;

            return this;
        }

        public Builder createCategoryAgePolicy(CategoryDTO category, int minAge) {
            commonSetup(Policy.Type.CATEGORY_AGE);
            if (minAge < 0) {
                throw new IllegalArgumentException("Minimum Age must be ≥ 0");
            }

            this.ageCategory = category;
            this.minAge = minAge;

            return this;
        }

        public PolicyDTO build() {
            return build(null);
        }

        public PolicyDTO build(String policyId) {
            return new PolicyDTO(policyId, this);
        }
    }


}
