package Domain.Repos;

import java.util.List;

import Domain.Store.Store;

public abstract class IStoreRepository extends ILockbasedRepository<Store, String> {
    abstract public Store getStoreByName(String name);
    abstract public List<Store> getAllStores();
    public IStoreRepository() {
        super();
    }


}
