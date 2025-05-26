package Domain.Store.Discounts.Conditions;

import java.util.Set;

public abstract class CompositeCondition implements Condition {

    protected Set<Condition> conditions;

    public CompositeCondition(Set<Condition> conditions) {
        this.conditions = conditions;
    }

}
