package Domain.Store.Discounts;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.*;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

/**
 * Facade class for creating, storing, and retrieving discounts and conditions.
 * Provides type-safe factory methods and repository operations with store-aware functionality.
 */
@Component
public class DiscountFacade {
    
    private final IDiscountRepository discountRepository;
    private final IConditionRepository conditionRepository;
    
    @Autowired
    public DiscountFacade(IDiscountRepository discountRepository, IConditionRepository conditionRepository) {
        if (discountRepository == null) {
            throw new IllegalArgumentException("DiscountRepository cannot be null");
        }
        if (conditionRepository == null) {
            throw new IllegalArgumentException("ConditionRepository cannot be null");
        }
        
        this.discountRepository = discountRepository;
        this.conditionRepository = conditionRepository;
    }
    
    // ===========================================
    // CONDITION CREATION METHODS (STORE-AWARE)
    // ===========================================
    
    /**
     * Creates and saves a MinPriceCondition for a specific store.
     */
    public MinPriceCondition createMinPriceCondition(String storeId, ItemFacade itemFacade, double minPrice) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        if (minPrice < 0) {
            throw new IllegalArgumentException("Min price cannot be negative");
        }
        
        MinPriceCondition condition = new MinPriceCondition(itemFacade, minPrice);
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    /**
     * Creates and saves a MaxPriceCondition for a specific store.
     */
    public MaxPriceCondition createMaxPriceCondition(String storeId, ItemFacade itemFacade, double maxPrice) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        if (maxPrice < 0) {
            throw new IllegalArgumentException("Max price cannot be negative");
        }
        
        MaxPriceCondition condition = new MaxPriceCondition(itemFacade, maxPrice);
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    /**
     * Creates and saves a MinQuantityCondition for a specific store.
     */
    public MinQuantityCondition createMinQuantityCondition(String storeId, ItemFacade itemFacade, String productId, int minQuantity) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateProductId(productId);
        if (minQuantity < 0) {
            throw new IllegalArgumentException("Min quantity cannot be negative");
        }
        
        MinQuantityCondition condition = new MinQuantityCondition(itemFacade, productId, minQuantity);
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    /**
     * Creates and saves a MaxQuantityCondition for a specific store.
     */
    public MaxQuantityCondition createMaxQuantityCondition(String storeId, ItemFacade itemFacade, String productId, int maxQuantity) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateProductId(productId);
        if (maxQuantity < 0) {
            throw new IllegalArgumentException("Max quantity cannot be negative");
        }
        
