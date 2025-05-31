package Infrastructure.JpaSpringRepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Domain.Shopping.ShoppingBasket;
import Domain.Shopping.ShoppingBasketId;

public interface IJpaShoppingBasketRepository extends JpaRepository<ShoppingBasket, ShoppingBasketId> {

    /**
     * Retrieves all baskets for a specific client.
     */
    List<ShoppingBasket> getByClientId(String clientId);

    /**
     * Retrieves all baskets in a specific store.
     */
    List<ShoppingBasket> getByStoreId(String storeId);

    /**
     * Retrieves a specific basket by client and store IDs.
     */
    @Query("SELECT b FROM ShoppingBasket b WHERE b.clientId = :clientId AND b.storeId = :storeId")
    ShoppingBasket getBasket(@Param("clientId") String clientId, @Param("storeId") String storeId);
} 