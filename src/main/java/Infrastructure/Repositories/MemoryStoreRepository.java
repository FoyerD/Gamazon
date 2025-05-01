package Infrastructure.Repositories;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Domain.Store.IStoreRepository;
import Domain.Store.Store;

public class MemoryStoreRepository extends IStoreRepository{
    private Map<String, Store> stores;

    public MemoryStoreRepository() {
        super();
        this.stores = new ConcurrentHashMap<>();
    }

    @Override
    public boolean add(String storeId, Store store) {
        if (!isIdValid(storeId)) throw new IllegalArgumentException("ID cannot be null");
        if (!storeId.equals(store.getId())) throw new IllegalArgumentException("ID does not match the store ID");
        if (this.stores.containsKey(storeId)) throw new IllegalArgumentException("Item with this ID already exists");

        this.addLock(storeId);
        return this.stores.put(storeId, store) == null;
    }

    @Override
    public Store remove(String id) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");

        this.removeLock(id);
        return stores.remove(id);
    }

    @Override
    public Store get(String id) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");
        return this.stores.get(id);
    }

    @Override
    public Store update(String id, Store store) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");
        if (!id.equals(store.getId())) throw new IllegalArgumentException("ID does not match the store ID");
        if (!this.stores.containsKey(id)) throw new IllegalArgumentException("Item with this ID does not exist");
        return this.stores.put(id, store);
    }

    @Override
    public Store getStoreByName(String name) {
        for (Store store : stores.values()) {
            if (store.getName().equals(name)) return store;
        }
        return null;
    }
}
