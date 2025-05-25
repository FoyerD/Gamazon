package Domain.Store.Policies;

import Domain.Store.Item;


import Domain.User.Member;

/**
 * Enforces a maximum quantity per item.
 * e.g. maxItems=10 means you cannot buy more than 10 of any single product.
 */
public class MaxQuantityPolicy extends IPolicy {
    private final int maxItems;
    private final int amountGot; 
    

    /**
     * Constructor for MinQuantityPolicy.
     * @param minItems The minimum quantity required for each item.
     */
    public MaxQuantityPolicy(int maxItems, int amountGot, String policyId, String storeId) 
    {
        super(policyId, storeId);
        this.maxItems = maxItems;
        this.amountGot = amountGot;
    }

    @Override
    public boolean isApplicable(Item item, Member member) 
    {
        return maxItems <= amountGot;
    }
}

