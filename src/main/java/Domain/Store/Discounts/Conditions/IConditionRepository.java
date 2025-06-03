package Domain.Store.Discounts.Conditions;

import java.util.List;


/**
 * Repository interface for managing condition entities.
 * Provides CRUD operations for conditions using UUID as the primary key.
 */
public interface IConditionRepository {
    
    /**
     * Saves a condition to the repository.
     * If a condition with the same ID already exists, it will be updated.
     * 
     * @param condition the condition to save
     * @throws IllegalArgumentException if condition is null or has a null ID
     */
    boolean save(String StoreID, Condition condition);
    
    /**
     * Finds a condition by its unique identifier.
     * 
     * @param id the UUID of the condition to find
     * @return an Optional containing the condition if found, empty otherwise
     * @throws IllegalArgumentException if id is null
     */
    Condition get(String id);
    
    /**
     * Deletes a condition by its unique identifier.
     * 
     * @param id the UUID of the condition to delete
     * @throws IllegalArgumentException if id is null
     */
    void delete(String id);
    
    
    List<Condition> getStoreConditions(String storeId);
 
    
    /**
     * Checks if a condition exists with the given ID.
     * 
     * @param id the String to check
     * @return true if a condition exists with the given ID, false otherwise
     */
    boolean exists(String id);
    
    /**
     * Removes all conditions from the repository.
     * Primarily used for testing purposes.
     */
    void clear();
    
    /**
     * Gets the total number of conditions in the repository.
     * 
     * @return the count of conditions
     */
    int size();
}