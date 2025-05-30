package Domain.Repos;

import java.util.List;

import Domain.Pair;
import Domain.Store.Item;

/**
 * Abstract repository interface for managing items.
 * Supports querying by store, product, and availability.
 */
public abstract class IItemRepository extends ILockbasedRepository<Item, Pair<String, String>> {

    /**
     * Retrieves an item by store and product ID.
     */
    public abstract Item getItem(String storeId, String productId);

    /**
     * Returns all items belonging to a specific store.
     */
    public abstract List<Item> getByStoreId(String storeId);

    /**
     * Returns all items corresponding to a given product ID.
     */
    public abstract List<Item> getByProductId(String productId);

    /**
     * Retrieves a list of all items currently in stock.
     */
    public abstract List<Item> getAvailabeItems();

    /**
     * Updates an existing item.
     */
    public abstract Item update(Pair<String, String> id, Item item);
}
