package Domain.Store.Discounts;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.*;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

/**
 * Facade class for creating, storing, and retrieving discounts and conditions.
 * Provides type-safe factory methods and repository operations.
 */
public class DiscountFacade {
    
    private final IDiscountRepository discountRepository;
    private final IConditionRepository conditionRepository;
    
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
    // CONDITION CREATION METHODS
    // ===========================================
    
    /**
     * Creates and saves a MinPriceCondition.
     */
    public MinPriceCondition createMinPriceCondition(ItemFacade itemFacade, double minPrice) {
        validateItemFacade(itemFacade);
        if (minPrice < 0) {
            throw new IllegalArgumentException("Min price cannot be negative");
        }
        
        MinPriceCondition condition = new MinPriceCondition(itemFacade, minPrice);
        conditionRepository.save(condition);
        return condition;
    }
    
    /**
     * Creates and saves a MaxPriceCondition.
     */
    public MaxPriceCondition createMaxPriceCondition(ItemFacade itemFacade, double maxPrice) {
        validateItemFacade(itemFacade);
        if (maxPrice < 0) {
            throw new IllegalArgumentException("Max price cannot be negative");
        }
        
        MaxPriceCondition condition = new MaxPriceCondition(itemFacade, maxPrice);
        conditionRepository.save(condition);
        return condition;
    }
    
    /**
     * Creates and saves a MinQuantityCondition.
     */
    public MinQuantityCondition createMinQuantityCondition(ItemFacade itemFacade, String productId, int minQuantity) {
        validateItemFacade(itemFacade);
        validateProductId(productId);
        if (minQuantity < 0) {
            throw new IllegalArgumentException("Min quantity cannot be negative");
        }
        
        MinQuantityCondition condition = new MinQuantityCondition(itemFacade, productId, minQuantity);
        conditionRepository.save(condition);
        return condition;
    }
    
    /**
     * Creates and saves a MaxQuantityCondition.
     */
    public MaxQuantityCondition createMaxQuantityCondition(ItemFacade itemFacade, String productId, int maxQuantity) {
        validateItemFacade(itemFacade);
        validateProductId(productId);
        if (maxQuantity < 0) {
            throw new IllegalArgumentException("Max quantity cannot be negative");
        }
        
        MaxQuantityCondition condition = new MaxQuantityCondition(itemFacade, productId, maxQuantity);
        conditionRepository.save(condition);
        return condition;
    }
    
    /**
     * Creates and saves an AndCondition.
     */
    public AndCondition createAndCondition(Set<Condition> conditions) {
        validateConditionSet(conditions);
        
        AndCondition condition = new AndCondition(conditions);
        conditionRepository.save(condition);
        return condition;
    }
    
    /**
     * Creates and saves an AndCondition with two conditions.
     */
    public AndCondition createAndCondition(Condition condition1, Condition condition2) {
        validateCondition(condition1);
        validateCondition(condition2);
        
        AndCondition condition = new AndCondition(condition1, condition2);
        conditionRepository.save(condition);
        return condition;
    }
    
    /**
     * Creates and saves an OrCondition.
     */
    public OrCondition createOrCondition(Set<Condition> conditions) {
        validateConditionSet(conditions);
        
        OrCondition condition = new OrCondition(conditions);
        conditionRepository.save(condition);
        return condition;
    }
    
    /**
     * Creates and saves an OrCondition with two conditions.
     */
    public OrCondition createOrCondition(Condition condition1, Condition condition2) {
        validateCondition(condition1);
        validateCondition(condition2);
        
        OrCondition condition = new OrCondition(condition1, condition2);
        conditionRepository.save(condition);
        return condition;
    }
    
    /**
     * Creates and saves a TrueCondition.
     */
    public TrueCondition createTrueCondition() {
        TrueCondition condition = new TrueCondition();
        conditionRepository.save(condition);
        return condition;
    }
    
    // ===========================================
    // DISCOUNT CREATION METHODS
    // ===========================================
    
    /**
     * Creates and saves a SimpleDiscount with condition.
     */
    public SimpleDiscount createSimpleDiscount(ItemFacade itemFacade, float discountPercentage, 
                                             DiscountQualifier qualifier, Condition condition) {
        validateItemFacade(itemFacade);
        validateDiscountPercentage(discountPercentage);
        validateQualifier(qualifier);
        validateCondition(condition);
        
        SimpleDiscount discount = new SimpleDiscount(itemFacade, discountPercentage, qualifier, condition);
        discountRepository.save(discount);
        return discount;
    }
    
