package Domain.Shopping;

public interface IShoppingCart {
    void Checkout();
    void addItem(String shopId, String productId, int quantity);  
    void removeItem(String shopId, String productId, int quantity); // removes the given quantity of the product.
    int getProduct(String shopId, String productId); // returns product quantity.
    void removeItem(String shopId, String productId); // removes the entire quantity of the product.
    void clear();
    double getTotalPrice();
    int getTotalItems();
    boolean isEmpty();
}