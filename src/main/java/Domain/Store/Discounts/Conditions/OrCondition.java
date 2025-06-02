package Domain.Store.Discounts.Conditions;

import java.util.List;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class OrCondition extends CompositeCondition{

    public OrCondition(List<Condition> conditions) {
        super(conditions);
    }

    public OrCondition(Condition condition1, Condition condition2) {
        super(List.of(condition1, condition2));
    }

    // Constructor for loading from repository with existing UUID
    public OrCondition(String id, List<Condition> conditions) {
        super(id, conditions);
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