package Domain.management;

import java.util.Date;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Permission {

    private final String member;
    private final String permissionGiverId;
    private RoleType role;
    private Set<PermissionType> permissions;
    private Date expirationDate;
    private Date creationDate;

    public Permission(String permissionGiverId, String member) {
        this.permissionGiverId = permissionGiverId;
        this.member = member;
        this.permissions = Collections.synchronizedSet(new HashSet<>());
        this.role = null;
        this.creationDate = new Date();
        this.expirationDate = new Date(Long.MAX_VALUE);
    }

    public Permission(String permissionGiverId, String member, Date expDate){
        this.permissionGiverId = permissionGiverId;
        this.member = member;
        this.permissions = Collections.synchronizedSet(new HashSet<>());
        this.role = null;
        this.creationDate = new Date();
        this.expirationDate = expDate;
    }

    public void setPermissions(Set<PermissionType> permissionTypes) {
        this.permissions = Collections.synchronizedSet(new HashSet<>(permissionTypes));
    }

    public RoleType getRoleType() {
        return this.role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public boolean hasPermission(PermissionType permissionType) {
        return permissions.contains(permissionType);
    }

    public Set<PermissionType> getPermissions() {
        return new HashSet<>(permissions);
    }

    public void addPermission(PermissionType permission) {
        permissions.add(permission);
    }

    public void removePermission(PermissionType permission) {
        permissions.remove(permission);
    }

    public void clearPermissions() {
        permissions.clear();
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

    public String getPermissionGiverId() {
        return permissionGiverId;
    }

    public String getMember() {
        return member;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
