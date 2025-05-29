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

    // For AND policies
    private final List<Policy> subPolicies;

    // For quantity policies on all items
    private final Integer minItemsAll;
    private final Integer maxItemsAll;

    // For quantity policies on a specific product
    private final String targetProductId;
    private final Integer minItemsProduct;
    private final Integer maxItemsProduct;

    // For quantity policies on a specific category
    private final String targetCategory;
    private final Integer minItemsCategory;
    private final Integer maxItemsCategory;

    // For category disallow policies
    private final String disallowedCategory;

    // For age-restriction policies
    private final Integer minAge;
    private final String ageCategory;

    public PolicyDTO(Policy policy) {
        this.policyId = policy.getPolicyId();
        this.storeId  = policy.getStoreId();
        this.type     = policy.getType();

        // Initialize optionals based on policy type
        this.subPolicies = (type == Policy.Type.AND)
            ? policy.getSubPolicies() : null;

        this.minItemsAll = (type == Policy.Type.MIN_QUANTITY_ALL)
            ? policy.getMinItemsAll() : null;
        this.maxItemsAll = (type == Policy.Type.MAX_QUANTITY_ALL)
            ? policy.getMaxItemsAll() : null;

        this.targetProductId = (type == Policy.Type.MIN_QUANTITY_PRODUCT || type == Policy.Type.MAX_QUANTITY_PRODUCT)
            ? policy.getTargetProductId() : null;
        this.minItemsProduct = (type == Policy.Type.MIN_QUANTITY_PRODUCT)
            ? policy.getMinItemsProduct() : null;
        this.maxItemsProduct = (type == Policy.Type.MAX_QUANTITY_PRODUCT)
            ? policy.getMaxItemsProduct() : null;

        this.targetCategory = (type == Policy.Type.MIN_QUANTITY_CATEGORY || type == Policy.Type.MAX_QUANTITY_CATEGORY)
            ? policy.getTargetCategory() : null;
        this.minItemsCategory = (type == Policy.Type.MIN_QUANTITY_CATEGORY)
            ? policy.getMinItemsCategory() : null;
        this.maxItemsCategory = (type == Policy.Type.MAX_QUANTITY_CATEGORY)
            ? policy.getMaxItemsCategory() : null;

        this.disallowedCategory = (type == Policy.Type.CATEGORY_DISALLOW)
            ? policy.getDisallowedCategory() : null;

        this.minAge = (type == Policy.Type.CATEGORY_AGE)
            ? policy.getMinAge() : null;
        this.ageCategory = (type == Policy.Type.CATEGORY_AGE)
            ? policy.getAgeCategory() : null;
    }

    public String getPolicyId() { return policyId; }
    public String getStoreId()  { return storeId;  }
    public Policy.Type getType() { return type; }
    public List<Policy> getSubPolicies() { return subPolicies; }
    public Integer getMinItemsAll() { return minItemsAll; }
    public Integer getMaxItemsAll() { return maxItemsAll; }
    public String getTargetProductId() { return targetProductId; }
    public Integer getMinItemsProduct() { return minItemsProduct; }
    public Integer getMaxItemsProduct() { return maxItemsProduct; }
    public String getTargetCategory() { return targetCategory; }
    public Integer getMinItemsCategory() { return minItemsCategory; }
    public Integer getMaxItemsCategory() { return maxItemsCategory; }
    public String getDisallowedCategory() { return disallowedCategory; }
    public Integer getMinAge() { return minAge; }
    public String getAgeCategory() { return ageCategory; }
}
