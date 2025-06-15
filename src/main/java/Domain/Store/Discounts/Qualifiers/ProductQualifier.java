package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Item;

import jakarta.persistence.*;

@Entity
@Table(name = "product_qualifier")
public class ProductQualifier extends DiscountQualifier {

    private String productId;

    public ProductQualifier(String productId) {
        super();
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        this.productId = productId;
    }

    protected ProductQualifier() {
        super(); // JPA
    }
    
    @Override
    public boolean isQualified(Item item) {
        if (item == null || item.getProductId() == null) {
            throw new IllegalArgumentException("Product or product ID cannot be null");
        }

        return item.getProductId().equals(this.productId);
    }

    public String getProductId() {
        return productId;
    }
}
