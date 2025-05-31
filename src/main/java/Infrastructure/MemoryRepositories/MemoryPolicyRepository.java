package Infrastructure.MemoryRepositories;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import Domain.Repos.IPolicyRepository;
import Domain.Store.Policy;

import java.util.UUID;

/**
 * In-memory implementation of IPolicyRepository for Policy objects.
 */
@Repository
public class MemoryPolicyRepository extends IPolicyRepository {
    // Thread-safe storage of policies by ID
    private final Map<String, Policy> policies = new ConcurrentHashMap<>();

    /**
     * Return all policies belonging to the given store.
     */
    @Override
    public List<Policy> getAllStorePolicies(String storeId) {
        return policies.values().stream()
                .filter(p -> storeId.equals(p.getStoreId()))
                .collect(Collectors.toList());
    }

    /**
     * Add a new policy under the given ID.
     * @return true if added, false if ID already existed
     */
    @Override
    public boolean add(String id, Policy policy) {
        if (id == null || id.trim().isEmpty() || policy == null) {
            return false;
        }
        return policies.putIfAbsent(id, policy) == null;
    }

    /**
     * Remove and return the policy with the given ID, or null if none.
     */
    @Override
    public Policy remove(String id) {
        return policies.remove(id);
    }

    /**
     * Lookup a policy by its ID.
     */
    @Override
    public Policy get(String id) {
        return policies.get(id);
    }

    /**
     * Replace the policy at the given ID if it exists.
     * @return the new policy if updated, or null if no existing entry
     */
    @Override
    public Policy update(String id, Policy policy) {
        if (!policies.containsKey(id)) {
            return null;
        }
        policies.put(id, policy);
        return policy;
    }

    @Override
    public void deleteAll() {
        policies.clear();
        this.deleteAllLocks();
    }
}
