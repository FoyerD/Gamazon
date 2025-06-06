package Domain.Store;

import java.io.Serializable;
import java.util.Objects;

public class ItemId implements Serializable {
    private String storeId;
    private String productId;

    public ItemId() {}  // Required by JPA

    public ItemId(String storeId, String productId) {
        this.storeId = storeId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemId)) return false;
        ItemId itemId = (ItemId) o;
        return Objects.equals(storeId, itemId.storeId) &&
               Objects.equals(productId, itemId.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, productId);
    }
}
