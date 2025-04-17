package Domain.Shopping;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ShoppingCartRepository implements IShoppingCartRepository {
    private final Map<String, IShoppingCart> carts = new HashMap<>();
    private final IShoppingBasketRepository basketRepo;

    public ShoppingCartRepository(IShoppingBasketRepository basketRepo) {
        this.basketRepo = basketRepo;
    }

    @Override
    public IShoppingCart getCart(String clientId) {
        return carts.get(clientId);
    }

    @Override
    public void saveCart(IShoppingCart cart) {
        carts.put(cart.getClientId(), cart);
    }
}
