package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Item;

public class StoreQualifier implements DiscountQualifier {
    String storeId;

    public StoreQualifier(String storeId) {
        if (storeId == null || storeId.isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        this.storeId = storeId;
    }

    @Override
    public boolean isQualified(Item item) {
        return item.getStoreId() == storeId; // all in the store products are qualified
    }

    public String getStoreId() {
        return storeId;
    }

}
