package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class CompositeCondition implements Condition {

    protected final String id;
    protected List<Condition> conditions;

    public CompositeCondition(List<Condition> conditions) {
        this.id = UUID.randomUUID().toString();
        this.conditions = conditions;
    }

    // Constructor for loading from repository with existing UUID
    public CompositeCondition(String id, List<Condition> conditions) {
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