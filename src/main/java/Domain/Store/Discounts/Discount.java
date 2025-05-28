package Domain.Store.Discounts;

import java.util.Map;
import java.util.UUID;
import Domain.Store.Discounts.Conditions.Condition;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.TrueCondition;

public abstract class Discount {

    protected final UUID id;
    protected ItemFacade itemFacade;
    protected Condition condition;

    public Discount(ItemFacade itemFacade, Condition condition) {
        this.id = UUID.randomUUID();
        this.itemFacade = itemFacade;
        this.condition = condition;
    }

    public Discount(ItemFacade itemFacade) {
        this.id = UUID.randomUUID();
        this.itemFacade = itemFacade;
        this.condition = new TrueCondition();
    }

    // Constructor for loading from repository with existing UUID
    public Discount(UUID id, ItemFacade itemFacade, Condition condition) {
        this.id = id;
        this.itemFacade = itemFacade;
        this.condition = condition;
    }

    public String getId() {
        return id.toString();
    }

    // Outputs a map from product to the price breakdown
    public abstract Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket);

    public abstract boolean isQualified(String productId);

    public boolean conditionApplies(ShoppingBasket basket) {
        if (condition == null) {
            return true; // If no condition is set, we assume it applies
        }
        return condition.isSatisfied(basket);
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