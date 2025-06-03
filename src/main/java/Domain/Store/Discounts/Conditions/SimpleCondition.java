package Domain.Store.Discounts.Conditions;

import java.util.UUID;

public abstract class SimpleCondition implements Condition {

    protected final String id;

    // Constructor for loading from repository with existing ID
    public SimpleCondition(String id) {
        this.id = id;
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