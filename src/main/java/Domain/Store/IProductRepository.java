package Domain.Store;

import Domain.ILockbasedRepository;

public abstract class IProductRepository extends ILockbasedRepository<Product, String> {
    public abstract Product getByName(String name);
}