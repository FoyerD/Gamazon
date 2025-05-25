package Domain.management;

import Domain.Store.IPolicyRepository;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.Policies.AndPolicy;
import Domain.Store.Policies.CategoryPolicy;
import Domain.Store.Policies.CategoryAgePolicy;
import Domain.Store.Policies.IPolicy;
import Domain.Store.Policies.MaxQuantityPolicy;
import Domain.Store.Policies.MinQuantityPolicy;
import Domain.User.IUserRepository;
import Domain.User.LoginManager;
import Domain.User.Member;
import java.util.List;
import java.util.NoSuchElementException;


public class PolicyFacade 
{
    private final IPolicyRepository policyRepository;
    private IUserRepository userRepository;
    private ItemFacade itemFacade;

    public PolicyFacade(IPolicyRepository policyRepository, IUserRepository userRepository, ItemFacade itemFacade) 
    {
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.itemFacade = itemFacade;
    }

    /**
     * Creates a new AndPolicy with the given policies and persists it.
     *
     * @param policies The list of policies to combine.
     * @param policyId The ID for the new AndPolicy.
     * @param storeId  The ID of the store to which the policy belongs.
     */
    public IPolicy createAndPolicy(List<IPolicy> policies, String policyId, String storeId) 
    {
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("Policies list cannot be null or empty");
        }
        if (policyId == null || policyId.isEmpty()) {
            throw new IllegalArgumentException("Policy ID cannot be null or empty");
        }
        if (storeId == null || storeId.isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }

