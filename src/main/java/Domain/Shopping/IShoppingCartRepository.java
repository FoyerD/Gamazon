package Domain.Shopping;


import Domain.ILockbasedRepository;

public abstract class IShoppingCartRepository extends ILockbasedRepository<IShoppingCart, String> {
    abstract public void clear();
}
