package Domain.Store;

import java.util.Map;

import Domain.IRepository;
import Domain.Shopping.IShoppingBasket;
import Domain.Shopping.IShoppingCart;

public interface IStoreRepository extends IRepository<Store, String> {

    Store getStore(int storeId);

    void closeStore(String storeId);

}
