package Domain.Shopping;

import java.util.Map;

public interface IShoppingCart {
    void checkout();
    void addItem(String storeId, String productId, int quantity);  
    void removeItem(String storeId, String productId, int quantity); // removes the given quantity of the product.
    Map<String, ShoppingBasket> getCart();
    ShoppingBasket getBasket(String storeId);
    void removeItem(String storeId, String productId); // removes the entire quantity of the product.
    void clear();
    int getTotalItems();
    boolean isEmpty();
    String getClientId();
}