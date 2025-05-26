package Domain.Store.Discounts.Conditions;

import Domain.Store.ItemFacade;

public abstract class SimpleCondition implements Condition {

    protected ItemFacade itemFacade; // used for evaluating conditions

    public SimpleCondition(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }
}
