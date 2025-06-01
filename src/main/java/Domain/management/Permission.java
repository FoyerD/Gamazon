package Domain.management;

import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@IdClass(PermissionId.class)
@Table(name = "permission")
public class Permission {

    @Id
    @Column(name = "store_id")
    private String storeId;

    @Id
    @Column(name = "member_id")
    private String member;

    @Column(nullable = false)
    private String permissionGiverId;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "permission_types", joinColumns = {
        @JoinColumn(name = "store_id", referencedColumnName = "store_id"),
        @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    })
    @Column(name = "permission_type")
    @Enumerated(EnumType.STRING)
    private Set<PermissionType> permissions = new HashSet<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;


    protected Permission() {
        // Required by JPA
    }

    public Permission(String permissionGiverId, String storeId, String member) {
        this.permissionGiverId = permissionGiverId;
        this.storeId = storeId;
        this.member = member;
        this.creationDate = new Date();
        this.expirationDate = new Date(Long.MAX_VALUE);
    }

    public Permission(String permissionGiverId, String storeId, String member, Date expirationDate) {
        this.permissionGiverId = permissionGiverId;
        this.storeId = storeId;
        this.member = member;
        this.creationDate = new Date();
        this.expirationDate = expirationDate;
    }


    public String getStoreId() {
        return storeId;
    }

    public String getMember() {
        return member;
    }

    public String getPermissionGiverId() {
        return permissionGiverId;
    }

    public RoleType getRoleType() {
        return role;
    }

    public Set<PermissionType> getPermissions() {
        return new HashSet<>(permissions);
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }


    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public void setPermissionGiverId(String permissionGiverId) {
        this.permissionGiverId = permissionGiverId;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public void setPermissions(Set<PermissionType> permissionTypes) {
        this.permissions = new HashSet<>(permissionTypes);
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }


    public void addPermission(PermissionType permission) {
        this.permissions.add(permission);
    }

    public void removePermission(PermissionType permission) {
        this.permissions.remove(permission);
    }

    public void clearPermissions() {
        this.permissions.clear();
    }

    public boolean hasPermission(PermissionType permissionType) {
        return permissions.contains(permissionType);
    }

    public boolean isStoreOwner() {
        return role == RoleType.STORE_OWNER;
    }

    public boolean isStoreManager() {
        return role == RoleType.STORE_MANAGER;
    }

    public boolean isStoreFounder() {
        return role == RoleType.STORE_FOUNDER;
    }

    public boolean isTradingManager() {
        return role == RoleType.TRADING_MANAGER;
    }
}
