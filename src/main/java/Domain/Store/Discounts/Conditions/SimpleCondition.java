package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import java.util.function.BiFunction;

import Domain.Store.Item;
import Domain.Store.ItemFacade;

public abstract class SimpleCondition implements Condition {

    protected final String id;
    protected BiFunction<String, String, Item> itemGetter; // used for evaluating conditions

    public SimpleCondition(ItemFacade itemFacade) {
        this.id = UUID.randomUUID().toString();
        this.itemGetter = itemFacade::getItem;
    }

    // Constructor for loading from repository with existing UUID
    public SimpleCondition(String id, ItemFacade itemFacade) {
        this.id = id;
        this.itemGetter = itemFacade::getItem;
    }

    @Override
    public String getId() {
        return id.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SimpleCondition condition = (SimpleCondition) obj;
        return id.equals(condition.id);
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