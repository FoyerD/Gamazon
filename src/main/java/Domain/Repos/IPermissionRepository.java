package Domain.Repos;

import Domain.Pair;
import java.util.Map;
import Domain.management.Permission;

public abstract class IPermissionRepository extends ILockbasedRepository<Permission, Pair<String, String>> {

    public IPermissionRepository() {}

    public abstract boolean add(String storeId, String userId, Permission value);

    public abstract Permission update(String storeId, String userId, Permission value);

    public abstract Permission remove(String storeId, String userId);

    public abstract Permission get(String storeId, String userId);

    public abstract Map<String, Permission> getAllPermissionsForStore(String storeId);

    public abstract Map<String, Map<String, Permission>> getAllPermissions();

}
