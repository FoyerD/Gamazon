package Domain.Shopping;

import Domain.ILockbasedRepository;

/**
 * Abstract repository class for managing shopping carts.
 * Extends the lock-based repository pattern for thread-safe operations on shopping carts.
 * Uses the client ID as the unique identifier for shopping carts.
 */
public abstract class IShoppingCartRepository extends ILockbasedRepository<IShoppingCart, String> {
    /**
     * Removes all shopping carts from the repository.
     * This method should clear the entire repository state.
     */
    abstract public void clear();
}