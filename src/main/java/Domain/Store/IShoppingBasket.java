package Domain.Store;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IShoppingBasket {

    boolean areIdentical(IShoppingBasket storeBasket);

    int getShoppingBasketCount();

    Map<Integer, IShoppingBasket> getShoppingBaskets();

    IShoppingBasket getShoppingBasket(int id);

    void clean();

    void addShoppingBasket(IShoppingBasket basket, String userName, double price);

    List<IShoppingBasket> getUserShoppingBasketsInRange(String userName, LocalDateTime startDateTime,
            LocalDateTime endDateTime);

    List<IShoppingBasket> getStoreShoppingBaskets(int storeId);

    List<IShoppingBasket> getStoreShoppingBasketsInRange(int storeId, LocalDateTime startDateTime,
            LocalDateTime endDateTime);

    int getStoreId();

    List<Item> getItems();
}
