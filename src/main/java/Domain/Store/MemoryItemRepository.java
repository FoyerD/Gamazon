package Domain.Store;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Domain.Pair;

public class MemoryItemRepository implements IItemRepository{

    private Map<Pair<String, String>, Item> items;

    public MemoryItemRepository(){
        this.items = new ConcurrentHashMap<>();
    }
    
    @Override
    public Item getItem(String storeId, String productId) {
        List<Item> itemAsList = this.items.values().stream().filter(item -> item.getProductId().equals(productId) && item.getStoreId().equals(storeId)).toList();
        return itemAsList.isEmpty() ? null : itemAsList.get(0);
    }

    @Override
    public List<Item> getByStoreId(String storeId) {
        return this.items.values().stream().filter(item -> item.getStoreId().equals(storeId)).toList();
    }

    @Override
    public List<Item> getByProductId(String productId) {
        return this.items.values().stream().filter(item -> item.getProductId().equals(productId)).toList();
    }

    @Override
    public List<Item> getAvailabeItems() {
        return this.items.values().stream().filter(item -> item.getAmount() > 0).toList();
    }

    @Override
    public void update(Pair<String, String> id, Item item) {
        items.computeIfPresent(id, (k, v) -> item);
    }

    @Override
    public boolean add(Pair<String, String> id, Item item) {
        return this.items.put(id, item) != null;
    }

    @Override
    public Item remove(Pair<String, String> id) {
        return this.items.remove(id);
    }

    @Override
    public Item get(Pair<String, String> id) {
        return getItem(id.getFirst(), id.getSecond());
    }
    
}
