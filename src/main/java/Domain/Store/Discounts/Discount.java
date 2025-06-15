package Domain.Store.Discounts;

import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "discount")
public abstract class Discount {
    public enum MergeType {
        MAX,
        MUL
    }

    @Id
    protected final String id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "condition_id")
    protected Condition condition;
    protected String storeId;
    protected String description;

    // Constructor for loading from repository with existing ID
    public Discount(String id, String storeId, Condition condition, String description) {
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
        this.description = description; // Default description, can be set later
    }

    public Discount(String id, String storeId, Condition condition) {
        this(id, storeId, condition, "Default Discount Description");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.description = description;
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