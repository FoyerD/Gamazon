package Domain.Store.Discounts.Conditions;

import Domain.Shopping.ShoppingBasket;

public interface Condition {
    
    /**
     * Gets the unique identifier for this condition
     * @return UUID of this condition
     */
    String getId();
    
    /**
     * Checks if the condition is satisfied.
     * @return true if the condition is satisfied, false otherwise
     */
    boolean isSatisfied(ShoppingBasket basket);
}