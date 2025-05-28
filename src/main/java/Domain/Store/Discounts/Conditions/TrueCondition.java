package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import Domain.Shopping.ShoppingBasket;

public class TrueCondition implements Condition {

    private final UUID id;

    public TrueCondition() {
        this.id = UUID.randomUUID();
    }

    // Constructor for loading from repository with existing UUID
    public TrueCondition(UUID id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id.toString();
    }

    /**
     * Checks if the condition is satisfied.
     * @return true, as this condition is always satisfied
     */
    @Override
    public boolean isSatisfied(ShoppingBasket basket) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrueCondition condition = (TrueCondition) obj;
        return id.equals(condition.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "TrueCondition{id=" + id + "}";
    }
}