        MaxQuantityCondition condition = new MaxQuantityCondition(itemFacade, productId, maxQuantity);
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    /**
     * Creates and saves an AndCondition for a specific store.
     */
    public AndCondition createAndCondition(String storeId, Set<Condition> conditions) {
        validateStoreId(storeId);
        validateConditionSet(conditions);
        
        AndCondition condition = new AndCondition(conditions);
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    /**
     * Creates and saves an AndCondition with two conditions for a specific store.
     */
    public AndCondition createAndCondition(String storeId, Condition condition1, Condition condition2) {
        validateStoreId(storeId);
        validateCondition(condition1);
        validateCondition(condition2);
        
        AndCondition condition = new AndCondition(condition1, condition2);
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    /**
     * Creates and saves an OrCondition for a specific store.
     */
    public OrCondition createOrCondition(String storeId, Set<Condition> conditions) {
        validateStoreId(storeId);
        validateConditionSet(conditions);
        
        OrCondition condition = new OrCondition(conditions);
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    /**
     * Creates and saves an OrCondition with two conditions for a specific store.
     */
    public OrCondition createOrCondition(String storeId, Condition condition1, Condition condition2) {
        validateStoreId(storeId);
        validateCondition(condition1);
        validateCondition(condition2);
        
        OrCondition condition = new OrCondition(condition1, condition2);
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    /**
     * Creates and saves a TrueCondition for a specific store.
     */
    public TrueCondition createTrueCondition(String storeId) {
        validateStoreId(storeId);
        
        TrueCondition condition = new TrueCondition();
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    // ===========================================
    // DISCOUNT CREATION METHODS (STORE-AWARE)
    // ===========================================
    
    /**
     * Creates and saves a SimpleDiscount with condition for a specific store.
     */
    public SimpleDiscount createSimpleDiscount(String storeId, ItemFacade itemFacade, float discountPercentage, 
                                             DiscountQualifier qualifier, Condition condition) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateDiscountPercentage(discountPercentage);
        validateQualifier(qualifier);
        validateCondition(condition);
        
        SimpleDiscount discount = new SimpleDiscount(itemFacade, discountPercentage, qualifier, condition);
        discountRepository.save(storeId, discount);
        return discount;
    }
    
    /**
     * Creates and saves a SimpleDiscount without condition (uses TrueCondition) for a specific store.
     */
    public SimpleDiscount createSimpleDiscount(String storeId, ItemFacade itemFacade, float discountPercentage, 
                                             DiscountQualifier qualifier) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateDiscountPercentage(discountPercentage);
        validateQualifier(qualifier);
        
        SimpleDiscount discount = new SimpleDiscount(itemFacade, discountPercentage, qualifier);
        discountRepository.save(storeId, discount);
        return discount;
    }
    
    /**
     * Creates and saves an AndDiscount from a set of discounts for a specific store.
     */
    public AndDiscount createAndDiscount(String storeId, ItemFacade itemFacade, Set<Discount> discounts) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateDiscountSet(discounts);
        
        AndDiscount discount = new AndDiscount(itemFacade, discounts);
        discountRepository.save(storeId, discount);
        return discount;
    }
    
    /**
     * Creates and saves an AndDiscount from two discounts for a specific store.
     */
    public AndDiscount createAndDiscount(String storeId, ItemFacade itemFacade, Discount discount1, Discount discount2) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateDiscount(discount1);
        validateDiscount(discount2);
        
        AndDiscount discount = new AndDiscount(itemFacade, discount1, discount2);
        discountRepository.save(storeId, discount);
        return discount;
    }
    
    /**
     * Creates and saves an OrDiscount for a specific store.
     */
    public OrDiscount createOrDiscount(String storeId, ItemFacade itemFacade, Discount discount, Set<Condition> conditions) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateDiscount(discount);
        validateConditionSet(conditions);
        
        OrDiscount orDiscount = new OrDiscount(itemFacade, discount, conditions);
        discountRepository.save(storeId, orDiscount);
        return orDiscount;
    }
    
    /**
     * Creates and saves a XorDiscount for a specific store.
     */
    public XorDiscount createXorDiscount(String storeId, ItemFacade itemFacade, Discount discount1, Discount discount2) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateDiscount(discount1);
        validateDiscount(discount2);
        
