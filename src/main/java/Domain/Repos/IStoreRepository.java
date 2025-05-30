package Domain.Repos;

import Domain.Store.Store;

public abstract class IStoreRepository extends ILockbasedRepository<Store, String> {
    abstract public Store getStoreByName(String name);

    public IStoreRepository() {
        super();
    }
}
