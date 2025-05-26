package Infrastructure.Repositories;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Conditions.IConditionRepository;

@Repository
public class MemoryConditionRepository implements IConditionRepository {
    
    private final Map<UUID, Condition> conditions;

    public MemoryConditionRepository() {
        this.conditions = new ConcurrentHashMap<>();
    }

    @Override
    public void save(Condition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        if (condition.getId() == null) {
            throw new IllegalArgumentException("Condition ID cannot be null");
        }
        conditions.put(condition.getId(), condition);
    }

    @Override
    public Optional<Condition> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return Optional.ofNullable(conditions.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        conditions.remove(id);
    }

    @Override
    public Map<UUID, Condition> findAll() {
        return new HashMap<>(conditions);
    }

    @Override
    public boolean existsById(UUID id) {
        if (id == null) {
            return false;
        }
        return conditions.containsKey(id);
    }

    @Override
    public void clear() {
        conditions.clear();
    }

    @Override
    public int size() {
        return conditions.size();
    }
}