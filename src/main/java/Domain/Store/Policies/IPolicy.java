package Domain.Store.Policies;

import Domain.User.Member;
import Domain.Store.Item;

import Domain.User.User;

public abstract class IPolicy 
{
    private String policyId;
    private String storeId;

    public IPolicy(String policyId, String storeId) {
        this.policyId = policyId;
        this.storeId = storeId;
    }
    /**
     * Checks if the policy is applicable to the given item.
     *
     * @param item The item to check.
     * @return True if the policy applies, false otherwise.
     */
    public abstract boolean isApplicable(Item item, Member member);

    public String getPolicyId() 
    {
        return policyId;
    }

    public String getStoreId() 
    {
        return storeId;
    }
    public void setStoreId(String storeId2) {
        this.storeId = storeId2;
    }
}
