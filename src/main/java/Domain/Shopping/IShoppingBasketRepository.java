package Domain.Shopping;

import Domain.IRepository;
import Domain.Pair;

public interface IShoppingBasketRepository extends IRepository<ShoppingBasket, Pair<String, String>> {
    public void clear();
}
