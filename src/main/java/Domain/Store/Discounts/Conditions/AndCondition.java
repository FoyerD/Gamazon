package Domain.Store.Discounts.Conditions;

import java.util.Set;
import java.util.UUID;
import Domain.Shopping.ShoppingBasket;

public class AndCondition extends CompositeCondition {

    public AndCondition(Set<Condition> conditions) {
        super(conditions);
    }

    public AndCondition(Condition condition1, Condition condition2) {
        super(Set.of(condition1, condition2));
    }

    // Constructor for loading from repository with existing UUID
    public AndCondition(UUID id, Set<Condition> conditions) {
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