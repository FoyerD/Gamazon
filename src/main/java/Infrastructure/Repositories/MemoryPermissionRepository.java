package Infrastructure.Repositories;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Domain.management.IPermissionRepository;
import Domain.management.Permission;

public class MemoryPermissionRepository extends IPermissionRepository {

    private final Map<String, Map<String, Permission>> permissions = new ConcurrentHashMap<>();

    @Override
    public boolean add(String storeId, String username, Permission value) {
        permissions.putIfAbsent(storeId, new HashMap<>());
        permissions.get(storeId).put(username, value);
        return true;
    }

    @Override
    public Permission update(String storeId, String username, Permission value) {
        Map<String, Permission> storeMap = permissions.get(storeId);
        if (storeMap == null || !storeMap.containsKey(username)) return null;
        storeMap.put(username, value);
        return value;
    }

    @Override
    public Permission remove(String storeId, String username) {
        Map<String, Permission> storeMap = permissions.get(storeId);
        if (storeMap == null) return null;
        return storeMap.remove(username);
    }

    @Override
    public Permission get(String storeId, String username) {
        Map<String, Permission> storeMap = permissions.get(storeId);
        return storeMap == null ? null : storeMap.get(username);
    }

    public Map<String, Permission> getAllPermissionsForStore(String storeId) {
        return permissions.getOrDefault(storeId, new ConcurrentHashMap<>());
    }

    public Map<String, Map<String, Permission>> getAllPermissions() {
        return permissions;
    }
}
