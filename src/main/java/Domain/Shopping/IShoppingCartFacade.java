package Domain.Shopping;

import java.util.Date;

public interface IShoppingCartFacade {
    IShoppingCart getCart(String clientId);
    ShoppingBasket getBasket(String clientId, String storeId);
    boolean addProductToCart(String storeId, String clientId, String productId, int quantity);
    boolean checkout(String clientId, String card_number, Date expiry_date, String cvv, long andIncrement, String clientName, String deliveryAddress);
    boolean removeProductFromCart(String storeId, String clientId, String productId, int quantity);
    boolean removeProductFromCart(String storeId, String clientId, String productId);
    int getTotalItems(String clientId);
    boolean isEmpty(String clientId);
    boolean clearCart(String clientId);
    boolean clearBasket(String clientId, String storeId);
}