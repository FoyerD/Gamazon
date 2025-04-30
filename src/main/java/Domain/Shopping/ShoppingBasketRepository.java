package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Domain.Pair;

/**
 * Implementation of the shopping basket repository.
 * Provides thread-safe operations for managing shopping baskets.
 */
public class ShoppingBasketRepository extends IShoppingBasketRepository {
    
    private Map<Pair<String, String>, ShoppingBasket> baskets;

    /**
     * Constructs a new shopping basket repository.
     * Initializes with a thread-safe concurrent hash map.
     */
    public ShoppingBasketRepository() {
        this.baskets = new ConcurrentHashMap<>();
    }

    /**
     * Removes all shopping baskets from the repository.
     */
    @Override
    public void clear() {
        baskets.clear();
    }

    /**
     * Adds a new shopping basket to the repository.
     * 
     * @param id The pair of client ID and store ID that uniquely identifies the basket
     * @param value The shopping basket to add
     * @return true if the basket was successfully added, false if a basket with the same ID already exists
     */
    @Override
    public boolean add(Pair<String, String> id, ShoppingBasket value) {
        if (baskets.containsKey(id)) {
            return false;
        }
        baskets.put(id, value);
        return true;
    }

    /**
     * Removes a shopping basket from the repository.
     * 
     * @param id The pair of client ID and store ID that uniquely identifies the basket
     * @return The removed shopping basket, or null if no basket was found with the given ID
     */
    @Override
    public ShoppingBasket remove(Pair<String, String> id) {
        if (!baskets.containsKey(id)) {
            return null;
        }
        return baskets.remove(id);
    }

    /**
     * Retrieves a shopping basket from the repository.
     * 
     * @param id The pair of client ID and store ID that uniquely identifies the basket
     * @return The shopping basket, or null if no basket was found with the given ID
     */
    @Override
    public ShoppingBasket get(Pair<String, String> id) {
        if (!baskets.containsKey(id)) {
            return null;
        }
        return baskets.get(id);
    }

    /**
     * Updates an existing shopping basket in the repository.
     * 
     * @param id The pair of client ID and store ID that uniquely identifies the basket
     * @param value The new shopping basket value
     * @return The updated shopping basket, or null if no basket was found with the given ID
     */
    @Override
    public ShoppingBasket update(Pair<String, String> id, ShoppingBasket value) {
        if (!baskets.containsKey(id)) {
            return null;
        }
        baskets.put(id, value);
        return value;
    }
    
}