package Domain.Store.Discounts;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * @return the discount if found, null otherwise
     * @throws IllegalArgumentException if id is null
     */
    Discount findById(UUID id);
    
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
     * Retrieves all discounts for a specific store.
     * 
     * @param storeId the ID of the store to find discounts for
     * @return a list of discounts for the specified store, empty list if none found or store doesn't exist
     * @throws IllegalArgumentException if storeId is null or empty
     */
    List<Discount> findByStoreId(String storeId);
    
    /**
     * Checks if a discount exists with the given ID.
     * 
     * @param id the UUID to check
     * @return true if a discount exists with the given ID, false otherwise
     * @throws IllegalArgumentException if id is null
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
    
    /**
     * Gets all the discounts for a specific store.
     * 
     * @param storeId the ID of the store to count discounts for
     * @return the count of discounts for the specified store
     * @throws IllegalArgumentException if storeId is null or empty
     */
    Set<Discount> getStoreDiscounts(String storeId);
}