package Domain.Store.Discounts.Conditions;

import Domain.Shopping.ShoppingBasket;

public class TrueCondition implements Condition {

    /**
     * Checks if the condition is satisfied.
     *
     * @return true, as this condition is always satisfied
     */
    @Override
    public boolean isSatisfied(ShoppingBasket basket) {
        return true;
    }

    @Override
    public String toString() {
        return "True";
    }
    
}
