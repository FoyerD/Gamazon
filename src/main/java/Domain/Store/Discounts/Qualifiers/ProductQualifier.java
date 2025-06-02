package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Item;

public class ProductQualifier implements DiscountQualifier {

    private String productId;

    public ProductQualifier(String productId) {
        this.productId = productId;
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
