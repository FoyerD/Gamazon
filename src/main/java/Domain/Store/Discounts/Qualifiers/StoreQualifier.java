package Domain.Store.Discounts.Qualifiers;

import Domain.Store.Item;

import jakarta.persistence.*;

@Entity
@Table(name = "store_qualifier")
public class StoreQualifier extends DiscountQualifier {
    String storeId;

    public StoreQualifier(String storeId) {
        super();
        if (storeId == null || storeId.isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        this.storeId = storeId;
    }

    protected StoreQualifier() {
        super(); // JPA
    }

    @Override
    public boolean isQualified(Item item) {
        return item.getStoreId() == storeId; // all in the store products are qualified
    }

    public String getStoreId() {
        return storeId;
    }

}
