package Domain.Shopping;

public interface IShoppingBasketRepository {
    public ShoppingBasket get(String clientId, String storeId); 
    public void add(ShoppingBasket shoppingBasket);
    public ShoppingBasket remove(String clientId, String storeId);
    public boolean contains(String clientId, String storeId);
    public void clear();
    public void update(ShoppingBasket shoppingBasket);
}
