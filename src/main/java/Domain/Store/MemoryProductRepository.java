package Domain.Store;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory implementation of {@link IProductRepository}.
 * Provides thread-safe CRUD operations on products.
 */
public class MemoryProductRepository extends IProductRepository {
    private final ConcurrentMap<String, Product> products;

    public MemoryProductRepository() {
        products = new ConcurrentHashMap<>();
    }

    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    /** Adds a new product if it doesn't already exist. */
    @Override
    public boolean add(String id, Product value) {
        if (!isValidId(id) || value == null) {
            return false;
        }
        if (products.containsKey(id)) {
            return false;
        }
        products.put(id, value);
        return true;
    }

    /** Removes a product by its ID. */
    @Override
    public Product remove(String id) {
        if (!isValidId(id)) {
            return null;
        }
        return products.remove(id);
    }

    /** Retrieves a product by its ID. */
    @Override
    public Product get(String id) {
        if (!isValidId(id)) {
            return null;
        }
        return products.get(id);
    }

    /** Updates an existing product. */
    @Override
    public Product update(String id, Product value) {
        if (!isValidId(id) || value == null || !products.containsKey(id)) {
            return null;
        }
        products.put(id, value);
        return value;
    }
}
