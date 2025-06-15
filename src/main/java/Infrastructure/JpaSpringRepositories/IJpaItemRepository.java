package Infrastructure.JpaSpringRepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Domain.Store.Item;
import Domain.Store.ItemId;

public interface IJpaItemRepository extends JpaRepository<Item, ItemId> {

    /**
     * Retrieves an item by storeId and productId (composite key).
     */
    @Query("SELECT i FROM Item i WHERE i.storeId = :storeId AND i.productId = :productId")
    Item getItem(@Param("storeId") String storeId, @Param("productId") String productId);

    /**
     * Returns all items belonging to a specific store.
     */
    List<Item> getByStoreId(String storeId);

    /**
     * Returns all items corresponding to a given product ID.
     */
    List<Item> getByProductId(String productId);

    /**
     * Retrieves a list of all items currently in stock (amount > 0).
     */
    @Query("SELECT i FROM Item i WHERE i.amount > 0")
    List<Item> getAvailabeItems();

}
