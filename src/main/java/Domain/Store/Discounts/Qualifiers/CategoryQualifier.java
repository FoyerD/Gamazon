package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Category;
import Domain.Store.Item;

public class CategoryQualifier implements DiscountQualifier {

    private Category category;

    public CategoryQualifier(Category category) {
        if(category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        this.category = category;
    }

    @Override
    public boolean isQualified(Item item) {
        return item.getCategories().contains(this.category);  
    }

    public String getCategory() {
        return category.getName();
    }
}
