package Domain.management;

import java.util.Set;

public class PermissionFactory {

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
        PermissionType.CONTROL_CONTRACTS,
        PermissionType.OPEN_DEACTIVATE_STORE,
        PermissionType.RESPOND_TO_INQUIRIES
    );

    public static final Set<PermissionType> FOUNDER_PERMISSIONS = Set.of(
        PermissionType.ASSIGN_OR_REMOVE_OWNERS,
        PermissionType.SUPERVISE_MANAGERS,
        PermissionType.OPEN_DEACTIVATE_STORE,
        PermissionType.HANDLE_INVENTORY,
        PermissionType.EDIT_STORE_POLICIES,
        PermissionType.MODIFY_OWNER_RIGHTS,
        PermissionType.VIEW_EMPLOYEE_INFO,
        PermissionType.ACCESS_PURCHASE_RECORDS,
        PermissionType.ADMINISTER_STORE,
        PermissionType.OVERSEE_OFFERS,
        PermissionType.CONTROL_CONTRACTS
    );

    public static final Set<PermissionType> MANAGER_PERMISSIONS = Set.of(
        PermissionType.ACCESS_PURCHASE_RECORDS
    );

    public static final Set<PermissionType> TRADING_PERMISSIONS = Set.of(
        PermissionType.ASSIGN_OR_REMOVE_OWNERS,
        PermissionType.SUPERVISE_MANAGERS,
        PermissionType.OPEN_DEACTIVATE_STORE,
        PermissionType.HANDLE_INVENTORY,
        PermissionType.EDIT_STORE_POLICIES,
        PermissionType.MODIFY_OWNER_RIGHTS,
        PermissionType.VIEW_EMPLOYEE_INFO,
        PermissionType.ACCESS_PURCHASE_RECORDS,
        PermissionType.ADMINISTER_STORE,
        PermissionType.OVERSEE_OFFERS,
        PermissionType.CONTROL_CONTRACTS,
        PermissionType.BAN_USERS
    );

    public static final Set<PermissionType> BANNED_PERMISSIONS = Set.of(
        PermissionType.BANNED
    );

    public static void initPermissionAsRole(Permission permission, RoleType roleType) {
        permission.setRole(roleType);
        switch (roleType) {
            case STORE_MANAGER -> permission.setPermissions(MANAGER_PERMISSIONS);
            case STORE_OWNER -> permission.setPermissions(OWNER_PERMISSIONS);
            case STORE_FOUNDER -> permission.setPermissions(FOUNDER_PERMISSIONS);
            case TRADING_MANAGER -> permission.setPermissions(TRADING_PERMISSIONS);
            case BANNED_USER -> permission.setPermissions(BANNED_PERMISSIONS);
        }
    }
}
