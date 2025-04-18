package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;

public class ShoppingBasket implements IShoppingBasket {
    
    private String clientId;
    private String storeId;
    private Map<String, Integer> products;

    public ShoppingBasket(String clientId, String storeId) {
        this.clientId = clientId;
        this.storeId = storeId;
        this.products = new HashMap<>();
    }

    public String getClientId() {
        return clientId;
    }

    public String getStoreId() {
        return storeId;
    }

    public Map<String, Integer> getProducts() {
        return new HashMap<>(products);
    }

    public void addItem(String productId, int quantity) {
        if (products.containsKey(productId)) {
            products.put(productId, products.get(productId) + quantity);
        } else {
            products.put(productId, quantity);
        }
    }

    public void removeItem(String productId, int quantity) {
        products.put(productId, products.get(productId) - quantity);
        if (products.get(productId) <= 0) {
            products.remove(productId);
        }
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public void clear() {
        products.clear();
    }

        
    @Override
    public Map<String, Integer> getItems() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getItems'");
    }

    @Override
    public boolean areIdentical(IShoppingBasket storeBasket) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'areIdentical'");
    }

    @Override
    public Map<Integer, IShoppingBasket> getShoppingBaskets() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShoppingBaskets'");
    }

    @Override
    public void addShoppingBasket(IShoppingBasket basket, String userName, double price) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addShoppingBasket'");
    }


}
