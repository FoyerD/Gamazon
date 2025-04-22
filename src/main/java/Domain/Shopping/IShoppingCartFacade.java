package Domain.Shopping;


interface IShoppingCartFacade {
    IShoppingCart getCart(String clientId);
    ShoppingBasket getBasket(String clientId, String storeId);
    void addProductToCart(String storeId, String clientId, String productId, int quantity);
    void checkout(String clientId);
    void removeProductFromCart(String storeId, String clientId, String productId, int quantity);
    void removeProductFromCart(String storeId, String clientId, String productId);
    int getTotalItems(String clientId);
    boolean isEmpty(String clientId);
    void clearCart(String clientId);
}