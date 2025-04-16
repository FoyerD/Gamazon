package Domain.Store;

import Domain.IRepository;

public interface IStoreRepository extends IRepository<Store, String> {
    public Store getStoreByName(String name);
}
