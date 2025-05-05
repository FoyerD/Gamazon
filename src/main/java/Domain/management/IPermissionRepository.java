package Domain.management;

import java.util.Map;

public abstract class IPermissionRepository {

    public IPermissionRepository() {}

    public abstract boolean add(String storeId, String username, Permission value);

    public abstract Permission update(String storeId, String username, Permission value);

    public abstract Permission remove(String storeId, String username);

    public abstract Permission get(String storeId, String username);

    public abstract Map<String, Permission> getAllPermissionsForStore(String storeId);

    public abstract Map<String, Map<String, Permission>> getAllPermissions();;
}
