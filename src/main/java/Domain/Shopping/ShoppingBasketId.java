package Domain.Shopping;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the composite primary key for ShoppingBasket entities.
 * Combines storeId and clientId to form a unique identifier.
 */
public class ShoppingBasketId implements Serializable {
    private String storeId;
    private String clientId;

    public ShoppingBasketId() {
        // Required by JPA
    }

    public ShoppingBasketId(String storeId, String clientId) {
        this.storeId = storeId;
        this.clientId = clientId;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShoppingBasketId)) return false;
        ShoppingBasketId that = (ShoppingBasketId) o;
        return Objects.equals(storeId, that.storeId) &&
               Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, clientId);
    }
} 