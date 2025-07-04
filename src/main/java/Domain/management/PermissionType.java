package Domain.management;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public enum PermissionType {
    
    ASSIGN_OR_REMOVE_OWNERS,
    SUPERVISE_MANAGERS,
    OPEN_DEACTIVATE_STORE,
    BANNED,
    BAN_USERS,    
    HANDLE_INVENTORY,
    EDIT_STORE_POLICIES,
    MODIFY_OWNER_RIGHTS,
    VIEW_EMPLOYEE_INFO,
    ACCESS_PURCHASE_RECORDS,
    RESPOND_TO_INQUIRIES,
    ADMINISTER_STORE,
    OVERSEE_OFFERS,
    CONTROL_CONTRACTS;

    @Override
    public String toString() {
        // Converts enum name to "Title Case" with spaces
        return name().toLowerCase()
                     .replace('_', ' ');
    }
    
    public static Set<PermissionType> collectionToSet(Collection<PermissionType> perms) {
        return EnumSet.copyOf(perms);
    }

    public static boolean hasPermission(Set<PermissionType> set, PermissionType permission) {
        return set.contains(permission);
    }
}
