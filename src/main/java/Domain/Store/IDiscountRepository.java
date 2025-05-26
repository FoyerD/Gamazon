package Domain.Store;

import java.util.Set;

import Domain.Store.Discounts.Discount;

public class IDiscountRepository {
    
    // This interface is intended to define methods for managing discounts in a store.
    // It should include methods for adding, removing, and retrieving discounts.
    
    // Example method signatures:
    // void addDiscount(Discount discount);
    // void removeDiscount(String discountId);
    // List<Discount> getAllDiscounts();
    // Discount getDiscountById(String discountId);


    // gets all discounts for a specific store
    public Set<Discount> getAllDiscounts(String storeId) {
        // This method should return all discounts for a specific store.
        // Implementation will depend on the underlying data storage.
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void addDiscount(Discount discount) {
        // This method should add a new discount to the store.
        // Implementation will depend on the underlying data storage.
        throw new UnsupportedOperationException("Method not implemented");
    }

}
