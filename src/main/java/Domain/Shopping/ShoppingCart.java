package Domain.Shopping;

import java.util.HashSet;
import java.util.Set;

class ShoppingCart implements IShoppingCart {

    private String clientId;
    private Set<String> baskets;
    public ShoppingCart(String clientId) {
        this.clientId = clientId;
        this.baskets = new HashSet<>();
    }


    public ShoppingCart(String clientId, Set<String> baskets) {
        this.clientId = clientId;
        this.baskets = baskets;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public Set<String> getCart() {
        return new HashSet<>(baskets); // return a copy for safety
    }

    @Override
    public void addStore(String storeId) {
        baskets.add(storeId);
    }

    @Override
    public void removeStore(String storeId) {
        baskets.remove(storeId);
    }

    @Override
    public boolean hasStore(String storeId) {
        return baskets.contains(storeId);
    }

    @Override
    public void clear() {
        baskets.clear();
    }

    @Override
    public boolean isEmpty() {
        return baskets.isEmpty();
    }
}
