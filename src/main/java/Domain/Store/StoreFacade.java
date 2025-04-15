package Domain.Store;

import Application.Response;

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
}
