package Domain.Shopping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import Domain.Store.IStoreRepository;
import Domain.Store.Item;

// TODO: Auto-generated Code
public class ShoppingBasket implements IShoppingBasket {
    private IShoppingCart shoppingCart;
    private IStoreRepository storeFacade;

    public ShoppingBasket(IShoppingCart shoppingCart, IStoreRepository storeFacade) {
        this.shoppingCart = shoppingCart;
        this.storeFacade = storeFacade;
    }

    public ShoppingBasket() {
        // Default constructor
    }

    @Override
    public boolean areIdentical(IShoppingBasket storeBasket) {
        // Implement the logic to compare two shopping baskets
        return false; // Placeholder return value
    }
   
    public IShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(IShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    public IStoreRepository getStoreFacade() {
        return storeFacade;
    }

    public void setStoreFacade(IStoreRepository storeFacade) {
        this.storeFacade = storeFacade;
    }

    public void setShoppingBasketCount(int count) {
        // This method is not needed in the current context
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getShoppingBasketCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IShoppingBasket getShoppingBasket(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clean() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addShoppingBasket(IShoppingBasket basket, String userName, double price) {
        // TODO Auto-generated method stub
        
    }

    @Override   
    public List<IShoppingBasket> getUserShoppingBasketsInRange(String userName, LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public List<IShoppingBasket> getStoreShoppingBaskets(int storeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IShoppingBasket> getStoreShoppingBasketsInRange(int storeId, LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Map<Integer, IShoppingBasket> getShoppingBaskets() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Item> getItems() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int getStoreId() {
        // TODO Auto-generated method stub
        return 0;
    }
}
