package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the shopping cart repository.
 * Provides thread-safe operations for managing shopping carts.
 */
class ShoppingCartRepository extends IShoppingCartRepository {
    private final Map<String, IShoppingCart> carts;

    /**
     * Constructs a new shopping cart repository.
     * Initializes with a thread-safe concurrent hash map.
     */
    public ShoppingCartRepository() {
        this.carts = new ConcurrentHashMap<>();
    }

    /**
     * Adds a new shopping cart to the repository.
     * 
     * @param id The client ID that uniquely identifies the cart
     * @param value The shopping cart to add
     * @return true if the cart was successfully added, false if a cart with the same ID already exists
     */
    @Override
    public boolean add(String id, IShoppingCart value) {
        if (carts.containsKey(id)) {
            return false;
        }
        carts.put(id, value);
        return true;
    }

    /**
     * Removes a shopping cart from the repository.
     * 
     * @param id The client ID that uniquely identifies the cart
     * @return The removed shopping cart, or null if no cart was found with the given ID
     */
    @Override
    public IShoppingCart remove(String id) {
        if (!carts.containsKey(id)) {
            return null;
        }
        return carts.remove(id); 
    }

    /**
     * Retrieves a shopping cart from the repository.
     * 
     * @param id The client ID that uniquely identifies the cart
     * @return The shopping cart, or null if no cart was found with the given ID
     */
    @Override
    public IShoppingCart get(String id) {
        if (!carts.containsKey(id)) {
            return null; 
        }
        return carts.get(id); 
    }

    /**
     * Updates an existing shopping cart in the repository.
     * 
     * @param id The client ID that uniquely identifies the cart
     * @param value The new shopping cart value
     * @return The updated shopping cart, or null if no cart was found with the given ID
     */
    @Override
    public IShoppingCart update(String id, IShoppingCart value) {
        if (!carts.containsKey(id)) {
            return null;
        }
        carts.put(id, value);
        return value;
    }

    /**
     * Removes all shopping carts from the repository.
     */
    @Override
    public void clear() {
        carts.clear(); 
    }
}