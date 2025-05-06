package Domain.Store;

import Domain.ILockbasedRepository;

public abstract class IStoreRepository extends ILockbasedRepository<Store, String> {
    abstract public Store getStoreByName(String name);

    public IStoreRepository() {
        super();
    }
}
