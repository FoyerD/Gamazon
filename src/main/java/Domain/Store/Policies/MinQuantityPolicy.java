package Domain.Store.Policies;
import Domain.Store.Item;
import Domain.User.Member;

/**
 * Enforces a minimum quantity per item.
 * e.g. minItems=5 means you must buy at least 5 of each product.
 */
public class MinQuantityPolicy extends IPolicy 
{
    private final int minItems;
    private final int amountGot; 

    /**
     * Constructor for MinQuantityPolicy.
     * @param minItems The minimum quantity required for each item.
     */
    public MinQuantityPolicy(int minItems, int amountGot, String policyId, String storeId) 
    {
        super(policyId, storeId);        
        this.minItems = minItems;
        this.amountGot = amountGot;
    }

    @Override
    public boolean isApplicable(Item item, Member member) 
    {
        return minItems <= amountGot;
    }
}