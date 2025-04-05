package Domain.Store;

import java.util.Map;

public interface IStoreFacade {

    void checkProductsExist(int storeId, Map<Integer, Item> products);

    double calculateBasketPrice(IShoppingBasket basket);

    void removeCartQuantity(IShoppingCart cart);

    void addCartQuantity(IShoppingCart cart);

    IStore getStore(Object storeId);

}
