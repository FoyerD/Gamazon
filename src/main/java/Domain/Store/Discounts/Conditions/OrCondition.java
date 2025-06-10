package Domain.Store.Discounts.Conditions;

import java.util.List;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import jakarta.persistence.*;

@Entity
@Table(name = "or_condition")
public class OrCondition extends CompositeCondition{

    // Constructor for loading from repository with existing ID
    public OrCondition(String id, List<Condition> conditions) {
        super(id, conditions);
    }

    protected OrCondition() {
        super(); // JPA
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket, BiFunction<String, String, Item> itemGetter) {
        for (Condition condition : conditions) {
            if (condition.isSatisfied(shoppingBasket, itemGetter)) {
                return true;
            }
        }
        return false;
    }
}