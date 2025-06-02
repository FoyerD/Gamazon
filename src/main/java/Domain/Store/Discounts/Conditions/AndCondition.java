package Domain.Store.Discounts.Conditions;

import java.util.List;
import Domain.Shopping.ShoppingBasket;

public class AndCondition extends CompositeCondition {

    public AndCondition(List<Condition> conditions) {
        super(conditions);
    }

    public AndCondition(Condition condition1, Condition condition2) {
        super(List.of(condition1, condition2));
    }

    // Constructor for loading from repository with existing UUID
    public AndCondition(String id, List<Condition> conditions) {
        super(id, conditions);
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {
        for (Condition condition : conditions) {
            if (!condition.isSatisfied(shoppingBasket)) {
                return false;
            }
        }
        return true;
    }
}