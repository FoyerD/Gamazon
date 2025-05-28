package Domain.Store.Discounts;


import java.util.Set;


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
    void save(String StoreID, Discount discount);
    
    /**
     * Finds a discount by its unique identifier.
     * 
     * @param id the UUID of the discount to find
     * @return the discount if found, null otherwise
     * @throws IllegalArgumentException if id is null
     */
    Discount get(String id);
    
    /**
     * Deletes a discount by its unique identifier.
     * 
     * @param id the UUID of the discount to delete
     * @throws IllegalArgumentException if id is null
     */
    void delete(String id);
    
    
    /**
     * Checks if a discount exists with the given ID.
     * 
     * @param id the UUID to check
     * @return true if a discount exists with the given ID, false otherwise
     * @throws IllegalArgumentException if id is null
     */
    boolean exists(String id);
    
    /**
     * Removes all discounts from the repository.
     * Primarily used for testing purposes.
     */
    void clear();
    
    
    /**
     * Gets all the discounts for a specific store.
     * 
     * @param storeId the ID of the store to count discounts for
     * @return the count of discounts for the specified store
     * @throws IllegalArgumentException if storeId is null or empty
     */
    Set<Discount> getStoreDiscounts(String storeId);
}