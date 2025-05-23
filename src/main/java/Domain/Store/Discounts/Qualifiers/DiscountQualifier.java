package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Product;

public interface DiscountQualifier {

    public boolean isQualified(Product product);

}
