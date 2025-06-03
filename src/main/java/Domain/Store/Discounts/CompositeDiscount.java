package Domain.Store.Discounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;


public abstract class CompositeDiscount extends Discount {

    protected List<Discount> discounts;
    protected MergeType mergeType;

    public CompositeDiscount(List<Discount> discounts, Condition condition, MergeType mergeType) {
        super(condition);
        this.discounts = discounts;
        this.mergeType = mergeType;
    }

    public CompositeDiscount(List<Discount> discounts, MergeType mergeType) {
        super();
        this.discounts = discounts;
        this.mergeType = mergeType;
    }

    // Constructor for loading from repository with existing ID
    public CompositeDiscount(String id, List<Discount> discounts, Condition condition, MergeType mergeType) {
        super(id, condition);
        this.mergeType = mergeType;
        this.discounts = discounts;
    }

    protected List<Map<String, ItemPriceBreakdown>> calculateAllSubDiscounts(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter) {
        List<Map<String, ItemPriceBreakdown>> allSubDiscounts = new ArrayList<>(discounts.size());
        for (Discount discount : discounts) {
            allSubDiscounts.add(discount.calculatePrice(basket, itemGetter));
        }
        return allSubDiscounts;
    }

    // Getter for repository serialization
    public List<Discount> getDiscounts() {
        return discounts == null ? new ArrayList<>() : new ArrayList<>(discounts);
    }
}