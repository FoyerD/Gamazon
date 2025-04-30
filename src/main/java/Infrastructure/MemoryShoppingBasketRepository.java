package Infrastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Domain.Pair;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.ShoppingBasket;

public class MemoryShoppingBasketRepository extends IShoppingBasketRepository {
    
    private Map<Pair<String, String>, ShoppingBasket> baskets;

    public MemoryShoppingBasketRepository() {
        
        this.baskets = new ConcurrentHashMap<>();
    }

    @Override
    public void clear() {
        baskets.clear();
    }

    @Override
    public boolean add(Pair<String, String> id, ShoppingBasket value) {
        if (baskets.containsKey(id)) {
            return false;
        }
        baskets.put(id, value);
        return true;
    }

    @Override
    public ShoppingBasket remove(Pair<String, String> id) {
        if (!baskets.containsKey(id)) {
            return null;
        }
        return baskets.remove(id);
    }

    @Override
    public ShoppingBasket get(Pair<String, String> id) {
        if (!baskets.containsKey(id)) {
            return null;
        }
        return baskets.get(id);
    }

    @Override
    public ShoppingBasket update(Pair<String, String> id, ShoppingBasket value) {
        if (!baskets.containsKey(id)) {
            return null;
        }
        baskets.put(id, value);
        return value;
    }
    
}