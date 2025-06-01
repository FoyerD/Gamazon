package Domain.Store.Discounts;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import Domain.Store.Discounts.ItemPriceBreakdown;
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

    protected Set<Map<String, ItemPriceBreakdown>> calculateAllSubDiscounts(ShoppingBasket basket) {
        Set<Map<String, ItemPriceBreakdown>> allSubDiscounts = new HashSet<>();
        for (Discount discount : discounts) {
            allSubDiscounts.add(discount.calculatePrice(basket));
        }
        return allSubDiscounts;
    }

    // Getter for repository serialization
    public Set<Discount> getDiscounts() {
        return new HashSet<>(discounts);
    }

        // Helper method that validates parameters and creates the set
    protected static Set<Discount> validateAndCreateSet(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        if (itemFacade == null) {
            throw new IllegalArgumentException("ItemFacade cannot be null");
        }
        if (discount1 == null) {
            throw new IllegalArgumentException("discount1 cannot be null");
        }
        if (discount2 == null) {
            throw new IllegalArgumentException("discount2 cannot be null");
        }
        
        Set<Discount> discounts = new HashSet<>();
        discounts.add(discount1);
        discounts.add(discount2);
        return discounts;
    }


}