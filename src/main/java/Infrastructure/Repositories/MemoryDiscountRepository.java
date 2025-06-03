package Infrastructure.Repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import Domain.Store.Discounts.Discount;
import Domain.Store.Discounts.IDiscountRepository;

@Repository
public class MemoryDiscountRepository implements IDiscountRepository {
    
    private final Map<String, Discount> discounts;
    private final Map<String, Map<String, Discount>> discountsByStore;

    public MemoryDiscountRepository() {
        this.discounts = new ConcurrentHashMap<>();
        this.discountsByStore = new ConcurrentHashMap<>();
    }

    @Override
    public boolean save(String storeID, Discount discount) {
        if (discount == null) {
            throw new IllegalArgumentException("Discount cannot be null");
        }
        if (discount.getId() == null) {
            throw new IllegalArgumentException("Discount ID cannot be null");
        }
        if (storeID == null || storeID.trim().isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        
        // Store in main discounts map
        discounts.put(discount.getId(), discount);
        
        // Store in store-specific map
        discountsByStore.computeIfAbsent(storeID, k -> new ConcurrentHashMap<>())
                       .put(discount.getId(), discount);
        return true;
    }

    @Override
    public Discount get(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return discounts.get(id);
    }

    @Override
    public void delete(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        Discount discount = discounts.remove(id);
        if (discount != null) {
            // Remove from all store maps
            discountsByStore.values().forEach(storeDiscounts -> 
                storeDiscounts.remove(id));
        }
    }

    @Override
    public boolean exists(String id) {
        if (id == null) {
            return false;
        }
        return discounts.containsKey(id);
    }

    @Override
    public void clear() {
        discounts.clear();
        discountsByStore.clear();
    }


    @Override
    public List<Discount> getStoreDiscounts(String storeId) {
        if(storeId == null || storeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty"); 
        }
        Map<String, Discount> storeDiscounts = discountsByStore.get(storeId);
        if (storeDiscounts == null) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(storeDiscounts.values());
    }


    /**
     * Deletes all discounts for a specific store.
     * 
     * @param storeId The ID of the store
     * @return The number of discounts deleted
     */
    public int deleteByStoreId(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) {
            return 0;
        }
        
        Map<String, Discount> storeDiscounts = discountsByStore.put(storeId, new HashMap<>());
        if (storeDiscounts == null) {
            return 0;
        }
        
        // Remove from main discounts map
        storeDiscounts.keySet().forEach(discounts::remove);
        
        return storeDiscounts.size();
    }

    /**
     * Gets the number of discounts for a specific store.
     * 
     * @param storeId The ID of the store
     * @return The number of discounts for the store
     */
    public int getStoreDiscountCount(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) {
            return 0;
        }
        
        Map<String, Discount> storeDiscounts = discountsByStore.get(storeId);
        return storeDiscounts != null ? storeDiscounts.size() : 0;
    }

    /**
     * Checks if a store has any discounts.
     * 
     * @param storeId The ID of the store
     * @return true if the store has discounts, false otherwise
     */
    public boolean hasDiscountsForStore(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) {
            return false;
        }
        
        Map<String, Discount> storeDiscounts = discountsByStore.get(storeId);
        return storeDiscounts != null && !storeDiscounts.isEmpty();
    }

    /**
     * Gets all store IDs that have discounts.
     * 
     * @return Map of store IDs to the number of discounts they have
     */
    public Map<String, Integer> getStoreDiscountCounts() {
        return discountsByStore.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().size()
                ));
    }

    /**
     * Updates a discount for a specific store.
     * 
     * @param storeId The ID of the store
     * @param discount The discount to update
     * @return The previous discount if it existed, null otherwise
     */
    public Discount updateDiscount(String storeId, Discount discount) {
        if (discount == null) {
            throw new IllegalArgumentException("Discount cannot be null");
        }
        if (discount.getId() == null) {
            throw new IllegalArgumentException("Discount ID cannot be null");
        }
        if (storeId == null || storeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }

        Discount previousDiscount = discounts.put(discount.getId(), discount);
        discountsByStore.computeIfAbsent(storeId, k -> new ConcurrentHashMap<>())
                       .put(discount.getId(), discount);
        
        return previousDiscount;
    }

    /**
     * Finds a discount by ID within a specific store.
     * 
     * @param storeId The ID of the store
     * @param discountId The ID of the discount
     * @return The discount if found within the store, null otherwise
     */
    public Discount findByStoreAndDiscountId(String storeId, String discountId) {
        if (storeId == null || storeId.trim().isEmpty() || discountId == null) {
            return null;
        }
        
        Map<String, Discount> storeDiscounts = discountsByStore.get(storeId);
        if (storeDiscounts == null) {
            return null;
        }
        
        return storeDiscounts.get(discountId);
    }

    public int size() {
        return discounts.size();
    }
}