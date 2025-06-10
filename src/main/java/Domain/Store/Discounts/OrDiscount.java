package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;

public class OrDiscount extends CompositeDiscount {

    // Constructor for loading from repository with existing ID
    public OrDiscount(String id, String storeId, List<Discount> discounts, Condition condition, MergeType mergeType, String description) {
        super(id, storeId, discounts, condition, mergeType);
    }

    public OrDiscount(String id, String storeId, List<Discount> discounts, Condition condition, MergeType mergeType) {
        this(id, storeId, discounts, condition, mergeType, "Default Or Discount Description");
    }

    @Override
    public Map<String, ItemPriceBreakdown> calculatePrice(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter) {

        if (basket == null || itemGetter == null) {
            throw new IllegalArgumentException("Basket and itemGetter cannot be null");
        }
        
        // Get original prices as fallback
        Map<String, ItemPriceBreakdown> originalPrices = basket.getBestPrice(itemGetter);
        
        // If main condition is not satisfied, return original prices
        if (!conditionApplies(basket, itemGetter)) {
            return originalPrices;
        }
        
        // Check if any sub-discount condition is satisfied (OR logic)
        boolean anyConditionSatisfied = false;
        for (Discount discount : this.discounts) {
            if (discount.getCondition().isSatisfied(basket, itemGetter)) {
                anyConditionSatisfied = true;
                break;
            }
        }
        
        // If no sub-conditions are satisfied, return original prices
        if (!anyConditionSatisfied) {
            return originalPrices;
        }
        
        // Calculate all sub-discounts
        List<Map<String, ItemPriceBreakdown>> allSubDiscounts = calculateAllSubDiscounts(basket, itemGetter);
        
        // Combine the sub-discounts based on merge type
        Map<String, ItemPriceBreakdown> result;
        if (mergeType == MergeType.MAX) {
            result = ItemPriceBreakdown.combineMaxMap(allSubDiscounts);
        } else if (mergeType == MergeType.MUL) {
            result = ItemPriceBreakdown.combineMultiplicateMaps(allSubDiscounts);
        } else {
            throw new IllegalArgumentException("Unsupported merge type: " + mergeType);
        }
        
        return result;
    }

}