package Domain.Store;

import java.util.Map;

import Domain.IRepository;
import Domain.Shopping.IShoppingBasket;
import Domain.Shopping.IShoppingCart;

public interface IStoreRepository extends IRepository<Store, String> {

    void checkProductsExist(int storeId, Map<Integer, Item> products);

    double calculateBasketPrice(IShoppingBasket basket);

    void removeCartQuantity(IShoppingCart cart);

    void addCartQuantity(IShoppingCart cart);

    Store getStore(Object storeId);

    void closeStore(int storeId);

}
