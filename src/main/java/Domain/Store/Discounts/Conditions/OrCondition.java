package Domain.Store.Discounts.Conditions;

import java.util.Set;
import java.util.UUID;
import Domain.Shopping.ShoppingBasket;

public class OrCondition extends CompositeCondition{

    public OrCondition(Set<Condition> conditions) {
        super(conditions);
    }

    public OrCondition(Condition condition1, Condition condition2) {
        super(Set.of(condition1, condition2));
    }

    // Constructor for loading from repository with existing UUID
    public OrCondition(UUID id, Set<Condition> conditions) {
        super(id, conditions);
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {
        for (Condition condition : conditions) {
            if (condition.isSatisfied(shoppingBasket)) {
                return true;
            }
        }
        return false;
    }
}