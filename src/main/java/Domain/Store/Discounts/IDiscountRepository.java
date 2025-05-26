package Domain.Store.Discounts;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;


/**
 * Repository interface for managing discount entities.
 * Provides CRUD operations for discounts using UUID as the primary key.
 */
public interface IDiscountRepository {
    
    /**
     * Saves a discount to the repository.
     * If a discount with the same ID already exists, it will be updated.
     * 
     * @param discount the discount to save
     * @throws IllegalArgumentException if discount is null or has a null ID
     */
    void save(Discount discount);
    
    /**
     * Finds a discount by its unique identifier.
     * 
     * @param id the UUID of the discount to find
     * @return an Optional containing the discount if found, empty otherwise
     * @throws IllegalArgumentException if id is null
     */
    Optional<Discount> findById(UUID id);
    
    /**
     * Deletes a discount by its unique identifier.
     * 
     * @param id the UUID of the discount to delete
     * @throws IllegalArgumentException if id is null
     */
    void deleteById(UUID id);
    
    /**
     * Retrieves all discounts in the repository.
     * 
     * @return a map of UUID to Discount containing all discounts
     */
    Map<UUID, Discount> findAll();
    
    /**
     * Checks if a discount exists with the given ID.
     * 
     * @param id the UUID to check
     * @return true if a discount exists with the given ID, false otherwise
     */
    boolean existsById(UUID id);
    
    /**
     * Removes all discounts from the repository.
     * Primarily used for testing purposes.
     */
    void clear();
    
    /**
     * Gets the total number of discounts in the repository.
     * 
     * @return the count of discounts
     */
    int size();
}