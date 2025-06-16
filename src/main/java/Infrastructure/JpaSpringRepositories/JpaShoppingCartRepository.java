package Infrastructure.JpaSpringRepositories;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Repos.IShoppingCartRepository;
import Domain.Shopping.IShoppingCart;
import Domain.Shopping.ShoppingCart;

@Repository
@Profile({"prod", "dbtest"})
public class JpaShoppingCartRepository extends IShoppingCartRepository {

    private final IJpaShoppingCartRepository jpaShoppingCartRepository;

    public JpaShoppingCartRepository(IJpaShoppingCartRepository jpaShoppingCartRepository) {
        this.jpaShoppingCartRepository = jpaShoppingCartRepository;
    }

    @Override
    public boolean add(String id, IShoppingCart value) {
        if (!(value instanceof ShoppingCart)) {
            throw new IllegalArgumentException("Value must be an instance of ShoppingCart");
        }
        if (jpaShoppingCartRepository.existsById(id)) {
            return false;
        }
        // Create a lock for the cart before saving
        addLock(id);
        jpaShoppingCartRepository.save((ShoppingCart) value);
        return true;
    }

    @Override
    public IShoppingCart remove(String id) {
        ShoppingCart existing = jpaShoppingCartRepository.findById(id).orElse(null);
        if (existing != null) {
            jpaShoppingCartRepository.deleteById(id);
            removeLock(id);
        }
        return existing;
    }

    @Override
    public IShoppingCart get(String id) {
        return jpaShoppingCartRepository.findById(id).orElse(null);
    }

    @Override
    public IShoppingCart update(String id, IShoppingCart cart) {
        if (!(cart instanceof ShoppingCart)) {
            throw new IllegalArgumentException("Cart must be an instance of ShoppingCart");
        }
        
        try {
            // First check if the cart exists
            if (!jpaShoppingCartRepository.existsById(id)) {
                return null;
            }

            // Ensure we have a lock
            if (getLock(id) == null) {
                addLock(id);
            }

            // Save the cart and return the result
            return jpaShoppingCartRepository.save((ShoppingCart) cart);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error updating shopping cart: " + e.getMessage());
            // Remove the lock in case of failure
            removeLock(id);
            return null;
        }
    }

    @Override
    public void clear() {
        jpaShoppingCartRepository.deleteAll();
        deleteAllLocks();
    }

    @Override
    public Map<String, IShoppingCart> getAll() {
        return jpaShoppingCartRepository.getAllCarts().stream()
            .collect(Collectors.toMap(
                ShoppingCart::getClientId,
                cart -> cart
            ));
    }

    @Override
    public void deleteAll() {
        jpaShoppingCartRepository.deleteAll();
        deleteAllLocks();
    }
} 