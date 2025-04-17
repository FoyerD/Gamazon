import Domain.Shopping.PurchaseInfo;

public interface IShoppingService {
    void addProductToCart(PurchaseInfo request);
    IShoppingCart getCart(String clientId);
    IShoppingBasket getBasket(String clientId, String storeId);
    void immediatePurchase(PurchaseInfo request);
    void makeBid(PurchaseInfo request);
    void joinAuction(PurchaseInfo request);
    void joinLottery(PurchaseInfo request);
}
