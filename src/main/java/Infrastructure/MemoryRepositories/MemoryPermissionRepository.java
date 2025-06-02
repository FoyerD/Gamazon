package Infrastructure.MemoryRepositories;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Pair;
import Domain.Repos.IPermissionRepository;
import Domain.management.Permission;

@Repository
@Profile("dev")
public class MemoryPermissionRepository extends IPermissionRepository {

    private final Map<String, Map<String, Permission>> permissions = new ConcurrentHashMap<>();

    @Override
    public boolean add(String storeId, String userId, Permission value) {
        permissions.putIfAbsent(storeId, new HashMap<>());
        permissions.get(storeId).put(userId, value);
        return true;
    }

    @Override
    public Permission update(String storeId, String userId, Permission value) {
        Map<String, Permission> storeMap = permissions.get(storeId);
        if (storeMap == null || !storeMap.containsKey(userId)) return null;
        storeMap.put(userId, value);
        return value;
    }

    @Override
    public Permission remove(String storeId, String userId) {
        Map<String, Permission> storeMap = permissions.get(storeId);
        if (storeMap == null) return null;
        return storeMap.remove(userId);
    }

    @Override
    public Permission get(String storeId, String userId) {
        Map<String, Permission> storeMap = permissions.get(storeId);
        return storeMap == null ? null : storeMap.get(userId);
    }

    public Map<String, Permission> getAllPermissionsForStore(String storeId) {
        return permissions.getOrDefault(storeId, new ConcurrentHashMap<>());
    }

    public Map<String, Map<String, Permission>> getAllPermissions() {
        return permissions;
    }

    @Override
    public void deleteAll() {
        permissions.clear();
        this.deleteAllLocks();
    }

    @Override
    public boolean add(Pair<String, String> id, Permission value) {
        if (id == null || id.getFirst() == null || id.getSecond() == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return add(id.getFirst(), id.getSecond(), value);
    }

    @Override
    public Permission remove(Pair<String, String> id) {
        if (id == null || id.getFirst() == null || id.getSecond() == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return remove(id.getFirst(), id.getSecond());
    }

    @Override
    public Permission get(Pair<String, String> id) {
        if (id == null || id.getFirst() == null || id.getSecond() == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return get(id.getFirst(), id.getSecond());
    }

    @Override
    public Permission update(Pair<String, String> id, Permission value) {
        if (id == null || id.getFirst() == null || id.getSecond() == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return update(id.getFirst(), id.getSecond(), value);
    }
}
