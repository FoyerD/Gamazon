package Domain.Store.Policies;
import Domain.Store.Item;

import Domain.User.Member;

import java.util.Arrays;
import java.util.List;

/**
 * Combines multiple policies using a logical AND operation.
 * All policies must be applicable for the combined policy to be applicable.
 */
public class AndPolicy extends IPolicy 
{
    private final List<IPolicy> policies;

    public AndPolicy(List<IPolicy> policies, String policyId, String storeId) 
    {
        super(policyId, storeId);
        this.policies = policies;
    }

    @Override
    public boolean isApplicable(Item item, Member member) 
    {
        for (IPolicy p : policies) 
        {
            if (!p.isApplicable(item, member)) 
            {
                return false; 
            }
        }
        return true;
    }
}

