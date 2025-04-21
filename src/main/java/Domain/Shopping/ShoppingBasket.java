package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;

import Domain.Shopping.IShoppingCart;
import Domain.Shopping.IShoppingCartRepository;

class ShoppingBasket {
    
    private Map<String, Integer> orders;

    public ShoppingBasket() {
        this.orders = new HashMap<>();
    }

    public Map<String, Integer> getOrders() {
        // clone the set to prevent external modification
        return new HashMap<>(orders);
    }

    public void addOrder(String productId, Integer quantity) {
        if (orders.containsKey(productId)) {
            orders.put(productId,orders.get(productId) + quantity); 
        } 
        else {
            orders.put(productId, quantity);
        }
    }

    public void removeItem(String productId, int quantity) {
        orders.put(productId, orders.get(productId) - quantity);
        if (orders.get(productId) <= 0) {
            orders.remove(productId);
        }
    }

    public void removeItem(String productId){
        orders.remove(productId);
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public void clear() {
        orders.clear();
    }

    
    // Returns the quantity of a specific product in the basket
    // If the product is not in the basket, it returns 0
    public int getProduct(String productId) {
        return orders.getOrDefault(productId, 0);
    }

    public int getQuantity(){
        int total = 0;
        for(int quantity : orders.values()){
            total += quantity;
        }  
        return total;
    }


}
