package Domain.Store.Discounts;

import java.util.Set;


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

}
