package Domain.Store.Discounts.Conditions;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeCondition implements Condition {

    protected final String id;
    protected List<Condition> conditions;

    // Constructor for loading from repository with existing ID
    public CompositeCondition(String id, List<Condition> conditions) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException("Conditions list cannot be null or empty");
        }
        this.id = id;
        this.conditions = conditions;
    }

    @Override
    public String getId() {
        return id.toString();
    }

    // Getter for repository serialization
    public List<Condition> getConditions() {
        return conditions == null ? new ArrayList<>() : new ArrayList<>(conditions);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompositeCondition condition = (CompositeCondition) obj;
        return id.equals(condition.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{id=" + id + ", conditions=" + conditions.size() + "}";
    }
}