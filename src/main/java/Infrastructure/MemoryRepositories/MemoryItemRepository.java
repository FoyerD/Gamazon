package Infrastructure.MemoryRepositories;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Pair;
import Domain.Repos.IItemRepository;
import Domain.Store.Item;

/**
 * In-memory implementation of {@link IItemRepository}.
 * Stores items in a thread-safe map keyed by (storeId, productId) pairs.
 */
@Repository
@Profile("dev")
public class MemoryItemRepository extends IItemRepository {

    private final Map<Pair<String, String>, Item> items;

    /**
     * Constructs a new, empty memory-based item repository.
     */
    public MemoryItemRepository() {
        this.items = new ConcurrentHashMap<>();
    }

    /**
     * Validates that the given ID pair is non-null and contains non-empty values.
     */
    private boolean isValidId(Pair<String, String> id) {
        return id != null
                && id.getFirst() != null && !id.getFirst().trim().isEmpty()
                && id.getSecond() != null && !id.getSecond().trim().isEmpty();
    }

    /**
     * Retrieves an item by store ID and product ID.
     * 
     * @param storeId   the store's ID
     * @param productId the product's ID
     * @return the corresponding item, or null if not found
     */
    @Override
    public Item getItem(String storeId, String productId) {
        if (storeId == null || productId == null) return null;
        return items.get(new Pair<>(storeId, productId));
    }

    /**
     * Retrieves all items associated with the given store ID.
     * 
     * @param storeId the store's ID
     * @return a list of items belonging to the store
     */
    @Override
    public List<Item> getByStoreId(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) return List.of();
        return items.values().stream()
                .filter(item -> item.getStoreId().equals(storeId))
                .toList();
    }

    /**
     * Retrieves all items associated with the given product ID.
     * 
     * @param productId the product's ID
     * @return a list of items with the specified product ID
     */
    @Override
    public List<Item> getByProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) return List.of();
        return items.values().stream()
                .filter(item -> item.getProductId().equals(productId))
                .toList();
    }

    /**
     * Retrieves all available (non-zero quantity) items.
     * 
     * @return a list of available items
     */
    @Override
    public List<Item> getAvailabeItems() {
        return items.values().stream()
                .filter(item -> item.getAmount() > 0)
                .toList();
    }

    /**
     * Updates the item associated with the given ID.
     * 
     * @param id   the item's ID (storeId, productId)
     * @param item the new item to replace the existing one
     * @return the updated item, or null if no item was present
     */
    @Override
    public Item update(Pair<String, String> id, Item item) {
        if (!isValidId(id) || item == null) return null;
        Object lock = getOrCreateLock(id);
        synchronized (lock) {
            return items.computeIfPresent(id, (k, v) -> item);
        }
    }

    /**
     * Adds a new item to the repository.
     * 
     * @param id   the item's ID (storeId, productId)
     * @param item the item to add
     * @return true if the item was added, false if it already exists or is invalid
     */
    @Override
    public boolean add(Pair<String, String> id, Item item) {
        if (!isValidId(id) || item == null) return false;
        Object lock = getOrCreateLock(id);
        synchronized (lock) {
            if (items.containsKey(id)) return false;
            items.put(id, item);
            return true;
        }
    }

    /**
     * Removes the item associated with the given ID.
     * 
     * @param id the item's ID
     * @return the removed item, or null if it did not exist
     */
    @Override
    public Item remove(Pair<String, String> id) {
        if (!isValidId(id)) return null;
        Object lock = getOrCreateLock(id);
        synchronized (lock) {
            Item removed = items.remove(id);
            removeLock(id);
            return removed;
        }
    }

    /**
     * Increases the quantity of the specified item.
     * 
     * @param id     the item's ID
     * @param amount the amount to increase
     */
    public void increaseAmount(Pair<String, String> id, int amount) {
        if (!isValidId(id)) return;
        Object lock = getOrCreateLock(id);
        synchronized (lock) {
            Item item = items.get(id);
            if (item != null) item.increaseAmount(amount);
        }
    }

    /**
     * Decreases the quantity of the specified item.
     * 
     * @param id     the item's ID
     * @param amount the amount to decrease
     */
    public void decreaseAmount(Pair<String, String> id, int amount) {
        if (!isValidId(id)) return;
        Object lock = getOrCreateLock(id);
        synchronized (lock) {
            Item item = items.get(id);
            if (item != null) item.decreaseAmount(amount);
        }
    }

    /**
     * Retrieves the item associated with the given ID.
     * 
     * @param id the item's ID
     * @return the item, or null if not found or invalid ID
     */
    @Override
    public Item get(Pair<String, String> id) {
        if (!isValidId(id)) return null;
        return items.get(id);
    }

    /**
     * Ensures a lock exists for the given ID, creating one if necessary.
     * 
     * @param id the ID to get or create a lock for
     * @return the lock object
     */
    private Object getOrCreateLock(Pair<String, String> id) {
        if (getLock(id) == null) {
            addLock(id); // safe no-op if already exists
        }
        return getLock(id);
    }

    @Override
    public void deleteAll() {
        items.clear();
        deleteAllLocks();
    }
}