    /**
     * Creates and saves a SimpleDiscount without condition (uses TrueCondition).
     */
    public SimpleDiscount createSimpleDiscount(ItemFacade itemFacade, float discountPercentage, 
                                             DiscountQualifier qualifier) {
        validateItemFacade(itemFacade);
        validateDiscountPercentage(discountPercentage);
        validateQualifier(qualifier);
        
        SimpleDiscount discount = new SimpleDiscount(itemFacade, discountPercentage, qualifier);
        discountRepository.save(discount);
        return discount;
    }
    
    /**
     * Creates and saves an AndDiscount from a set of discounts.
     */
    public AndDiscount createAndDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
        validateItemFacade(itemFacade);
        validateDiscountSet(discounts);
        
        AndDiscount discount = new AndDiscount(itemFacade, discounts);
        discountRepository.save(discount);
        return discount;
    }
    
    /**
     * Creates and saves an AndDiscount from two discounts.
     */
    public AndDiscount createAndDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        validateItemFacade(itemFacade);
        validateDiscount(discount1);
        validateDiscount(discount2);
        
        AndDiscount discount = new AndDiscount(itemFacade, discount1, discount2);
        discountRepository.save(discount);
        return discount;
    }
    
    /**
     * Creates and saves an OrDiscount.
     */
    public OrDiscount createOrDiscount(ItemFacade itemFacade, Discount discount, Set<Condition> conditions) {
        validateItemFacade(itemFacade);
        validateDiscount(discount);
        validateConditionSet(conditions);
        
        OrDiscount orDiscount = new OrDiscount(itemFacade, discount, conditions);
        discountRepository.save(orDiscount);
        return orDiscount;
    }
    
    /**
     * Creates and saves a XorDiscount.
     */
    public XorDiscount createXorDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        validateItemFacade(itemFacade);
        validateDiscount(discount1);
        validateDiscount(discount2);
        
        XorDiscount discount = new XorDiscount(itemFacade, discount1, discount2);
        discountRepository.save(discount);
        return discount;
    }
    
    /**
     * Creates and saves a DoubleDiscount.
     */
    public DoubleDiscount createDoubleDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
        validateItemFacade(itemFacade);
        validateDiscountSet(discounts);
        
        DoubleDiscount discount = new DoubleDiscount(itemFacade, discounts);
        discountRepository.save(discount);
        return discount;
    }
    
    /**
     * Creates and saves a MaxDiscount from a set of discounts.
     */
    public MaxDiscount createMaxDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
        validateItemFacade(itemFacade);
        validateDiscountSet(discounts);
        
        MaxDiscount discount = new MaxDiscount(itemFacade, discounts);
        discountRepository.save(discount);
        return discount;
    }
    
    /**
     * Creates and saves a MaxDiscount from two discounts.
     */
    public MaxDiscount createMaxDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        validateItemFacade(itemFacade);
        validateDiscount(discount1);
        validateDiscount(discount2);
        
        MaxDiscount discount = new MaxDiscount(itemFacade, discount1, discount2);
        discountRepository.save(discount);
        return discount;
    }
    
    // ===========================================
    // REPOSITORY OPERATIONS - DISCOUNTS
    // ===========================================
    
    /**
     * Finds a discount by ID.
     */
    public Discount findDiscountById(UUID id) {
        validateId(id);
        return discountRepository.findById(id);
    }
    
    /**
     * Gets all discounts.
     */
    public Map<UUID, Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }
    
    /**
     * Removes a discount by ID.
     */
    public void removeDiscount(UUID id) {
        validateId(id);
        discountRepository.deleteById(id);
    }
    
    /**
     * Checks if a discount exists.
     */
    public boolean discountExists(UUID id) {
        validateId(id);
        return discountRepository.existsById(id);
    }
    
    /**
     * Gets the count of discounts.
     */
    public int getDiscountCount() {
        return discountRepository.size();
    }
    
    // ===========================================
    // REPOSITORY OPERATIONS - CONDITIONS
    // ===========================================
    
    /**
     * Finds a condition by ID.
     */
    public Condition findConditionById(UUID id) {
        validateId(id);
        return conditionRepository.findById(id);
    }
    
    /**
     * Gets all conditions.
     */
    public Map<UUID, Condition> getAllConditions() {
        return conditionRepository.findAll();
    }
    
    /**
     * Removes a condition by ID.
     */
    public void removeCondition(UUID id) {
        validateId(id);
        conditionRepository.deleteById(id);
    }
    
    /**
     * Checks if a condition exists.
     */
    public boolean conditionExists(UUID id) {
        validateId(id);
        return conditionRepository.existsById(id);
    }
    
    /**
     * Gets the count of conditions.
     */
    public int getConditionCount() {
        return conditionRepository.size();
    }
    
    /**
     * Clears all discounts and conditions (for testing).
     */
    public void clearAll() {
        discountRepository.clear();
        conditionRepository.clear();
    }

    public Set<Discount> getStoreDiscounts(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        return discountRepository.getStoreDiscounts(storeId);
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