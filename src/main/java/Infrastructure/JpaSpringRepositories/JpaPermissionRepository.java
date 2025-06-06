package Infrastructure.JpaSpringRepositories;

import Domain.Pair;
import Domain.Repos.IPermissionRepository;
import Domain.management.Permission;
import Domain.management.PermissionId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Profile("prod")
public class JpaPermissionRepository extends IPermissionRepository {

    private final IJpaPermissionRepository jpaRepository;

    public JpaPermissionRepository(IJpaPermissionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean add(String storeId, String userId, Permission value) {
        PermissionId id = new PermissionId(storeId, userId);
        if (jpaRepository.existsById(id)) return false;

        addLock(new Pair<>(storeId, userId)); // Locking logic
        value.setStoreId(storeId);
        value.setMember(userId);
        jpaRepository.save(value);
        return true;
    }

    @Override
    public Permission update(String storeId, String userId, Permission value) {
        PermissionId id = new PermissionId(storeId, userId);
        if (!jpaRepository.existsById(id)) return null;

        if (getLock(new Pair<>(storeId, userId)) == null) {
            addLock(new Pair<>(storeId, userId));
        }

        value.setStoreId(storeId);
        value.setMember(userId);
        return jpaRepository.save(value);
    }

    @Override
    public Permission remove(String storeId, String userId) {
        PermissionId id = new PermissionId(storeId, userId);
        Permission permission = jpaRepository.findById(id).orElse(null);
        if (permission != null) {
            jpaRepository.deleteById(id);
            removeLock(new Pair<>(storeId, userId));
        }
        return permission;
    }

    @Override
    public Permission get(String storeId, String userId) {
        return jpaRepository.findById(new PermissionId(storeId, userId)).orElse(null);
    }

    public Map<String, Permission> getAllPermissionsForStore(String storeId) {
        List<Permission> list = jpaRepository.findAllByStoreId(storeId);
        Map<String, Permission> result = new HashMap<>();
        for (Permission p : list) {
            result.put(p.getMember(), p);
        }
        return result;
    }

    public Map<String, Map<String, Permission>> getAllPermissions() {
        Map<String, Map<String, Permission>> result = new HashMap<>();
        for (Permission p : jpaRepository.findAll()) {
            String storeId = p.getStoreId();
            String userId = p.getMember();
            result.putIfAbsent(storeId, new HashMap<>());
            result.get(storeId).put(userId, p);
        }
        return result;
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
        deleteAllLocks();
    }


    @Override
    public boolean add(Pair<String, String> id, Permission value) {
        return add(id.getFirst(), id.getSecond(), value);
    }

    @Override
    public Permission remove(Pair<String, String> id) {
        return remove(id.getFirst(), id.getSecond());
    }

    @Override
    public Permission get(Pair<String, String> id) {
        return get(id.getFirst(), id.getSecond());
    }

    @Override
    public Permission update(Pair<String, String> id, Permission value) {
        return update(id.getFirst(), id.getSecond(), value);
    }
}
