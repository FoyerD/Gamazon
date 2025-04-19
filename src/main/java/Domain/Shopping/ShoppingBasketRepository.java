package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;

public class ShoppingBasketRepository implements IShoppingBasketRepository{
    
    private Map<Pair, ShoppingBasket> shoppingBaskets;

    public ShoppingBasketRepository() {
        
        this.shoppingBaskets = new HashMap<>();
    }

    public IShoppingBasket get(String clientId, String storeId) {

        return shoppingBaskets.get(new Pair(clientId, storeId));
    }

    public IShoppingBasket remove(String clientId, String storeId) {
        return shoppingBaskets.remove(new Pair(clientId, storeId));
    }

    public void add(IShoppingBasket shoppingBasket) {
        if (shoppingBasket == null) {
            throw new IllegalArgumentException("Shopping basket cannot be null");
        }
        String clientId = shoppingBasket.getClientId();
        String storeId = shoppingBasket.getStoreId();
        shoppingBaskets.put(new Pair(clientId, storeId), (ShoppingBasket) shoppingBasket);
    }

    public boolean contains(String clientId, String storeId) {
        return shoppingBaskets.containsKey(new Pair(clientId, storeId));
    }
    
    public void clear() {
        shoppingBaskets.clear();
    }
    

    private class Pair {

        private String s1;
        private String s2;

        public Pair(String s1, String s2) {
            this.s1 = s1;
            this.s2 = s2;
        }

        public String getS1() {
            return s1;
        }

        public String getS2() {
            return s2;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair pair = (Pair) o;
            return s1.equals(pair.s1) && s2.equals(pair.s2);
        }

    }
}