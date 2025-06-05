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
    public OrDiscount(String id, String storeId, List<Discount> discounts, Condition condition, MergeType mergeType) {
        super(id, storeId, discounts, condition, mergeType);
    }

    @Override
    public Map<String, ItemPriceBreakdown> calculatePrice(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter) {

        if (basket == null || basket.getStoreId() == null || basket.getOrders() == null) {
            throw new IllegalArgumentException("Basket, Store ID, and Orders cannot be null");
        }
        
        // If main condition doesn't apply, return original prices
        if (!conditionApplies(basket, itemGetter)) {
            return basket.getPriceBreakdowns(itemGetter);
        }
        
        // Check if ANY sub-condition is satisfied (OR logic)
        boolean anyConditionSatisfied = false;
        for (Condition cond : this.discounts.stream().map(Discount::getCondition).toList()) {
            if (cond.isSatisfied(basket, itemGetter)) {
                anyConditionSatisfied = true;
                break; // Found at least one satisfied condition
            }
        }
        
        // If no sub-conditions are satisfied, return original prices
        if (!anyConditionSatisfied) {
            return basket.getPriceBreakdowns(itemGetter);
        }
        
        // Apply discounts since at least one condition is satisfied
        List<Map<String, ItemPriceBreakdown>> allSubDiscounts = calculateAllSubDiscounts(basket, itemGetter);
        
        if (mergeType == MergeType.MAX) {
            return ItemPriceBreakdown.combineMaxMap(allSubDiscounts);
        } else if (mergeType == MergeType.MUL) {
            return ItemPriceBreakdown.combineMultiplicateMaps(allSubDiscounts);
        } else {
            throw new IllegalArgumentException("Unsupported merge type: " + mergeType);
        }
    }
    
}