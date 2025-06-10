package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class TrueCondition implements Condition {

    private final String id;

    public TrueCondition() {
        this.id = UUID.randomUUID().toString();
    }

    // Constructor for loading from repository with existing ID
    public TrueCondition(String id) {
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
    public boolean isSatisfied(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter) {
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
        return id.toString().hashCode();
    }

    @Override
    public String toString() {
        return "TrueCondition{id=" + id + "}";
    }
}