package Domain.Shopping;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCartRepository implements IShoppingCartRepository {
    private final Map<String, IShoppingCart> carts = new HashMap<>();
    private final IShoppingBasketRepository basketRepo;

    public ShoppingCartRepository(IShoppingBasketRepository basketRepo) {
        this.basketRepo = basketRepo;
    }

    @Override
    public IShoppingCart get(String clientId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public void add(IShoppingCart shoppingCart) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public void remove(IShoppingCart shoppingCart) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }
}
