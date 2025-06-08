package Domain.Store.Discounts;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.DTOs.DiscountDTO;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Discount.MergeType;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Conditions.ConditionBuilder;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;


/**
 * Facade class for creating, storing, and retrieving discounts and conditions.
 * Provides type-safe factory methods and repository operations with store-aware functionality.
 */
@Component
public class DiscountFacade {
    
    private final IDiscountRepository discountRepository;
    // private final IConditionRepository conditionRepository;
    private final ItemFacade itemFacade;
    private final DiscountBuilder discountBuilder;
    private final ConditionBuilder conditionBuilder;
    
    @Autowired
    public DiscountFacade(IDiscountRepository discountRepository, ItemFacade itemFacade) {
        if (discountRepository == null) {
            throw new IllegalArgumentException("DiscountRepository cannot be null");
        }
        if (itemFacade == null) {
            throw new IllegalArgumentException("ItemFacade cannot be null");
        }

        this.itemFacade = itemFacade;
        this.discountRepository = discountRepository;
        this.conditionBuilder = new ConditionBuilder();
        this.discountBuilder = new DiscountBuilder(conditionBuilder);
    }
    
    // ===========================================
    // CONDITION CREATION METHODS (STORE-AWARE)
    // ===========================================
    
    // /**
    //  * Creates and saves a MinPriceCondition for a specific store.
    //  */
    // public MinPriceCondition createMinPriceCondition(String storeId, double minPrice) {
    //     validateItemFacade(itemFacade);
    //     if (minPrice < 0) {
    //         throw new IllegalArgumentException("Min price cannot be negative");
    //     }
        
    //     MinPriceCondition condition = new MinPriceCondition(UUID.randomUUID().toString(), minPrice);
    //     conditionRepository.save(storeId, condition);
    //     return condition;
    // }
    
    // /**
    //  * Creates and saves a MaxPriceCondition for a specific store.
    //  */
    // public MaxPriceCondition createMaxPriceCondition(String storeId, double maxPrice) {
    //     validateItemFacade(itemFacade);
    //     if (maxPrice < 0) {
    //         throw new IllegalArgumentException("Max price cannot be negative");
    //     }
        
    //     MaxPriceCondition condition = new MaxPriceCondition(UUID.randomUUID().toString(), maxPrice);
    //     conditionRepository.save(storeId, condition);
    //     return condition;
    // }
    
    // /**
    //  * Creates and saves a MinQuantityCondition for a specific store.
    //  */
    // public MinQuantityCondition createMinQuantityCondition(String storeId, String productId, int minQuantity) {
    //     validateItemFacade(itemFacade);
    //     validateProductId(productId);
    //     if (minQuantity < 0) {
    //         throw new IllegalArgumentException("Min quantity cannot be negative");
    //     }
        
    //     MinQuantityCondition condition = new MinQuantityCondition(UUID.randomUUID().toString(), productId, minQuantity);
    //     conditionRepository.save(storeId, condition);
    //     return condition;
    // }
    
    // /**
    //  * Creates and saves a MaxQuantityCondition for a specific store.
    //  */
    // public MaxQuantityCondition createMaxQuantityCondition(String storeId, String productId, int maxQuantity) {
    //     validateItemFacade(itemFacade);
    //     validateProductId(productId);
    //     if (maxQuantity < 0) {
    //         throw new IllegalArgumentException("Max quantity cannot be negative");
    //     }
        
    //     MaxQuantityCondition condition = new MaxQuantityCondition(UUID.randomUUID().toString(), productId, maxQuantity);
    //     conditionRepository.save(storeId, condition);
    //     return condition;
    // }
    
    // /**
    //  * Creates and saves an AndCondition for a specific store.
    //  */
    // public AndCondition createAndCondition(String storeId, List<Condition> conditions) {
    //     validateConditionSet(conditions);
        
    //     AndCondition condition = new AndCondition(UUID.randomUUID().toString(), conditions);
    //     conditionRepository.save(storeId, condition);
    //     return condition;
    // }
    
    // /**
    //  * Creates and saves an OrCondition for a specific store.
    //  */
    // public OrCondition createOrCondition(String storeId, List<Condition> conditions) {
    //     validateConditionSet(conditions);
        
    //     OrCondition condition = new OrCondition(UUID.randomUUID().toString(), conditions);
    //     conditionRepository.save(storeId, condition);
    //     return condition;
    // }
    
    // /**
    //  * Creates and saves a TrueCondition for a specific store.
    //  */
    // public TrueCondition createTrueCondition(String storeId) {
        
    //     TrueCondition condition = new TrueCondition(UUID.randomUUID().toString());
    //     conditionRepository.save(storeId, condition);
    //     return condition;
    // }
    
    // ===========================================
    // DISCOUNT CREATION METHODS (STORE-AWARE)
    // ===========================================
    
    /**
     * Creates and saves a SimpleDiscount with condition for a specific store.
     */
    public SimpleDiscount createSimpleDiscount(String storeId, float discountPercentage, 
                                             DiscountQualifier qualifier, Condition condition) {
        validateItemFacade(itemFacade);
        validateDiscountPercentage(discountPercentage);
        validateQualifier(qualifier);
        
        SimpleDiscount discount = new SimpleDiscount(UUID.randomUUID().toString(), storeId, discountPercentage, qualifier, condition);
        discountRepository.add(discount.getId(), discount);
        return discount;
    }
    
