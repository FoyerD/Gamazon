package Domain.Store.Discounts;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;
import jakarta.persistence.*;

@Entity
@Table(name = "and_discount")
public class AndDiscount extends CompositeDiscount {


    // Constructor for loading from repository with existing ID
    public AndDiscount(String id, String storeId, List<Discount> discounts, Condition condition, MergeType mergeType, String description) {
        super(id, storeId, discounts, condition, mergeType, description);
    }

    public AndDiscount(String id, String storeId, List<Discount> discounts, Condition condition, MergeType mergeType) {
        this(id, storeId, discounts, condition, mergeType, "Default And Discount Description");
    }

    @Override
    public Map<String, ItemPriceBreakdown> calculatePrice(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter) {

        if (basket == null || basket.getStoreId() == null || basket.getOrders() == null) {
            throw new IllegalArgumentException("Basket, Store ID, and Orders cannot be null");
        }
        
        Map<String, ItemPriceBreakdown> output = null;
        
        // condition only applies if all discounts apply
        if (!conditionApplies(basket, itemGetter)) {
            output = basket.getBestPrice(itemGetter);
            return output;
        }
        for (Condition cond : this.discounts.stream().map(Discount::getCondition).toList()) {
            if (!cond.isSatisfied(basket, itemGetter)) {
                output = basket.getBestPrice(itemGetter);
                return output; // If any condition is not satisfied, return original prices
            }
        }

        List<Map<String, ItemPriceBreakdown>> allSubDiscounts = calculateAllSubDiscounts(basket, itemGetter);
        if (mergeType == MergeType.MAX) {
            output = ItemPriceBreakdown.combineMaxMap(allSubDiscounts);
        } else if (mergeType == MergeType.MUL) {
            output = ItemPriceBreakdown.combineMultiplicateMaps(allSubDiscounts);
        } else {
            throw new IllegalArgumentException("Unsupported merge type: " + mergeType);
        }

        return output;
    }
}