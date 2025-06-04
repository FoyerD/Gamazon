package Domain.Store.Discounts;

import java.util.List;

import Domain.ILockbasedRepository;


/**
 * Repository interface for managing discount entities.
 * Provides CRUD operations for discounts using UUID as the primary key.
 */
public abstract class IDiscountRepository  extends ILockbasedRepository<Discount, String> {
    
    /**
     * Saves a discount to the repository.
     * If a discount with the same ID already exists, it will be updated.
     * 
     * @param discountId the unique identifier for the discount
     * @param discount the discount to save
     * @return true if the discount was saved successfully
     * @throws IllegalArgumentException if discount is null or has a null ID
     */
    public abstract boolean add(String discountId, Discount discount);
    
    /**
     * Finds a discount by its unique identifier.
     * 
     * @param id the UUID of the discount to find
     * @return the discount if found, null otherwise
     * @throws IllegalArgumentException if id is null
     */
    public abstract Discount get(String id);
    
    /**
     * Deletes a discount by its unique identifier.
     * 
     * @param id the UUID of the discount to delete
     * @throws IllegalArgumentException if id is null
     */
    public abstract Discount remove(String id);
    
    
    /**
     * Checks if a discount exists with the given ID.
     * 
     * @param id the UUID to check
     * @return true if a discount exists with the given ID, false otherwise
     * @throws IllegalArgumentException if id is null
     */
    public abstract boolean exists(String id);
    
    /**
     * Removes all discounts from the repository.
     * Primarily used for testing purposes.
     */
    public abstract void clear();
    
    
    //TODO! Roi or Itay or Almog or anyone adding a DB to this,
    // this method should only return HEAD DISCOUNTS, i.e
    // the discounts that have no parent discount.
    // please make sure to implement this correctly, otherwise some diocunts will be applied multiple times.
    /**
     * Gets all the discounts for a specific store.
     * 
     * @param storeId the ID of the store to count discounts for
     * @return the count of discounts for the specified store
     * @throws IllegalArgumentException if storeId is null or empty
     */
    public abstract List<Discount> getStoreDiscounts(String storeId);

    /**
     * Returns the total number of discounts in the repository.
     */
    public abstract int size();
}