package Domain.Store.Discounts.Conditions;

import java.util.Map;
import java.util.UUID;


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
    void save(Condition condition);
    
    /**
     * Finds a condition by its unique identifier.
     * 
     * @param id the UUID of the condition to find
     * @return an Optional containing the condition if found, empty otherwise
     * @throws IllegalArgumentException if id is null
     */
    Condition findById(UUID id);
    
    /**
     * Deletes a condition by its unique identifier.
     * 
     * @param id the UUID of the condition to delete
     * @throws IllegalArgumentException if id is null
     */
    void deleteById(UUID id);
    
    /**
     * Retrieves all conditions in the repository.
     * 
     * @return a map of UUID to Condition containing all conditions
     */
    Map<UUID, Condition> findAll();
    
    /**
     * Checks if a condition exists with the given ID.
     * 
     * @param id the UUID to check
     * @return true if a condition exists with the given ID, false otherwise
     */
    boolean existsById(UUID id);
    
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