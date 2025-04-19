package Domain.Shopping;

import java.util.HashSet;
import java.util.Set;

public class ShoppingCart implements IShoppingCart {

    private String clientId;
    private Set<IShoppingBasket> baskets;

    public ShoppingCart(String clientId) {
        this.clientId = clientId;
        this.baskets = new HashSet<>();
    }


    public ShoppingCart(String clientId, Set<IShoppingBasket> baskets) {
        this.clientId = clientId;
        this.baskets = baskets;
    }

    public String getClientId() {
        return clientId;
    }


    public void Checkout() {
        throw new UnsupportedOperationException("Not implemented yet!!");
    }


    @Override
    public void addItem(String shopId, String productId, int quantity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addItem'");
    }


    @Override
    public void removeItem(String shopId, String productId, int quantity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeItem'");
    }


    @Override
    public int getProduct(String shopId, String productId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProduct'");
    }


    @Override
    public void removeItem(String shopId, String productId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeItem'");
    }


    @Override
    public void clear() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }


    @Override
    public double getTotalPrice() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTotalPrice'");
    }


    @Override
    public int getTotalItems() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTotalItems'");
    }


    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }


    @Override
    public Set<IShoppingBasket> getStoreBaskets() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStoreBaskets'");
    }

    
}
