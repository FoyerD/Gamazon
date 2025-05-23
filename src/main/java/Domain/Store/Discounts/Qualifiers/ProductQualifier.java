package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Product;

public class ProductQualifier implements DiscountQualifier {

    private String productId;

    public ProductQualifier(String productId) {
        this.productId = productId;
    }

    @Override
    public boolean isQualified(Product product) {
        return product.getProductId().equals(this.productId);
    }

}
