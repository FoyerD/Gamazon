package Domain.Store;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StoreRepositoryMemory implements IStoreRepository{
    private Map<String, Store> stores;

    public StoreRepositoryMemory() {
        this.stores = new ConcurrentHashMap<>();
    }

    @Override
    public boolean add(String storeId, Store store) {
        if (this.stores.containsKey(storeId)) throw new IllegalArgumentException("Item with this ID already exists");
        return this.stores.put(storeId, store) == null;
    }

    @Override
    public Store remove(String id) {
        return stores.remove(id);
    }

    @Override
    public Store get(String id) {
        return this.stores.get(id);
    }

    @Override
    public Store update(String id, Store store) {
        if (!this.stores.containsKey(id)) throw new IllegalArgumentException("Item with this ID does not exist");
        return this.stores.put(id, store);
    }

}
