package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Domain.Pair;

public class ShoppingBasketRepository implements IShoppingBasketRepository {
    
    private Map<Pair<String, String>, ShoppingBasket> baskets;

    public ShoppingBasketRepository() {
        
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