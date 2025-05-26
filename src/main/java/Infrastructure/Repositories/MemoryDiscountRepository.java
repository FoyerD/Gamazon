package Infrastructure.Repositories;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import Domain.Store.Discounts.Discount;
import Domain.Store.Discounts.IDiscountRepository;

@Repository
public class MemoryDiscountRepository implements IDiscountRepository {
    
    private final Map<UUID, Discount> discounts;

    public MemoryDiscountRepository() {
        this.discounts = new ConcurrentHashMap<>();
    }

    @Override
    public void save(Discount discount) {
        if (discount == null) {
            throw new IllegalArgumentException("Discount cannot be null");
        }
        if (discount.getId() == null) {
            throw new IllegalArgumentException("Discount ID cannot be null");
        }
        discounts.put(discount.getId(), discount);
    }

    @Override
    public Optional<Discount> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return Optional.ofNullable(discounts.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        discounts.remove(id);
    }

    @Override
    public Map<UUID, Discount> findAll() {
        return new HashMap<>(discounts);
    }

    @Override
    public boolean existsById(UUID id) {
        if (id == null) {
            return false;
        }
        return discounts.containsKey(id);
    }

    @Override
    public void clear() {
        discounts.clear();
    }

    @Override
    public int size() {
        return discounts.size();
    }
}