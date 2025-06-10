package Domain.Store.Discounts.Conditions;

import java.util.List;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import jakarta.persistence.*;

@Entity
@Table(name = "and_condition")
public class AndCondition extends CompositeCondition {

     protected AndCondition() {
        super(); // JPA only
    }

    // Constructor for loading from repository with existing ID
    public AndCondition(String id, List<Condition> conditions) {
        super(id, conditions);
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket, BiFunction<String, String, Item> itemGetter) {
        for (Condition condition : conditions) {
            if (!condition.isSatisfied(shoppingBasket, itemGetter)) {
                return false;
            }
        }
        return true;
    }
}