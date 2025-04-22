package Domain;

import java.util.HashSet;
import java.util.Set;

public class Permission {

    // Store owner according to Section 4
    public static final Set<PermissionType> OWNER_PERMISSIONS = Set.of(
        PermissionType.SUPERVISE_MANAGERS,
        PermissionType.ASSIGN_OR_REMOVE_OWNERS,
        PermissionType.MODIFY_OWNER_RIGHTS,
        PermissionType.HANDLE_INVENTORY,
        PermissionType.EDIT_STORE_POLICIES,
        PermissionType.VIEW_EMPLOYEE_INFO,
        PermissionType.ACCESS_PURCHASE_RECORDS,
        PermissionType.ADMINISTER_STORE,
        PermissionType.OVERSEE_OFFERS,
        PermissionType.CONTROL_CONTRACTS
    );

    // Manages the trading system according to Section 4
    public static final Set<PermissionType> FOUNDER_PERMISSIONS = Set.of(
        PermissionType.ASSIGN_OR_REMOVE_OWNERS,
        PermissionType.SUPERVISE_MANAGERS,
        PermissionType.DEACTIVATE_STORE,
        PermissionType.HANDLE_INVENTORY,
        PermissionType.EDIT_STORE_POLICIES,
        PermissionType.MODIFY_OWNER_RIGHTS,
        PermissionType.VIEW_EMPLOYEE_INFO,
        PermissionType.ACCESS_PURCHASE_RECORDS,
        PermissionType.ADMINISTER_STORE,
        PermissionType.OVERSEE_OFFERS,
        PermissionType.CONTROL_CONTRACTS
    );

    // Manages a store according to Section 5
    public static final Set<PermissionType> MANAGER_PERMISSIONS = Set.of(PermissionType.ACCESS_PURCHASE_RECORDS);    

    // Trading system manager according to Section 6

    public static final Set<PermissionType> TRADING_PERMISSIONS = Set.of(
        PermissionType.ASSIGN_OR_REMOVE_OWNERS,
        PermissionType.SUPERVISE_MANAGERS,
        PermissionType.DEACTIVATE_STORE,
        PermissionType.HANDLE_INVENTORY,
        PermissionType.EDIT_STORE_POLICIES,
        PermissionType.MODIFY_OWNER_RIGHTS,
        PermissionType.VIEW_EMPLOYEE_INFO,
        PermissionType.ACCESS_PURCHASE_RECORDS,
        PermissionType.ADMINISTER_STORE,
        PermissionType.OVERSEE_OFFERS,
        PermissionType.CONTROL_CONTRACTS
    );

    private final String member;
    private final String permissionGiverName;
    private RoleType role;
    private Set<PermissionType> permissions;

    public Permission(String permissionGiverName, String member) {
        this.permissionGiverName = permissionGiverName;
        this.member = member;
        this.role = null;
        this.permissions = new HashSet<>();
    }

    public void initStoreManager() {
        this.role = RoleType.STORE_MANAGER;
        this.permissions = new HashSet<>(MANAGER_PERMISSIONS);
    }

    public void initStoreOwner() {
        this.role = RoleType.STORE_OWNER;
        this.permissions = new HashSet<>(OWNER_PERMISSIONS);
    }

    public void initStoreFounder() {
        this.role = RoleType.STORE_FOUNDER;
        this.permissions = new HashSet<>(FOUNDER_PERMISSIONS);
    }

    public void initTradingManager() {
        this.role = RoleType.TRADING_MANAGER;
        this.permissions = new HashSet<>(TRADING_PERMISSIONS);
    }

    public void setPermissions(Set<PermissionType> permissionTypes) {
        this.permissions = new HashSet<>(permissionTypes);
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

    public String getPermissionGiverName() {
        return permissionGiverName;
    }

    public String getMember() {
        return member;
    }
}
