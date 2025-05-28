package Domain.Store.Discounts.Conditions;

import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

public abstract class CompositeCondition implements Condition {

    protected final UUID id;
    protected Set<Condition> conditions;

    public CompositeCondition(Set<Condition> conditions) {
        this.id = UUID.randomUUID();
        this.conditions = conditions;
    }

    // Constructor for loading from repository with existing UUID
    public CompositeCondition(UUID id, Set<Condition> conditions) {
        this.id = id;
        this.conditions = conditions;
    }

    @Override
    public String getId() {
        return id.toString();
    }

    // Getter for repository serialization
    public Set<Condition> getConditions() {
        return new HashSet<>(conditions);
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