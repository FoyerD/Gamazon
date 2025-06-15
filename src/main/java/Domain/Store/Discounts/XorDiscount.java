package Domain.Store.Discounts;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;
import jakarta.persistence.*;

@Entity
@Table(name = "xor_discount")
public class XorDiscount extends CompositeDiscount {

    // Constructor for loading from repository with existing ID
    public XorDiscount(String id, String storeId, Discount discount1, Discount discount2, Condition condition, MergeType mergeType, String description) {
        super(id, storeId, List.of(discount1, discount2), condition, mergeType);
    }

    public XorDiscount(String id, String storeId, Discount discount1, Discount discount2, Condition condition, MergeType mergeType) {
        this(id, storeId, discount1, discount2, condition, mergeType, "Default Xor Discount Description");
    }

    @Override
    public Map<String, ItemPriceBreakdown> calculatePrice(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter) {        
        Map<String, ItemPriceBreakdown> output = null;
        if (basket == null || basket.getStoreId() == null || basket.getOrders() == null) {
            throw new IllegalArgumentException("Basket, Store ID, and Orders cannot be null");
        }
        if (!conditionApplies(basket, itemGetter)) {
            output = this.discounts.get(0).calculatePrice(basket, itemGetter);
        }
        else{
            output = this.discounts.get(1).calculatePrice(basket, itemGetter);
        }

        return output;
    }
}