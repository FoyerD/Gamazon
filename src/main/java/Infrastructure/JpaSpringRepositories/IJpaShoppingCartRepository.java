package Infrastructure.JpaSpringRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Domain.Shopping.ShoppingCart;

import java.util.List;

public interface IJpaShoppingCartRepository extends JpaRepository<ShoppingCart, String> {

    /**
     * Retrieves all shopping carts in the system.
     */
    @Query("SELECT c FROM ShoppingCart c")
    List<ShoppingCart> getAllCarts();

    /**
     * Retrieves all shopping carts that contain a specific store.
     */
    @Query("SELECT c FROM ShoppingCart c WHERE :storeId MEMBER OF c.baskets")
    List<ShoppingCart> getCartsByStore(String storeId);
} 