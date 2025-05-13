package Domain.management;

import java.util.List;
import java.util.Set;

import Infrastructure.Repositories.MemoryPermissionRepository;

public class PermissionManager {
    private IPermissionRepository permissionRepository;

    public PermissionManager() {
        this.permissionRepository = null;
    }
    public PermissionManager(IPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public void setPermissionRepository(IPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public void appointStoreManager(String appointerUsername, String appointeeUsername, String storeId) {
        checkPermission(appointerUsername, storeId, PermissionType.SUPERVISE_MANAGERS);
        getOrCreatePermission(appointerUsername, appointeeUsername, storeId, RoleType.STORE_MANAGER);
    }

    public void removeStoreManager(String removerUsername, String managerUsername, String storeId) {
        checkPermission(removerUsername, storeId, PermissionType.SUPERVISE_MANAGERS);
        Permission permission = permissionRepository.get(storeId, managerUsername);
        if (permission == null || !permission.isStoreManager()) {
            throw new IllegalStateException(managerUsername + " is not a manager.");
        }
        permission.setPermissions(Set.of());
        permission.setRole(null);
        permissionRepository.update(storeId, managerUsername, permission);
    }


    public void appointStoreOwner(String appointerUsername, String appointeeUsername, String storeId) {
        checkPermission(appointerUsername, storeId, PermissionType.ASSIGN_OR_REMOVE_OWNERS);
        getOrCreatePermission(appointerUsername, appointeeUsername, storeId, RoleType.STORE_OWNER);
    }

    // Currently assigns the first store owner as the permission giver of himself
    public void appointFirstStoreOwner(String appointeeUsername, String storeId) {
        getOrCreatePermission(appointeeUsername, appointeeUsername, storeId, RoleType.STORE_OWNER);
    }

    public Permission getOrCreatePermission(String giver, String member, String storeId, RoleType role) {
        Permission permission = permissionRepository.get(storeId, member);
        if (permission == null) {
            permission = new Permission(giver, member);
            PermissionFactory.initPermissionAsRole(permission, role);
            permissionRepository.add(storeId, member, permission);
        }
        return permission;
    }

    public void checkPermission(String userId, String storeId, PermissionType requiredPermission) {
        Permission permission = permissionRepository.get(storeId, userId);
        if (permission == null || !permission.hasPermission(requiredPermission)) {
            throw new SecurityException("User " + userId + " lacks permission " + requiredPermission + " for store " + storeId);
        }
    }

    public void changeManagerPermissions(String ownerUsername, String managerUsername, String storeId, List<PermissionType> newPermissions) {
        checkPermission(ownerUsername, storeId, PermissionType.MODIFY_OWNER_RIGHTS);
        Permission permission = permissionRepository.get(storeId, managerUsername);
        if (permission == null || !permission.isStoreManager()) {
            throw new IllegalStateException(managerUsername + " is not a manager.");
        }
        permission.setPermissions(PermissionType.collectionToSet(newPermissions));
        permissionRepository.update(storeId, managerUsername, permission);
    }
}
