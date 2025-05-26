package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import Domain.Store.ItemFacade;

public abstract class SimpleCondition implements Condition {

    protected final UUID id;
    protected ItemFacade itemFacade; // used for evaluating conditions

    public SimpleCondition(ItemFacade itemFacade) {
        this.id = UUID.randomUUID();
        this.itemFacade = itemFacade;
    }

    // Constructor for loading from repository with existing UUID
    public SimpleCondition(UUID id, ItemFacade itemFacade) {
        this.id = id;
        this.itemFacade = itemFacade;
    }

    @Override
    public UUID getId() {
        return id;
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