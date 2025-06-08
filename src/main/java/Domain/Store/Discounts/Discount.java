package Domain.Store.Discounts;

import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;

public abstract class Discount {
    public enum MergeType {
        MAX,
        MUL
    }

    protected final String id;
    protected Condition condition;
    protected String storeId;

    // Constructor for loading from repository with existing ID
    public Discount(String id, String storeId, Condition condition) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (storeId == null || storeId.isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        this.id = id;
        this.condition = condition;
        this.storeId = storeId;
    }

    public String getId() {
        return id.toString();
    }

    public String getStoreId() {
        return storeId;
    }
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    // Outputs a map from product to the price breakdown
    public abstract Map<String, ItemPriceBreakdown> calculatePrice(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter);

    public boolean conditionApplies(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter) {
        if (condition == null) {
            return true; // If no condition is set, we assume it applies
        }
        return condition.isSatisfied(basket, itemGetter);
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Discount discount = (Discount) obj;
        return id.equals(discount.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{id=" + id + "}";
    }
}