package Domain.Store;
import java.util.List;

import Domain.ILockbasedRepository;
import Domain.Pair;

public abstract class IItemRepository extends ILockbasedRepository<Item, Pair<String, String>>{
    abstract public Item getItem(String storeId, String productId);
    abstract public List<Item> getByStoreId(String storeId);
    abstract public List<Item> getByProductId(String productId);
    abstract public List<Item> getAvailabeItems();
    abstract public Item update(Pair<String, String> id, Item item);
     
}
