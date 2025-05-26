package Domain.Store.Discounts.Conditions;

public interface Condition {
    
    /**
     * Checks if the condition is satisfied.
     *
     * @return true if the condition is satisfied, false otherwise
     */
    boolean isSatisfied();
}
