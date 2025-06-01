package Domain.management;

import java.io.Serializable;
import java.util.Objects;

public class PermissionId implements Serializable {
    private String storeId;
    private String userId;

    public PermissionId() {
        // Required by JPA
    }

    public PermissionId(String storeId, String userId) {
        this.storeId = storeId;
        this.userId = userId;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionId)) return false;
        PermissionId that = (PermissionId) o;
        return Objects.equals(storeId, that.storeId) &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, userId);
    }
}
