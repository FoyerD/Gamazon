package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Product;

public class ProductQualifier implements DiscountQualifier {

    private String productId;

    public ProductQualifier(String productId) {
        this.productId = productId;
    }

    @Override
    public boolean isQualified(Product product) {
        if (product == null || product.getProductId() == null) {
            throw new IllegalArgumentException("Product or product ID cannot be null");
        }

        return product.getProductId().equals(this.productId);
    }

    public String getProductId() {
        return productId;
    }
}
