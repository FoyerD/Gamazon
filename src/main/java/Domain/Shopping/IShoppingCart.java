package Domain.Shopping;

import java.util.Set;

public interface IShoppingCart {
    Set<String> getCart();
    String getClientId();
    void addStore(String storeId);
    void removeStore(String storeId);
    boolean hasStore(String storeId);
    void clear();
    boolean isEmpty();
}