        AndPolicy andPolicy = new AndPolicy(policies, policyId, storeId);
        if (!policyRepository.add(policyId, andPolicy)) {
            throw new IllegalStateException("Policy with ID " + policyId + " already exists");
        }
        return andPolicy;
    }

    /**
     * Creates a new CategoryPolicy with the given disallowed categories and persists it.
     *
     * @param disallowedCategories The list of disallowed categories.
     * @param policyId             The ID for the new CategoryPolicy.
     * @param storeId              The ID of the store to which the policy belongs.
     */
    public IPolicy createCategoryPolicy(String disallowedCategory, String policyId, String storeId) 
    {
        if (disallowedCategory == null || disallowedCategory.isEmpty()) {
            throw new IllegalArgumentException("Disallowed category cannot be null or empty");
        }
        if (policyId == null || policyId.isEmpty()) {
            throw new IllegalArgumentException("Policy ID cannot be null or empty");
        }
        if (storeId == null || storeId.isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }

        CategoryPolicy categoryPolicy = new CategoryPolicy(disallowedCategory, policyId, storeId);
        if (!policyRepository.add(policyId, categoryPolicy)) {
            throw new IllegalStateException("Policy with ID " + policyId + " already exists");
        }
        return categoryPolicy;
    }

    /**
     * Creates a new CategoryAgePolicy with the given minimum age and category, and persists it.
     *
     * @param policyId   The ID for the new CategoryAgePolicy.
     * @param storeId    The ID of the store to which the policy belongs.
     * @param minAge     The minimum age required to purchase items in the specified category.
     * @param category   The category for which the age restriction applies.
     */
    public IPolicy createCategoryAgePolicy(String policyId, String storeId, int minAge, String category) 
    {
        if (policyId == null || policyId.isEmpty()) {
            throw new IllegalArgumentException("Policy ID cannot be null or empty");
        }
        if (storeId == null || storeId.isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        if (minAge < 0) {
            throw new IllegalArgumentException("Minimum age cannot be negative");
        }
        if (category == null || category.isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        CategoryAgePolicy categoryAgePolicy = new CategoryAgePolicy(minAge, storeId, policyId, category);
        if (!policyRepository.add(policyId, categoryAgePolicy)) {
            throw new IllegalStateException("Policy with ID " + policyId + " already exists");  
        }   
        return categoryAgePolicy;
    }

    /**
     * Creates a new MaxQuantityPolicy with the given maximum quantity and amount got, and persists it.
     *
     * @param policyId    The ID for the new MaxQuantityPolicy.
     * @param storeId     The ID of the store to which the policy belongs.
     * @param maxQuantity The maximum quantity allowed for the item.
     * @param amountGot   The amount already purchased by the member.
     */
    public IPolicy createMaxQuantitPolicy(String policyId, String storeId, int maxQuantity, int amountGot) 
    {
        if (policyId == null || policyId.isEmpty()) {
            throw new IllegalArgumentException("Policy ID cannot be null or empty");
        }
        if (storeId == null || storeId.isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        if (maxQuantity <= 0) {
            throw new IllegalArgumentException("Maximum quantity must be greater than zero");
        }
        // Assuming MaxQuantityPolicy is implemented
        MaxQuantityPolicy maxQuantityPolicy = new MaxQuantityPolicy(maxQuantity, amountGot, policyId, storeId);
        if (!policyRepository.add(policyId, maxQuantityPolicy)) {
            throw new IllegalStateException("Policy with ID " + policyId + " already exists");
        }
        return maxQuantityPolicy;
    }

    /**
     * Creates a new MinQuantityPolicy with the given minimum quantity and amount got, and persists it.
     *
     * @param policyId    The ID for the new MinQuantityPolicy.
     * @param storeId     The ID of the store to which the policy belongs.
     * @param minQuantity The minimum quantity required for the item.
     * @param amountGot   The amount already purchased by the member.
     */
    public IPolicy createMinQuantityPolicy(String policyId, String storeId, int minQuantity, int amountGot) 
    {
        if (policyId == null || policyId.isEmpty()) {
            throw new IllegalArgumentException("Policy ID cannot be null or empty");
        }
        if (storeId == null || storeId.isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        if (minQuantity <= 0) {
            throw new IllegalArgumentException("Minimum quantity must be greater than zero");
        }
        // Assuming MinQuantityPolicy is implemented
        MinQuantityPolicy minQuantityPolicy = new MinQuantityPolicy(minQuantity, amountGot, policyId, storeId);
        if (!policyRepository.add(policyId, minQuantityPolicy)) {
            throw new IllegalStateException("Policy with ID " + policyId + " already exists");
        }
        return minQuantityPolicy;
    }

    /**
     * Deletes the policy with the given ID.
     */
    public void removePolicy(String policyId) {
        policyRepository.remove(policyId);          
    }

    /**
     * Fetches the policy by its ID.
     */
    public IPolicy getPolicy(String policyId) {
        return policyRepository.get(policyId);
    }

    /**
     * Returns all policies.
     */
    public List<IPolicy> getAllStorPolicies(String storeId) 
    {
        if (storeId == null || storeId.isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        return policyRepository.getAllStorePolicies(storeId);
    }
    

    /**
     * Checks if a given item is allowed under the specified policy for a member.
     *
     * @param itemId   The ID of the item to check.
     * @param memberId The ID of the member to check against the policy.
     * @param policyId The ID of the policy to check.
     * @return True if the item is applicable under the policy, false otherwise.
     */
    public boolean isApplicable(String itemId, String memberId, String policyId) 
    {
        if (itemId == null || itemId.isEmpty() || memberId == null || memberId.isEmpty() || policyId == null || policyId.isEmpty()) {
            throw new IllegalArgumentException("Item ID, Member ID, and Policy ID cannot be null or empty");
        }

        IPolicy policy = policyRepository.get(policyId);
        if (policy == null) {
            throw new NoSuchElementException("Policy with ID " + policyId + " does not exist");
        }

        Item item = itemFacade.getItem(policy.getStoreId(), policyId);
        if (item == null) {
            throw new NoSuchElementException("Item with ID " + itemId + " does not exist");
        }

        Member member = userRepository.getMember(memberId);
        if (member == null) 
        {
            throw new NoSuchElementException("Member with ID " + memberId + " does not exist");
        }

        return policy.isApplicable(item, member);
    }

    public IPolicy updatePolicy(Long policyId, IPolicy updates) {
        if (policyId == null || updates == null) {
            throw new IllegalArgumentException("Policy ID and updates cannot be null");
        }

        IPolicy existingPolicy = policyRepository.get(policyId.toString());
        if (existingPolicy == null) {
            throw new NoSuchElementException("Policy with ID " + policyId + " does not exist");
        }

        existingPolicy.setStoreId(updates.getStoreId());

        IPolicy updatedPolicy = policyRepository.update(policyId.toString(), existingPolicy);
        if (updatedPolicy == null) 
        {
            throw new IllegalStateException("Failed to update policy with ID " + policyId);
        }

        return updatedPolicy;
    }

    public boolean deletePolicy(Long policyId) {
        if (policyId == null) {
            throw new IllegalArgumentException("Policy ID cannot be null");
        }

        IPolicy existingPolicy = policyRepository.get(policyId.toString());
        if (existingPolicy == null) {
            throw new NoSuchElementException("Policy with ID " + policyId + " does not exist");
        }

         IPolicy removed = policyRepository.remove(policyId.toString());
        if (removed == null) {
            throw new IllegalStateException("Failed to remove policy with ID " + policyId);
        }
        
        return true;
    }
}
