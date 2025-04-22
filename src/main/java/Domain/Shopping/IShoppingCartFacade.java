package Domain.Shopping;


interface IShoppingCartFacade {
    IShoppingCart getCart(String clientId);
    ShoppingBasket getBasket(String clientId, String storeId);
    void addProductToCart(String storeId, String clientId, String productId, int quantity);
    void checkout(String clientId);
    public void removeProductFromCart(String storeId, String clientId, String productId, int quantity);
    public void removeProductFromCart(String storeId, String clientId, String productId);
}