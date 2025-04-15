package Domain.Store;
import Domain.IRepository;
import Domain.Pair;



public interface IItemRepository extends IRepository<Item, Pair<String, String>>{
    Item getByName(String name);
    Item getByCategory(String category);
    Item getByStoreId(String storeId, String productId);
    void update(Item item);
    
}
