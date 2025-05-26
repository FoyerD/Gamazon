package Domain.Store.Policies;

import Domain.User.Member;
import Domain.Store.Category;
import Domain.Store.Item;

/**
 * Restricts purchases to a single disallowed product category.
 * If an item belongs to that category it cannot be purchased.
 */
public class CategoryPolicy extends IPolicy 
{
    private final String disallowedCategory;

    public CategoryPolicy(String disallowedCategory, String policyId, String storeId) 
    {
        super(policyId, storeId);
        if (disallowedCategory == null || disallowedCategory.isBlank()) 
        {
            throw new IllegalArgumentException("Disallowed category cannot be null or empty");
        }
        this.disallowedCategory = disallowedCategory.toLowerCase();
    }

    @Override
    public boolean isApplicable(Item item, Member member) 
    {
        for (Category category : item.getCategories()) 
        {
            if (category.getName().toLowerCase().equalsIgnoreCase(disallowedCategory)) 
            {
                return false;
            }
        }
        return true;
    }

    public String getDisallowedCategory() 
    {
        return disallowedCategory;
    }
}
