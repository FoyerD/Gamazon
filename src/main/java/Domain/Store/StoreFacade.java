package Domain.Store;

public class StoreFacade {
    private IStoreRepository storeRepository;

    public StoreFacade(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }
    public StoreFacade() {
        this.storeRepository = null;
    }
    public void setStoreRepository(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }
    public Store getStore(String storeId) {
        return storeRepository.get(storeId);
    }
    public Store addStore(String name, String description, String founderId) {
        if (this.getStoreByName(name) != null) throw new RuntimeException("Store name already exists.");

        String storeId = System.currentTimeMillis() + "";
        Store store = new Store(storeId, name, description, founderId);
        this.storeRepository.add(storeId, store);
        return store;
    }
    public Store getStoreByName(String name) {
        return storeRepository.getStoreByName(name);
    }

    public boolean openStore(String storeId){
        Store store = this.storeRepository.get(storeId);
        if (store == null) throw new RuntimeException("Store not found.");
        if(store.isOpen()) throw new RuntimeException("Store is already open.");

        store.setOpen(true);
        Store newStore = this.storeRepository.update(storeId, store);
        if(!store.equals(newStore)) throw new RuntimeException("Store not updated.");
        return true;
    }
}
