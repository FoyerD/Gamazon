package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Category;
import Domain.Store.Product;

public class CategoryQualifier implements DiscountQualifier {

    private Category category;

    public CategoryQualifier(Category category) {
        this.category = category;
    }

    @Override
    public boolean isQualified(Product product) {
        return product.getCategories().contains(this.category);  
    }

}
