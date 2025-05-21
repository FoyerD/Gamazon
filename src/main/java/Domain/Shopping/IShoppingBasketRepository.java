package Domain.Shopping;

import Domain.ILockbasedRepository;
import Domain.Pair;

/**
 * Abstract repository class for managing shopping baskets.
 * Extends the lock-based repository pattern for thread-safe operations on shopping baskets.
 * Uses a pair of client ID and store ID as the unique identifier for baskets.
 */
public abstract class IShoppingBasketRepository extends ILockbasedRepository<ShoppingBasket, Pair<String, String>> {
    /**
     * Removes all shopping baskets from the repository.
     * This method should clear the entire repository state.
     */
    abstract public void clear();
}