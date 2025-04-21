package Domain.Shopping;

public interface IShoppingCartFacade {
    IShoppingCart getCart(String clientId);
    IShoppingBasket getBasket(String clientId, String storeId);
    void addProductToCart(PurchaseInfo info);
}