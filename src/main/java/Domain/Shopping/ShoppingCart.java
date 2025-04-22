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

    // @Override
    // public void checkout() {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'Checkout'");
    // }


    // @Override
    // public void addItem(String storeId, String productId, int quantity) {
    //     ShoppingBasket basket;
    //     if(!baskets.contains(storeId)){
    //         basket = new ShoppingBasket();
    //     } 
    //     else {
    //         basket = baskets.remove(storeId);
    //     }
    //     basket.addOrder(productId, quantity);
    //     baskets.put(storeId, basket);
    // }


    // @Override
    // public void removeItem(String storeId, String productId, int quantity) {
    //     if(!baskets.containsKey(storeId)){
    //         throw new IllegalArgumentException("No orders in this store");
    //     }
    //     ShoppingBasket basket = baskets.remove(storeId);
    //     basket.removeItem(productId, quantity);
    //     baskets.put(storeId, basket);
    // }


    
    // @Override
    // public ShoppingBasket getBasket(String storeId){
    //     return baskets.get(storeId);
    // }


    // @Override
    // public void removeItem(String storeId, String productId) {
    //     baskets.get(storeId).removeItem(productId);
    // }


    // @Override
    // public void clear() {
    //     baskets.clear();
    // }


    // @Override
    // public int getTotalItems() {
    //     int total = 0;
    //     for(ShoppingBasket basket : baskets.values()){
    //         total += basket.getQuantity();
    //     }
    //     return total;
    // }


    // @Override
    // public boolean isEmpty() {
    //     return getTotalItems() == 0;
    // }

    
}
