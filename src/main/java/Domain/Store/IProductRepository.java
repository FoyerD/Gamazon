package Domain.Store;

import java.util.Set;

import Domain.ILockbasedRepository;

public abstract class IProductRepository extends ILockbasedRepository<Product, String> {
    public abstract Product getByName(String name);
    public abstract Set<Product> getAll();
}