package Infrastructure.MemoryRepositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Store.Discounts.Discount;
import Domain.Store.Discounts.IDiscountRepository;

@Repository
@Profile("dev")
public class MemoryDiscountRepository extends IDiscountRepository {
    
    private final Map<String, Discount> discounts;

    public MemoryDiscountRepository() {
        this.discounts = new ConcurrentHashMap<>();
    }

    @Override
    public boolean add(String discountID, Discount discount) {
        if (discount == null) {
            throw new IllegalArgumentException("Discount cannot be null");
        }
        if (discount.getId() == null) {
            throw new IllegalArgumentException("Discount ID cannot be null");
        }
        if (discountID == null || discountID.trim().isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        if (discount.getId() != null && !discount.getId().equals(discountID)) {
            throw new IllegalArgumentException("Discount ID does not match the provided discountID");
        }
        
        // Store in main discounts map
        this.addLock(discountID);
        return discounts.put(discountID, discount) == null;
    }

    @Override
    public Discount get(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return discounts.get(id);
    }

    @Override
    public Discount remove(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    
        this.removeLock(id);
        return discounts.remove(id);
    }

    @Override
    public boolean exists(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return get(id) != null;
    }

    @Override
    public void deleteAll() {
        discounts.clear();
    }

    /**
     * Updates a discount for a specific store.
     * 
     * @param id The ID of the store
     * @param discount The discount to update
     * @return The previous discount if it existed, null otherwise
     */
    public Discount update(String id, Discount discount) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (discount == null) {
            throw new IllegalArgumentException("Discount cannot be null");
        }
        if (!id.equals(discount.getId())) {
            throw new IllegalArgumentException("Discount ID does not match the provided ID");
        }
        if(!this.discounts.containsKey(id)) {
            throw new IllegalArgumentException("Discount with this ID does not exist");
        }
        
        return discounts.put(id, discount);
    }


    @Override
    public List<Discount> getStoreDiscounts(String storeId) {
        if(storeId == null || storeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty"); 
        }
        List<Discount> storeDiscounts = this.discounts.values().stream()
                .filter(discount -> storeId.equals(discount.getStoreId()))
                .collect(Collectors.toList());
        return new ArrayList<>(storeDiscounts);
    }


    public int size() {
        return discounts.size();
    }
}