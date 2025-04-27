package Domain.Shopping;

import org.slf4j.ILoggerFactory;

import Domain.ILockbasedRepository;
import Domain.Pair;

public abstract class IShoppingBasketRepository extends ILockbasedRepository<ShoppingBasket, Pair<String, String>> {
    abstract public void clear();
}
