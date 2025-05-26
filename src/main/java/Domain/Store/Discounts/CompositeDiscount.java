package Domain.Store.Discounts;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.Condition;

public abstract class CompositeDiscount extends Discount {

    protected Set<Discount> discounts;

    public CompositeDiscount(ItemFacade itemFacade, Set<Discount> discounts, Condition condition) {
        super(itemFacade, condition);
        this.discounts = discounts;
    }

    public CompositeDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
        super(itemFacade);
        this.discounts = discounts;
    }

    // Constructor for loading from repository with existing UUID
    public CompositeDiscount(UUID id, ItemFacade itemFacade, Set<Discount> discounts, Condition condition) {
        super(id, itemFacade, condition);
        this.discounts = discounts;
    }

    protected Set<Map<String, PriceBreakDown>> calculateAllSubDiscounts(ShoppingBasket basket) {
        Set<Map<String, PriceBreakDown>> allSubDiscounts = new HashSet<>();
        for (Discount discount : discounts) {
            allSubDiscounts.add(discount.calculatePrice(basket));
        }
        return allSubDiscounts;
    }

    // Getter for repository serialization
    public Set<Discount> getDiscounts() {
        return new HashSet<>(discounts);
    }
}