package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import Domain.Shopping.ShoppingBasket;

public interface Condition {
    
    /**
     * Gets the unique identifier for this condition
     * @return UUID of this condition
     */
    UUID getId();
    
    /**
     * Checks if the condition is satisfied.
     * @return true if the condition is satisfied, false otherwise
     */
    boolean isSatisfied(ShoppingBasket basket);
}