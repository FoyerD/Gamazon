package Domain.management;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Domain.User.Member;

@Component
public class PermissionManager {
    private IPermissionRepository permissionRepository;

    public PermissionManager() {
        this.permissionRepository = null;
    }
    @Autowired
    public PermissionManager(IPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public void setPermissionRepository(IPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public void appointStoreManager(String appointerId, String appointeeId, String storeId) {
        checkPermission(appointerId, storeId, PermissionType.SUPERVISE_MANAGERS);
        getOrCreatePermission(appointerId, appointeeId, storeId, RoleType.STORE_MANAGER);
    }

    public void removeStoreManager(String removerUsername, String managerUsername, String storeId) {
        checkPermission(removerUsername, storeId, PermissionType.SUPERVISE_MANAGERS);
        Permission permission = permissionRepository.get(storeId, managerUsername);
        if (permission == null || !(permission.isStoreManager())) {
            throw new IllegalStateException(managerUsername + " is not a manager.");
        }
        permission.setPermissions(Set.of());
        permission.setRole(null);
        permissionRepository.update(storeId, managerUsername, permission);
    }

    public Map<String, Map<String, Permission>> getAllStorePermissions(){
        return permissionRepository.getAllPermissions();
    }

    public void appointStoreOwner(String appointerId, String appointeeId, String storeId) {
        checkPermission(appointerId, storeId, PermissionType.ASSIGN_OR_REMOVE_OWNERS);
        getOrCreatePermission(appointerId, appointeeId, storeId, RoleType.STORE_OWNER);
    }

    // Currently assigns the first store owner as the permission giver of himself
    public void appointFirstStoreOwner(String appointeeId, String storeId) {
        getOrCreatePermission(appointeeId, appointeeId, storeId, RoleType.STORE_FOUNDER);
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

    public Permission getPermission(String storeId, String userId) {
        return permissionRepository.get(storeId, userId);
    }

    public void checkPermission(String userId, String storeId, PermissionType requiredPermission) {
        Permission permission = permissionRepository.get(storeId, userId);
        if (permission == null || !permission.hasPermission(requiredPermission)) {
            throw new SecurityException("User lacks permission " + requiredPermission + " for store " + storeId);
        }
    }

    public void changeManagerPermissions(String ownerUsername, String managerUsername, String storeId, List<PermissionType> newPermissions) {
        checkPermission(ownerUsername, storeId, PermissionType.MODIFY_OWNER_RIGHTS);
        Permission permission = permissionRepository.get(storeId, managerUsername);
        if (permission == null || !(permission.isStoreManager())) {
            throw new IllegalStateException(managerUsername + " is not a manager.");
        }
        permission.setPermissions(PermissionType.collectionToSet(newPermissions));
        permissionRepository.update(storeId, managerUsername, permission);
    }

    public Map<String, Permission> getStorePermissions(String storeId) {
        return permissionRepository.getAllPermissionsForStore(storeId);
    }

    public void removeAllPermissions(String storeId, String userId) {
        Permission permission = permissionRepository.get(storeId, userId);
        if (permission == null) {
            throw new IllegalStateException(userId + " does not have any permissions.");
        }
        permission.setPermissions(Set.of());
        permission.setRole(null);
        permissionRepository.update(storeId, userId, permission);
    }

    public Map<String, Permission> getAllPermissionsForStore(String storeId) {
        return permissionRepository.getAllPermissionsForStore(storeId);
    }

    public void addMarketManager(Member manager){
        Permission founder = new Permission("system", manager.getId());
        PermissionFactory.initPermissionAsRole(founder, RoleType.TRADING_MANAGER);
        permissionRepository.add("1", manager.getId(), founder);
    }

    public boolean banUser(String bannerId, String userId, Date endDate) {
        checkPermission(bannerId, "1", PermissionType.BAN_USERS);

        Permission perm = getOrCreatePermission(bannerId, userId, "1", RoleType.BANNED_USER);
        return perm.hasPermission(PermissionType.BAN_USERS);
    }

    public boolean unbanUser(String unbannerId, String userId) {
        checkPermission(unbannerId, "1", PermissionType.BAN_USERS);

        Permission perm = permissionRepository.get("1", userId);
        if (perm == null || !perm.hasPermission(PermissionType.BAN_USERS)) {
            throw new IllegalStateException(userId + " is not banned.");
        }
        perm.setPermissions(Set.of());
        perm.setRole(null);
        permissionRepository.update("1", userId, perm);
        return true;
    }
}
