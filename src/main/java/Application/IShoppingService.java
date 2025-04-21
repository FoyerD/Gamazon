package Application;

import Domain.Store.Item;

public interface IShoppingService {
    void addProductToCart(String clientId, Item item);
    void removeProductFromCart(String clientId, Item item);
    //IShoppingCart getCart(String clientId);
    void immediatePurchase(Item item);
    void makeBid(Item item);
    void joinAuction(Item item);
    void joinLottery(Item item);
}
