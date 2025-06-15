package Domain.Store.Discounts.Qualifiers;

import java.util.UUID;

import Domain.Store.Item;
import jakarta.persistence.*;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "discount_qualifier")
public abstract class DiscountQualifier {
    @Id
    protected String id;

    protected DiscountQualifier() {
        this.id = UUID.randomUUID().toString();
    }

    protected DiscountQualifier(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    
    public abstract boolean isQualified(Item item);
    
}
