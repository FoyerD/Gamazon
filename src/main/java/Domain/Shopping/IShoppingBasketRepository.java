package Domain.Shopping;

public interface IShoppingBasketRepository {
    public IShoppingBasket get(String clientId, String storeId); 
    public void add(IShoppingBasket shoppingBasket);
    public IShoppingBasket remove(String clientId, String storeId);
    public boolean contains(String clientId, String storeId);
    public void clear();
}
