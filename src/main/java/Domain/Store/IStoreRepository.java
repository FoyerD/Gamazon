package Domain.Store;

import java.util.Map;

import Domain.Shopping.IShoppingBasket;
import Domain.Shopping.IShoppingCart;

public interface IStoreRepository {

    Store getStore(int storeId);

    void closeStore(String storeId);

}
