package Infrastructure.Repositories;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Conditions.IConditionRepository;

@Repository
public class MemoryConditionRepository implements IConditionRepository {
    
    private final Map<String, Condition> conditions;
    private final Map<String, Map<String, Condition>> conditionsByStore;

    public MemoryConditionRepository() {
        this.conditions = new ConcurrentHashMap<>();
        this.conditionsByStore = new ConcurrentHashMap<>();
    }

    @Override
    public boolean save(String storeID, Condition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        if (condition.getId() == null) {
            throw new IllegalArgumentException("Condition ID cannot be null");
        }
        if (storeID == null || storeID.trim().isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        // Store in main conditions map
        conditions.put(condition.getId(), condition);
        
        // Store in store-specific map
        conditionsByStore.computeIfAbsent(storeID, k -> new ConcurrentHashMap<>())
                         .put(condition.getId(), condition);
        return true;
    }

    @Override
    public Condition get(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return conditions.get(id);
    }

    @Override
    public void delete(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        Condition condition = conditions.remove(id);
        if (condition != null) {
            // Remove from all store maps
            conditionsByStore.values().forEach(storeConditions -> 
                storeConditions.remove(id));
        }
    }

    @Override
    public boolean exists(String id) {
        if (id == null) {
            return false;
        }
        return conditions.containsKey(id);
    }

    @Override
    public void clear() {
        conditions.clear();
        conditionsByStore.clear();
    }

    @Override
    public int size() {
        return conditions.size();
    }

    /**
     * Finds all conditions for a specific store.
     * 
     * @param storeID The ID of the store
     * @return Map of condition IDs to conditions for the specified store
     */
    public Map<String, Condition> findByStoreId(String storeID) {
        if (storeID == null || storeID.trim().isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Condition> storeConditions = conditionsByStore.get(storeID);
        return storeConditions != null ? new HashMap<>(storeConditions) : new HashMap<>();
    }

    /**
     * Deletes all conditions for a specific store.
     * 
     * @param storeID The ID of the store
     * @return The number of conditions deleted
     */
    public int deleteByStoreId(String storeID) {
        if (storeID == null || storeID.trim().isEmpty()) {
            return 0;
        }
        
        Map<String, Condition> storeConditions = conditionsByStore.remove(storeID);
        if (storeConditions == null) {
            return 0;
        }
        
        // Remove from main conditions map
        storeConditions.keySet().forEach(conditions::remove);
        
        return storeConditions.size();
    }

    /**
     * Gets the number of conditions for a specific store.
     * 
     * @param storeID The ID of the store
     * @return The number of conditions for the store
     */
    public int getStoreConditionCount(String storeID) {
        if (storeID == null || storeID.trim().isEmpty()) {
            return 0;
        }
        
        Map<String, Condition> storeConditions = conditionsByStore.get(storeID);
        return storeConditions != null ? storeConditions.size() : 0;
    }

    /**
     * Checks if a store has any conditions.
     * 
     * @param storeID The ID of the store
     * @return true if the store has conditions, false otherwise
     */
    public boolean hasConditionsForStore(String storeID) {
        if (storeID == null || storeID.trim().isEmpty()) {
            return false;
        }
        
        Map<String, Condition> storeConditions = conditionsByStore.get(storeID);
        return storeConditions != null && !storeConditions.isEmpty();
    }

    /**
     * Gets all store IDs that have conditions.
     * 
     * @return Map of store IDs to the number of conditions they have
     */
    public Map<String, Integer> getStoreConditionCounts() {
        return conditionsByStore.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().size()
                ));
    }

    @Override
    public Set<Condition> getStoreConditions(String storeId) {
        Map<String, Condition> storeConditions = conditionsByStore.get(storeId);
        return storeConditions != null ? Set.copyOf(storeConditions.values()) : Set.of();
    }
}