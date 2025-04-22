package Domain.Shopping;

import Domain.IRepository;

public interface IShoppingCartRepository extends IRepository<IShoppingCart, String> {
    public void clear();
}
