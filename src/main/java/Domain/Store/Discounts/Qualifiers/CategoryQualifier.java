package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Category;
import Domain.Store.Item;
import jakarta.persistence.*;


@Entity
@Table(name = "category_qualifier")
public class CategoryQualifier extends DiscountQualifier {

    private Category category;

    public CategoryQualifier(Category category) {
        super();
        if(category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        this.category = category;
    }

    protected CategoryQualifier() {
        super(); // JPA
    }

    @Override
    public boolean isQualified(Item item) {
        return item.getCategories().contains(this.category);  
    }

    public String getCategory() {
        return category.getName();
    }
}
