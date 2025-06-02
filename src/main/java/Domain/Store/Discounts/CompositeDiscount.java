package Domain.Store.Discounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Discounts.Conditions.Condition;

public abstract class CompositeDiscount extends Discount {

    protected List<Discount> discounts;

    public CompositeDiscount(List<Discount> discounts, Condition condition) {
        super(condition);
        this.discounts = discounts;
    }

    public CompositeDiscount(List<Discount> discounts) {
        super();
        this.discounts = discounts;
    }

    // Constructor for loading from repository with existing UUID
    public CompositeDiscount(String id, List<Discount> discounts, Condition condition) {
        super(id, condition);
        this.discounts = discounts;
    }

    protected List<Map<String, ItemPriceBreakdown>> calculateAllSubDiscounts(ShoppingBasket basket) {
        List<Map<String, ItemPriceBreakdown>> allSubDiscounts = new ArrayList<>(discounts.size());
        for (Discount discount : discounts) {
            allSubDiscounts.add(discount.calculatePrice(basket));
        }
        return allSubDiscounts;
    }

    // Getter for repository serialization
    public List<Discount> getDiscounts() {
        return discounts == null ? new ArrayList<>() : new ArrayList<>(discounts);
    }
}