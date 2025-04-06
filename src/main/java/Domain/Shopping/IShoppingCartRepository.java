package Domain.Shopping;

public interface IShoppingCartRepository {

    public ShoppingCart getById(int id); 

    public void add(ShoppingCart shoppingCart);
    
    public void remove(ShoppingCart shoppingCart);
}
