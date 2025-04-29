package Domain.Store;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MemoryProductRepository extends IProductRepository {
    private final ConcurrentMap<String, Product> products;

    public MemoryProductRepository() {
        products = new ConcurrentHashMap<>();
    }

    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    @Override
    public boolean add(String id, Product value) {
        if (!isValidId(id) || value == null) {
            return false;
        }
        if (products.containsKey(id)) {
            return false; // Already exists, do not overwrite
        }
        products.put(id, value);
        return true;
    }

    @Override
    public Product remove(String id) {
        if (!isValidId(id)) {
            return null;
        }
        return products.remove(id);
    }

    @Override
    public Product get(String id) {
        if (!isValidId(id)) {
            return null;
        }
        return products.get(id);
    }

    @Override
    public Product update(String id, Product value) {
        if (!isValidId(id) || value == null) {
            return null;
        }
        if (!products.containsKey(id)) {
            return null; // Can't update non-existing product
        }
        products.put(id, value);
        return value;
    }
}
