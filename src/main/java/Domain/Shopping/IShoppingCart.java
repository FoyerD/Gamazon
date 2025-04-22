package Domain.Shopping;

import java.util.Set;

public interface IShoppingCart {
    Set<String> getCart();
    String getClientId();
    void addStore(String storeId);
    void removeStore(String storeId);
    boolean hasStore(String storeId);
    // void checkout();
    // void addItem(String storeId, String productId, int quantity);  
    // void removeItem(String storeId, String productId, int quantity); // removes the given quantity of the product.
    // ShoppingBasket getBasket(String storeId);
    // void removeItem(String storeId, String productId); // removes the entire quantity of the product.
    // void clear();
    // int getTotalItems();
    // boolean isEmpty();
}