package Domain.Store.Discounts.Conditions;

import jakarta.persistence.*;

// import java.util.UUID;
@Entity
@Table(name = "simple_condition")
public abstract class SimpleCondition extends Condition {


    // Constructor for loading from repository with existing ID
    public SimpleCondition(String id) {
        super(id);
    }

    protected SimpleCondition() {
        super(); // JPA only
    }

    @Override
    public String getId() {
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