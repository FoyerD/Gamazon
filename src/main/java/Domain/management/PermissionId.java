package Domain.management;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class PermissionId implements Serializable {

    private String storeId;
    private String member;

    public PermissionId() {}

    public PermissionId(String storeId, String member) {
        this.storeId = storeId;
        this.member = member;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getMember() {
        return member;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public void setMember(String member) {
        this.member = member;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionId)) return false;
        PermissionId that = (PermissionId) o;
        return Objects.equals(storeId, that.storeId) &&
               Objects.equals(member, that.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, member);
    }
}
