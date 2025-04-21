package Domain.Shopping;

import java.util.Map;

public interface IShoppingBasket {
    void removeItem(String productId, int quantity);
    void addItem(String productId, int quantity); 
    void clear();
    // double getTotalPrice(); // Really needed? Isn't the price calculated somewhere else?
    boolean isEmpty();
    Map<String, Integer> getItems(); // returns a map of productId and quantity.


    boolean areIdentical(IShoppingBasket storeBasket); // Same as equals?
    String getClientId();
    String getStoreId();

    Map<Integer, IShoppingBasket> getShoppingBaskets(); // Weird to have it here
    void addShoppingBasket(IShoppingBasket basket, String userName, double price); // Weird to have it here

}