package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Item;

public interface DiscountQualifier {
    public boolean isQualified(Item item);

}
