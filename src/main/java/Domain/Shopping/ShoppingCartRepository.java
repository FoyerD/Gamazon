package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;

class ShoppingCartRepository implements IShoppingCartRepository {
    private final Map<String, IShoppingCart> carts = new HashMap<>();

    public ShoppingCartRepository() {
    }

    @Override
    public boolean add(String id, IShoppingCart value) {
        if (carts.containsKey(id)) {
            return false;
        }
        carts.put(id, value);
        return true;
    }

    @Override
    public IShoppingCart remove(String id) {
        if (!carts.containsKey(id)) {
            return null;
        }
        return carts.remove(id); 
    }

    @Override
    public IShoppingCart get(String id) {
        if (!carts.containsKey(id)) {
            return null; 
        }
        return carts.get(id); 
    }

    @Override
    public IShoppingCart update(String id, IShoppingCart value) {
        if (!carts.containsKey(id)) {
            return null;
        }
        carts.put(id, value);
        return value;
    }

    @Override
    public void clear() {
        carts.clear(); 
    }

    
}