        XorDiscount discount = new XorDiscount(itemFacade, discount1, discount2);
        discountRepository.save(storeId, discount);
        return discount;
    }
    
    /**
     * Creates and saves a DoubleDiscount for a specific store.
     */
    public DoubleDiscount createDoubleDiscount(String storeId, ItemFacade itemFacade, Set<Discount> discounts) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateDiscountSet(discounts);
        
        DoubleDiscount discount = new DoubleDiscount(itemFacade, discounts);
        discountRepository.save(storeId, discount);
        return discount;
    }
    
    /**
     * Creates and saves a MaxDiscount from a set of discounts for a specific store.
     */
    public MaxDiscount createMaxDiscount(String storeId, ItemFacade itemFacade, Set<Discount> discounts) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateDiscountSet(discounts);
        
        MaxDiscount discount = new MaxDiscount(itemFacade, discounts);
        discountRepository.save(storeId, discount);
        return discount;
    }
    
    /**
     * Creates and saves a MaxDiscount from two discounts for a specific store.
     */
    public MaxDiscount createMaxDiscount(String storeId, ItemFacade itemFacade, Discount discount1, Discount discount2) {
        validateStoreId(storeId);
        validateItemFacade(itemFacade);
        validateDiscount(discount1);
        validateDiscount(discount2);
        
        MaxDiscount discount = new MaxDiscount(itemFacade, discount1, discount2);
        discountRepository.save(storeId, discount);
        return discount;
    }
    
    // ===========================================
    // STORE-SPECIFIC REPOSITORY OPERATIONS
    // ===========================================
    
    /**
     * Adds a discount to a specific store.
     */
    public void addDiscount(String storeId, Discount discount) {
        validateStoreId(storeId);
        validateDiscount(discount);
        discountRepository.save(storeId, discount);
    }
    
    /**
     * Adds a condition to a specific store.
     */
    public void addCondition(String storeId, Condition condition) {
        validateStoreId(storeId);
        validateCondition(condition);
        conditionRepository.save(storeId, condition);
    }
    
    /**
     * Gets all discounts for a specific store.
     */
    public Set<Discount> getStoreDiscounts(String storeId) {
        validateStoreId(storeId);
        return discountRepository.getStoreDiscounts(storeId);
    }
    
    /**
     * Gets all discounts for a specific store as a List.
     */
    public List<Discount> getStoreDiscountsList(String storeId) {
        validateStoreId(storeId);
        return discountRepository.findByStoreId(storeId);
    }
    
    /**
     * Gets all conditions for a specific store (requires casting repository).
     */
    public Map<UUID, Condition> getStoreConditions(String storeId) {
        validateStoreId(storeId);
        // This assumes the repository implementation has this method
        if (conditionRepository instanceof Infrastructure.Repositories.MemoryConditionRepository) {
            Infrastructure.Repositories.MemoryConditionRepository memoryRepo = 
                (Infrastructure.Repositories.MemoryConditionRepository) conditionRepository;
            return memoryRepo.findByStoreId(storeId);
        }
        throw new UnsupportedOperationException("Store-specific condition retrieval not supported by this repository implementation");
    }
    
    /**
     * Updates a discount for a specific store.
     */
    public void updateDiscount(String storeId, Discount discount) {
        validateStoreId(storeId);
        validateDiscount(discount);
        
        // Check if the discount exists first
        if (!discountRepository.existsById(discount.getId())) {
            throw new IllegalArgumentException("Discount with ID " + discount.getId() + " does not exist");
        }
        
        discountRepository.save(storeId, discount);
    }
    
    /**
     * Removes a discount from a specific store.
     */
    public void removeDiscount(String storeId, UUID discountId) {
        validateStoreId(storeId);
        validateId(discountId);
        
        // Verify the discount belongs to this store
        Set<Discount> storeDiscounts = getStoreDiscounts(storeId);
        boolean found = storeDiscounts.stream().anyMatch(d -> d.getId().equals(discountId));
        
        if (!found) {
            throw new IllegalArgumentException("Discount with ID " + discountId + " not found in store " + storeId);
        }
        
        discountRepository.deleteById(discountId);
    }
    
    /**
     * Removes a condition from a specific store.
     */
    public void removeCondition(String storeId, UUID conditionId) {
        validateStoreId(storeId);
        validateId(conditionId);
        
        // Verify the condition belongs to this store
        Map<UUID, Condition> storeConditions = getStoreConditions(storeId);
        if (!storeConditions.containsKey(conditionId)) {
            throw new IllegalArgumentException("Condition with ID " + conditionId + " not found in store " + storeId);
        }
        
        conditionRepository.deleteById(conditionId);
    }
    
    /**
     * Gets the count of discounts for a specific store.
     */
    public int getStoreDiscountCount(String storeId) {
        validateStoreId(storeId);
        return getStoreDiscounts(storeId).size();
    }
    
    /**
     * Gets the count of conditions for a specific store.
     */
    public int getStoreConditionCount(String storeId) {
        validateStoreId(storeId);
        return getStoreConditions(storeId).size();
    }
    
    /**
     * Clears all discounts and conditions for a specific store.
     */
    public void clearStoreDiscountsAndConditions(String storeId) {
        validateStoreId(storeId);
        
        // Clear discounts
        if (discountRepository instanceof Infrastructure.Repositories.MemoryDiscountRepository) {
            Infrastructure.Repositories.MemoryDiscountRepository memoryDiscountRepo = 
                (Infrastructure.Repositories.MemoryDiscountRepository) discountRepository;
            memoryDiscountRepo.deleteByStoreId(storeId);
        }
        
        // Clear conditions
        if (conditionRepository instanceof Infrastructure.Repositories.MemoryConditionRepository) {
            Infrastructure.Repositories.MemoryConditionRepository memoryConditionRepo = 
                (Infrastructure.Repositories.MemoryConditionRepository) conditionRepository;
            memoryConditionRepo.deleteByStoreId(storeId);
        }
    }
    
    // ===========================================
    // GLOBAL REPOSITORY OPERATIONS - DISCOUNTS
    // ===========================================
    
    /**
     * Finds a discount by ID globally.
     */
    public Discount findDiscountById(UUID id) {
        validateId(id);
        return discountRepository.findById(id);
    }
    
    /**
     * Gets all discounts globally.
     */
    public Map<UUID, Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }
    
    /**
     * Removes a discount by ID globally.
     */
    public void removeDiscount(UUID id) {
        validateId(id);
        discountRepository.deleteById(id);
    }
    
    /**
     * Checks if a discount exists globally.
     */
    public boolean discountExists(UUID id) {
        validateId(id);
        return discountRepository.existsById(id);
    }
    
    /**
     * Gets the global count of discounts.
     */
    public int getDiscountCount() {
        return discountRepository.size();
    }
    
    // ===========================================
    // GLOBAL REPOSITORY OPERATIONS - CONDITIONS
    // ===========================================
    
    /**
     * Finds a condition by ID globally.
     */
    public Condition findConditionById(UUID id) {
        validateId(id);
        return conditionRepository.findById(id);
    }
    
    /**
     * Gets all conditions globally.
     */
    public Map<UUID, Condition> getAllConditions() {
        return conditionRepository.findAll();
    }
    
    /**
     * Removes a condition by ID globally.
     */
    public void removeCondition(UUID id) {
        validateId(id);
        conditionRepository.deleteById(id);
    }
    
    /**
     * Checks if a condition exists globally.
     */
    public boolean conditionExists(UUID id) {
        validateId(id);
        return conditionRepository.existsById(id);
    }
    
    /**
     * Gets the global count of conditions.
     */
    public int getConditionCount() {
        return conditionRepository.size();
    }
    
    /**
     * Clears all discounts and conditions globally (for testing).
     */
    public void clearAll() {
        discountRepository.clear();
        conditionRepository.clear();
    }
    
    // ===========================================
    // VALIDATION METHODS
    // ===========================================
    
    private void validateStoreId(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
    }
    
    private void validateItemFacade(ItemFacade itemFacade) {
        if (itemFacade == null) {
            throw new IllegalArgumentException("ItemFacade cannot be null");
        }
    }
    
    private void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
    }
    
    private void validateDiscountPercentage(float percentage) {
        if (percentage < 0 || percentage > 1) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 1");
        }
    }
    
    private void validateQualifier(DiscountQualifier qualifier) {
        if (qualifier == null) {
            throw new IllegalArgumentException("DiscountQualifier cannot be null");
        }
    }
    
    private void validateCondition(Condition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
    }
    
    private void validateConditionSet(Set<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException("Conditions set cannot be null or empty");
        }
        for (Condition condition : conditions) {
            validateCondition(condition);
        }
    }
    
    private void validateDiscount(Discount discount) {
        if (discount == null) {
            throw new IllegalArgumentException("Discount cannot be null");
        }
    }
    
    private void validateDiscountSet(Set<Discount> discounts) {
        if (discounts == null || discounts.isEmpty()) {
            throw new IllegalArgumentException("Discounts set cannot be null or empty");
        }
        for (Discount discount : discounts) {
            validateDiscount(discount);
        }
    }
    
    private void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }
}