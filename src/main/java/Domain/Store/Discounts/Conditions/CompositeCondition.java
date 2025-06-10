package Domain.Store.Discounts.Conditions;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "composite_condition")
public abstract class CompositeCondition extends Condition {

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "composite_condition_children",
        joinColumns = @JoinColumn(name = "composite_condition_id"),
        inverseJoinColumns = @JoinColumn(name = "child_condition_id")
    )
    protected List<Condition> conditions;

    // Constructor for loading from repository with existing ID
    public CompositeCondition(String id, List<Condition> conditions) {
        super(id);
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException("Conditions list cannot be null or empty");
        }
        this.conditions = conditions;
    }

    protected CompositeCondition() {
        super(); // JPA only
        this.conditions = new ArrayList<>();
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