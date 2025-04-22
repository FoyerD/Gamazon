package Domain.Shopping;

public interface IShoppingCartRepository {
    public IShoppingCart get(String clientId); 
    public void add(IShoppingCart shoppingCart);
    public IShoppingCart remove(String clientId);
    public boolean contains(String clientId);
    public void clear();
    public void update(IShoppingCart shoppingCart);
}
