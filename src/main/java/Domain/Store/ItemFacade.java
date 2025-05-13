package Domain.Store;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import Domain.Pair;

/**
 * Facade for managing item-related operations, including validation,
 * inventory updates, filtering, and dynamic property assignment.
 */
@Component
public class ItemFacade {
    private final IItemRepository itemRepository;
    private final IProductRepository productRepository;
    private final IStoreRepository storeRepository;

    /**
     * Constructs the facade with required repositories.
     */
    public ItemFacade(IItemRepository itemRepository, IProductRepository productRepository, IStoreRepository storeRepository) {
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    /**
     * Returns the list of items matching the given filter.
     */
    public List<Item> filterItems(ItemFilter itemFilter) {
        List<Item> availableItems = itemRepository.getAvailabeItems();
        return availableItems.stream().filter(itemFilter::matchesFilter).toList();
    }

    /**
     * Returns all items associated with the given product ID.
     */
    public List<Item> getItemsProductId(String productId) {
        return itemRepository.getByProductId(productId);
    }

    /**
     * Retrieves a single item by store and product ID after validating existence.
     */
    public Item getItem(String storeId, String productId) {
        validateStoreAndProductExist(storeId, productId);
        Item item = itemRepository.getItem(storeId, productId);
        if (item == null)
            throw new NoSuchElementException("Item not found for storeId: " + storeId + ", productId: " + productId);
        return item;
    }

    /**
     * Returns all items from a given store after checking the store exists.
     */
    public List<Item> getItemsByStoreId(String storeId) {
        if (storeRepository.get(storeId) == null)
            throw new NoSuchElementException("Store not found for storeId: " + storeId);
        return itemRepository.getByStoreId(storeId);
    }

    /**
     * Returns all available (non-zero stock) items.
     */
    public List<Item> getAvailableItems() {
        return itemRepository.getAvailabeItems();
    }

    /**
     * Updates an item in the repository after validation.
     */
    public void update(Pair<String, String> id, Item item) {
        validateStoreAndProductExist(id.getFirst(), id.getSecond());
        itemRepository.update(id, item);
    }

    /**
     * Adds a new item to the repository and sets its name and category fetchers.
     * Returns false if the item already exists.
     */
    public boolean add(Pair<String, String> id, Item item) {
        validateStoreAndProductExist(id.getFirst(), id.getSecond());

        if (itemRepository.get(id) != null) {
            return false;
        }

        String itemStoreId = item.getStoreId();
        String itemProductId = item.getProductId();

        item.setNameFetcher(() ->
            itemRepository.getByProductId(itemProductId).stream()
                .filter(i -> !(i.getStoreId().equals(itemStoreId) && i.getProductId().equals(itemProductId)))
                .findFirst()
                .map(Item::getProductName)
                .orElse("Unknown Product")
        );

        item.setCategoryFetcher(() ->
            itemRepository.getByProductId(itemProductId).stream()
                .filter(i -> !(i.getStoreId().equals(itemStoreId) && i.getProductId().equals(itemProductId)))
                .flatMap(i -> i.getCategories().stream())
                .collect(Collectors.toSet())
        );

        return itemRepository.add(id, item);
    }

    /**
     * Removes an item from the repository after validating it exists.
     */
    public Item remove(Pair<String, String> id) {
        validateStoreAndProductExist(id.getFirst(), id.getSecond());
        Item item = itemRepository.remove(id);
        if (item == null)
            throw new NoSuchElementException("No item with id: " + id + " exists.");
        return item;
    }

    /**
     * Increases the stock amount of the specified item.
     */
    public void increaseAmount(Pair<String, String> id, int amount) {
        validateStoreAndProductExist(id.getFirst(), id.getSecond());
    
        Item item = itemRepository.get(id);
        if (item == null) {
            throw new NoSuchElementException("Item not found for: " + id);
        }
    
        Object lock = getOrCreateLockSafe(id);
        synchronized (lock) {
            item.increaseAmount(amount);
        }
    }

    /**
     * Decreases the stock amount of the specified item.
     */
    public void decreaseAmount(Pair<String, String> id, int amount) {
        validateStoreAndProductExist(id.getFirst(), id.getSecond());
    
        Item item = itemRepository.get(id);
        if (item == null) {
            throw new NoSuchElementException("Item not found for: " + id);
        }
    
        Object lock = getOrCreateLockSafe(id);
        synchronized (lock) {
            item.decreaseAmount(amount);
        }
    }

    /**
     * Validates the existence of both store and product before any item operation.
     */
    private void validateStoreAndProductExist(String storeId, String productId) {
        if (storeRepository.get(storeId) == null) {
            throw new NoSuchElementException("Store not found for storeId: " + storeId);
        }
        if (productRepository.get(productId) == null) {
            throw new NoSuchElementException("Product not found for productId: " + productId);
        }
    }

    private Object getOrCreateLockSafe(Pair<String, String> id) {
        Object lock = itemRepository.getLock(id);
        if (lock == null) {
            // Trigger a side-effect that will create the lock safely.
            // For example, call `add(...)` again with a dummy item if you know it's already added,
            // or assume all existing items already have a lock because the repo does `addLock` on `add`.
            throw new IllegalStateException("Lock not found for id: " + id + ". Ensure all added items create locks.");
        }
        return lock;
    }
    
}
