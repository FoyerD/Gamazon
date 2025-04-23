package Domain.Shopping;

import java.util.Map;

public interface IShoppingCartFacade {
    // IShoppingCart getCart(String clientId);
    // ShoppingBasket getBasket(String clientId, String storeId);
    boolean addProductToCart(String storeId, String clientId, String productId, int quantity);
    boolean checkout(String clientId);
    boolean removeProductFromCart(String storeId, String clientId, String productId, int quantity);
    boolean removeProductFromCart(String storeId, String clientId, String productId);
    int getTotalItems(String clientId);
    boolean isEmpty(String clientId);
    boolean clearCart(String clientId);
    boolean clearBasket(String clientId, String storeId);
    Map<String, Map<String, Integer>> viewCart(String clientId);
}