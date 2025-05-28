package Domain.Repos;

import java.util.Set;

import Domain.Store.Product;

public abstract class IProductRepository extends ILockbasedRepository<Product, String> {
    public abstract Product getByName(String name);
    public abstract Set<Product> getAll();
}