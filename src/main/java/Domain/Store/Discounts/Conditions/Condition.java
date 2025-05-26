package Domain.Store.Discounts.Conditions;

import Domain.Shopping.ShoppingBasket;

public interface Condition {
    
    /**
     * Checks if the condition is satisfied.
     *
     * @return true if the condition is satisfied, false otherwise
     */
    boolean isSatisfied(ShoppingBasket basket);
}
