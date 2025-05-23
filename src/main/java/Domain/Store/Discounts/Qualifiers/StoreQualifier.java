package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Product;

public class StoreQualifier implements DiscountQualifier {

    @Override
    public boolean isQualified(Product productId) {
        return true; // all in the store products are qualified
    }

}
