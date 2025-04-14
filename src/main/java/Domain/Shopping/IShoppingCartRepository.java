package Domain.Shopping;

public interface IShoppingCartRepository {
    public IShoppingCart getById(String clientId); 
    public void add(IShoppingCart shoppingCart);
    public void remove(IShoppingCart shoppingCart);
}
