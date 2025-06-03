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
    public OrDiscount(String id, List<Discount> discounts, Condition condition, MergeType mergeType) {
        super(id, discounts, condition, mergeType);
    }

    @Override
    public Map<String, ItemPriceBreakdown> calculatePrice(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter) {

        if (basket == null || basket.getStoreId() == null || basket.getOrders() == null) {
            throw new IllegalArgumentException("Basket, Store ID, and Orders cannot be null");
        }
        
        Map<String, ItemPriceBreakdown> output = new HashMap<>();
        
        // condition only applies if all discounts apply
        if (!conditionApplies(basket, itemGetter)) {
            output = basket.getPriceBreakdowns(itemGetter);
            return output;
        }
        for (Condition cond : this.discounts.stream().map(Discount::getCondition).toList()) {
            if (cond.isSatisfied(basket, itemGetter)) {
                List<Map<String, ItemPriceBreakdown>> allSubDiscounts = calculateAllSubDiscounts(basket, itemGetter);
                switch(mergeType){
                    case MAX:
                        output = ItemPriceBreakdown.combineMaxMap(allSubDiscounts);
                        break;
                    case MUL:
                        output = ItemPriceBreakdown.combineMultiplicateMaps(allSubDiscounts);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported merge type: " + mergeType);
                }
            }
        }


        output = basket.getPriceBreakdowns(itemGetter);
        return output; // If any condition is not satisfied, return original prices
    }
}