    /**
     * Creates and saves an AndDiscount from a set of discounts for a specific store.
     */
    public AndDiscount createAndDiscount(String storeId, List<Discount> discounts, Condition condition, MergeType mergeType) {
        validateItemFacade(itemFacade);
        validateDiscountSet(discounts);
        
        AndDiscount discount = new AndDiscount(UUID.randomUUID().toString(), storeId, discounts, condition, mergeType);
        discountRepository.add(discount.getId(), discount);
        return discount;
    }
    
    
    /**
     * Creates and saves an OrDiscount for a specific store.
     */
    public OrDiscount createOrDiscount(String storeId, List<Discount> discounts, Condition condition, MergeType mergeType) {
        OrDiscount orDiscount = new OrDiscount(UUID.randomUUID().toString(), storeId, discounts, condition, mergeType);
        discountRepository.add(orDiscount.getId(), orDiscount);
        return orDiscount;
    }
    
    /**
     * Creates and saves a XorDiscount for a specific store.
     */
    public XorDiscount createXorDiscount(String storeId, Discount discount1, Discount discount2, Condition condition, MergeType mergeType) {
        validateItemFacade(itemFacade);
        validateDiscount(discount1);
        validateDiscount(discount2);
        
        XorDiscount discount = new XorDiscount(UUID.randomUUID().toString(), storeId, discount1, discount2, condition, mergeType);
        discountRepository.add(discount.getId(), discount);
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
        return discountRepository.add(discount.getId(), discount);
    }

    public Discount addDiscount(String storeId, DiscountDTO discountDTO) throws Exception {
        Discount dis = discountBuilder.buildDiscount(discountDTO, UUID.randomUUID().toString(), storeId);
        if (addDiscount(storeId, dis)){
            return dis;
        } else {
            throw new Exception("Failed to add discount to store " + storeId);
        }
    }
    
    // /**
    //  * Adds a condition to a specific store.
    //  */
    // public void addCondition(String storeId, Condition condition) {
    //     validateCondition(condition);
    //     conditionRepository.save(storeId, condition);
    // }


    /**
     * Gets all discounts for a specific store.
     */
    public List<Discount> getStoreDiscounts(String storeId) {
        return discountRepository.getStoreDiscounts(storeId);
    }
    
    
    // /**
    //  * Gets all conditions for a specific store (requires casting repository).
    //  */
    // public List<Condition> getStoreConditions(String storeId) {
    //     return conditionRepository.getStoreConditions(storeId);
    // }
    
    /**
     * Updates a discount for a specific store.
     */
    public void updateDiscount(String storeId, Discount discount) {
        validateDiscount(discount);
        
        // Check if the discount exists first
        if (!discountRepository.exists(discount.getId())) {
            throw new IllegalArgumentException("Discount with ID " + discount.getId() + " does not exist");
        }
        
        discountRepository.add(discount.getId(), discount);
    }
    
    /**
     * Removes a discount from a specific store.
     */
    public boolean removeDiscount(String storeId, String discountId) {
        validateDiscountId(discountId);
        
        // Verify the discount belongs to this store
        List<Discount> storeDiscounts = getStoreDiscounts(storeId);
        boolean found = storeDiscounts.stream().anyMatch(d -> d.getId().equals(discountId));
        
        if (!found) {
            throw new IllegalArgumentException("Discount with ID " + discountId + " not found in store " + storeId);
        }
        
        discountRepository.remove(discountId);
        return true;
    }
    
    // /**
    //  * Removes a condition from a specific store.
    //  */
    // public boolean removeCondition(String storeId, String conditionId) {
    //     validateConditionId(conditionId);
        
    //     // Verify the condition belongs to this store
    //     List<Condition> storeConditions = getStoreConditions(storeId);
    //     if (storeConditions.stream().noneMatch(c -> c.getId().equals(conditionId))) {
    //         throw new IllegalArgumentException("Condition with ID " + conditionId + " not found in store " + storeId);
    //     }

    //     conditionRepository.delete(conditionId);
    //     return true;
    // }



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
        discountRepository.remove(id);
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
    
    // /**
    //  * Finds a condition by ID globally.
    //  */
    // public Condition geCondition(String id) {
    //     validateConditionId(id);
    //     return conditionRepository.get(id);
    // }
    
    
    // /**
    //  * Removes a condition by ID globally.
    //  */
    // public void removeCondition(String id) {
    //     validateConditionId(id);
    //     conditionRepository.delete(id);
    // }
    
    // /**
    //  * Checks if a condition exists globally.
    //  */
    // public boolean conditionExists(String id) {
    //     validateConditionId(id);
    //     return conditionRepository.exists(id);
    // }

    // /**
    //  * Gets the global count of conditions.
    //  */
    // public int getConditionCount() {
    //     return conditionRepository.size();
    // }
    
    // /**
    //  * Clears all discounts and conditions globally (for testing).
    //  */
    // public void clearAll() {
    //     discountRepository.clear();
    //     conditionRepository.clear();
    // }
    
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
    
    // private void validateCondition(Condition condition) {
    //     if (condition == null || conditionRepository.get(condition.getId()) == null) {
    //         throw new IllegalArgumentException("Condition cannot be null");
    //     }
    // }

    
    // private void validateConditionSet(List<Condition> conditions) {
    //     if (conditions == null || conditions.isEmpty()) {
    //         throw new IllegalArgumentException("Conditions set cannot be null or empty");
    //     }
    //     for (Condition condition : conditions) {
    //         validateCondition(condition);
    //     }
    // }
    
    private void validateDiscount(Discount discount) {
        if (discount == null) {
            throw new IllegalArgumentException("Discount cannot be null");
        }
    }
    
    private void validateDiscountSet(List<Discount> discounts) {
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