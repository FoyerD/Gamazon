package Domain.Store;
import java.util.List;

import Domain.IRepository;
import Domain.Pair;

public interface IItemRepository extends IRepository<Item, Pair<String, String>>{
    Item getItem(String storeId, String productId);
    List<Item> getByStoreId(String storeId);
    List<Item> getByProductId(String productId);
    List<Item> getAvailabeItems();
    void update(Pair<String, String> id, Item item);
     
}
