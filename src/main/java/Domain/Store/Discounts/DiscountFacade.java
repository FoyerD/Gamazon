package Domain.Store.Discounts;

import java.util.Map;
import java.util.Set;

import org.hibernate.jdbc.Expectation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.DTOs.DiscountDTO;
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
    private final ItemFacade itemFacade;
    private final DiscountBuilder discountBuilder;
    private final ConditionBuilder conditionBuilder;
    
    @Autowired
    public DiscountFacade(IDiscountRepository discountRepository, IConditionRepository conditionRepository, ItemFacade itemFacade) {
        if (discountRepository == null) {
            throw new IllegalArgumentException("DiscountRepository cannot be null");
        }
        if (conditionRepository == null) {
            throw new IllegalArgumentException("ConditionRepository cannot be null");
        }
        if (itemFacade == null) {
            throw new IllegalArgumentException("ItemFacade cannot be null");
        }

        this.itemFacade = itemFacade;
        this.discountRepository = discountRepository;
        this.conditionRepository = conditionRepository;
        this.conditionBuilder = new ConditionBuilder(itemFacade);
        this.discountBuilder = new DiscountBuilder(itemFacade, conditionBuilder);
    }
    
    // ===========================================
    // CONDITION CREATION METHODS (STORE-AWARE)
    // ===========================================
    
    /**
     * Creates and saves a MinPriceCondition for a specific store.
     */
    public MinPriceCondition createMinPriceCondition(String storeId, ItemFacade itemFacade, double minPrice) {
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
        validateConditionSet(conditions);
        
        AndCondition condition = new AndCondition(conditions);
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    /**
     * Creates and saves an AndCondition with two conditions for a specific store.
     */
    public AndCondition createAndCondition(String storeId, Condition condition1, Condition condition2) {
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
        validateConditionSet(conditions);
        
        OrCondition condition = new OrCondition(conditions);
        conditionRepository.save(storeId, condition);
        return condition;
    }
    
    /**
     * Creates and saves an OrCondition with two conditions for a specific store.
     */
    public OrCondition createOrCondition(String storeId, Condition condition1, Condition condition2) {
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
    public boolean addDiscount(String storeId, Discount discount) {
        validateDiscount(discount);
        return discountRepository.save(storeId, discount);
    }

    public Discount addDiscount(String storeId, DiscountDTO discountDTO) throws Exception {
        Discount dis = discountBuilder.buildDiscount(discountDTO);
        if (addDiscount(storeId, dis)){
            return dis;
        } else {
            throw new Exception("Failed to add discount to store " + storeId);
        }
    }
    
    /**
     * Adds a condition to a specific store.
     */
    public void addCondition(String storeId, Condition condition) {
        validateCondition(condition);
        conditionRepository.save(storeId, condition);
    }


    /**
     * Gets all discounts for a specific store.
     */
    public Set<Discount> getStoreDiscounts(String storeId) {
        return discountRepository.getStoreDiscounts(storeId);
    }
    
    
    /**
     * Gets all conditions for a specific store (requires casting repository).
     */
    public Set<Condition> getStoreConditions(String storeId) {
        return conditionRepository.getStoreConditions(storeId);
    }
    
    /**
     * Updates a discount for a specific store.
     */
    public void updateDiscount(String storeId, Discount discount) {
        validateDiscount(discount);
        
        // Check if the discount exists first
        if (!discountRepository.exists(discount.getId())) {
            throw new IllegalArgumentException("Discount with ID " + discount.getId() + " does not exist");
        }
        
        discountRepository.save(storeId, discount);
    }
    
    /**
     * Removes a discount from a specific store.
     */
    public boolean removeDiscount(String storeId, String discountId) {
        validateDiscountId(discountId);
        
        // Verify the discount belongs to this store
        Set<Discount> storeDiscounts = getStoreDiscounts(storeId);
        boolean found = storeDiscounts.stream().anyMatch(d -> d.getId().equals(discountId));
        
        if (!found) {
            throw new IllegalArgumentException("Discount with ID " + discountId + " not found in store " + storeId);
        }
        
        discountRepository.delete(discountId);
        return true;
    }
    
    /**
     * Removes a condition from a specific store.
     */
    public boolean removeCondition(String storeId, String conditionId) {
        validateConditionId(conditionId);
        
        // Verify the condition belongs to this store
        Set<Condition> storeConditions = getStoreConditions(storeId);
        if (storeConditions.stream().noneMatch(c -> c.getId().equals(conditionId))) {
            throw new IllegalArgumentException("Condition with ID " + conditionId + " not found in store " + storeId);
        }

        conditionRepository.delete(conditionId);
        return true;
    }



    // ===========================================
    // GLOBAL REPOSITORY OPERATIONS - DISCOUNTS
    // ===========================================
    
    /**
     * Finds a discount by ID globally.
     */
    public Discount getDiscount(String id) {
        validateDiscountId(id);
        return discountRepository.get(id);
    }
    
    
    /**
     * Removes a discount by ID globally.
     */
    public void removeDiscount(String id) {
        validateDiscountId(id);
        discountRepository.delete(id);
    }

    /**
     * Checks if a discount exists globally.
     */
    public boolean discountExists(String id) {
        validateDiscountId(id);
        return discountRepository.exists(id);
    }
    
    
    // ===========================================
    // GLOBAL REPOSITORY OPERATIONS - CONDITIONS
    // ===========================================
    
    /**
     * Finds a condition by ID globally.
     */
    public Condition geCondition(String id) {
        validateConditionId(id);
        return conditionRepository.get(id);
    }
    
    
    /**
     * Removes a condition by ID globally.
     */
    public void removeCondition(String id) {
        validateConditionId(id);
        conditionRepository.delete(id);
    }
    
    /**
     * Checks if a condition exists globally.
     */
    public boolean conditionExists(String id) {
        validateConditionId(id);
        return conditionRepository.exists(id);
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
    
    
    
    private void validateItemFacade(ItemFacade itemFacade) {
        if (itemFacade == null) {
            throw new IllegalArgumentException("ItemFacade cannot be null");
        }
    }
    
    private void validateProductId(String productId) {
        if (itemFacade.getProduct(productId) == null) {
            throw new IllegalArgumentException("Product Must exist in the product repository");
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
        if (condition == null || conditionRepository.get(condition.getId()) == null) {
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
    

    private void validateDiscountId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
    }

    private void validateConditionId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition ID cannot be null or empty");
        }
    }
}