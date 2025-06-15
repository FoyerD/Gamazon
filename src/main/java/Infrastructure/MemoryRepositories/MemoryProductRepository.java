package Infrastructure.MemoryRepositories;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Repos.IProductRepository;
import Domain.Store.Product;

/**
 * In-memory implementation of {@link IProductRepository}.
 * Provides thread-safe CRUD operations on products.
 */
@Repository
@Profile("dev")
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
    @Override
    public Product getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (Product product : products.values()) {
            if (product.getName().equals(name)) {
                return product;
            }
        }
        return null;
    }
    

    /** Retrieves all products. */
    @Override
    public Set<Product> getAll() {
        return new HashSet<>(products.values());
    }

    @Override
    public void deleteAll() {
        products.clear();
        this.deleteAllLocks();
    }
}
