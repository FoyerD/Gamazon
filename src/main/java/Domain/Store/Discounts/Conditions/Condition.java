package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "condition")
@Access(AccessType.FIELD)
@DiscriminatorColumn(name = "condition_type")
public abstract class Condition {
    @Id
    protected final String id;

    protected Condition(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        this.id = id;
    }

    protected Condition() {
    this.id = UUID.randomUUID().toString();
}
    /**
     * Gets the unique identifier for this condition
     * @return UUID of this condition
     */
    String getId(){
        return id;
    }
    
    /**
     * Checks if the condition is satisfied.
     * @return true if the condition is satisfied, false otherwise
     */
    public abstract boolean isSatisfied(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter);
}