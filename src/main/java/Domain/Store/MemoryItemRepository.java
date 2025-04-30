package Domain.Store;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Domain.Pair;

/**
 * In-memory implementation of {@link IItemRepository}.
 * Uses a thread-safe map to manage items keyed by (storeId, productId) pairs.
 */
public class MemoryItemRepository extends IItemRepository {

    private final Map<Pair<String, String>, Item> items;

    public MemoryItemRepository() {
        this.items = new ConcurrentHashMap<>();
    }

    private boolean isValidId(Pair<String, String> id) {
        return id != null
                && id.getFirst() != null && !id.getFirst().trim().isEmpty()
                && id.getSecond() != null && !id.getSecond().trim().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public Item getItem(String storeId, String productId) {
        if (storeId == null || storeId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
            return null;
        }
        return items.values().stream()
                .filter(item -> item.getStoreId().equals(storeId) && item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public List<Item> getByStoreId(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) {
            return List.of();
        }
        return items.values().stream()
                .filter(item -> item.getStoreId().equals(storeId))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<Item> getByProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return List.of();
        }
        return items.values().stream()
                .filter(item -> item.getProductId().equals(productId))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<Item> getAvailabeItems() {
        return items.values().stream()
                .filter(item -> item.getAmount() > 0)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public Item update(Pair<String, String> id, Item item) {
        if (!isValidId(id) || item == null) {
            return null;
        }
        return items.computeIfPresent(id, (k, v) -> item);
    }

    /** Adds a new item if the ID is valid and doesn't already exist. */
    @Override
    public boolean add(Pair<String, String> id, Item item) {
        if (!isValidId(id) || item == null) {
            return false;
        }
        if (items.containsKey(id)) {
            return false;
        }
        items.put(id, item);
        return true;
    }

    /** Removes the item with the given ID. */
    @Override
    public Item remove(Pair<String, String> id) {
        if (!isValidId(id)) {
            return null;
        }
        return items.remove(id);
    }

    /** {@inheritDoc} */
    @Override
    public Item get(Pair<String, String> id) {
        if (!isValidId(id)) {
            return null;
        }
        return items.get(id);
    }
}
