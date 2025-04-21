package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;

class ShoppingCart implements IShoppingCart {

    private String clientId;
    private Map<String, ShoppingBasket> baskets; // Map of storeId to ShoppingBasket

    public ShoppingCart(String clientId) {
        this.clientId = clientId;
        this.baskets = new HashMap<>();
    }


    public ShoppingCart(String clientId, Map<String, ShoppingBasket> baskets) {
        this.clientId = clientId;
        this.baskets = baskets;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void checkout() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'Checkout'");
    }


    @Override
    public void addItem(String storeId, String productId, int quantity) {
        ShoppingBasket basket;
        if(!baskets.containsKey(storeId)){
            basket = new ShoppingBasket();
        } 
        else {
            basket = baskets.remove(storeId);
        }
        basket.addOrder(productId, quantity);
        baskets.put(storeId, basket);
    }


    @Override
    public void removeItem(String storeId, String productId, int quantity) {
        if(!baskets.containsKey(storeId)){
            throw new IllegalArgumentException("No orders in this store");
        }
        ShoppingBasket basket = baskets.remove(storeId);
        basket.removeItem(productId, quantity);
        baskets.put(storeId, basket);
    }


    @Override
    public Map<String, ShoppingBasket> getCart() {
        return baskets;
    }

    
    @Override
    public ShoppingBasket getBasket(String storeId){
        return baskets.get(storeId);
    }


    @Override
    public void removeItem(String storeId, String productId) {
        baskets.get(storeId).removeItem(productId);
    }


    @Override
    public void clear() {
        baskets.clear();
    }


    @Override
    public int getTotalItems() {
        int total = 0;
        for(ShoppingBasket basket : baskets.values()){
            total += basket.getQuantity();
        }
        return total;
    }


    @Override
    public boolean isEmpty() {
        return getTotalItems() == 0;
    }

    
}
