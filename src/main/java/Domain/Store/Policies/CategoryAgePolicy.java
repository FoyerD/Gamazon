package Domain.Store.Policies;

import Domain.User.User;
import Domain.User.Member;

import Domain.Store.Category;
import Domain.Store.Item;
import Domain.Store.Policies.IPolicy;



public class CategoryAgePolicy extends IPolicy {
    private final int minAge;
    private String category = "";

    public CategoryAgePolicy(int minAge, String category, String policyId, String storeId) {
        super(policyId, storeId);
        this.minAge = minAge;
        this.category = category.toLowerCase();
    }

    @Override
    public boolean isApplicable(Item item, Member member) 
    {
        for(Category category : item.getCategories()) 
        {
            if (category.getName().toLowerCase() == this.category && member.getAge() < minAge) 
            {
                return false;
            }
        }
        return true;
    }

    public int getMinAge() {
        return minAge;
    }

}
