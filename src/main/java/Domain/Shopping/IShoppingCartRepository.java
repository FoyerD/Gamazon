package Domain.Shopping;

public interface IShoppingCartRepository {
<<<<<<< HEAD
    public IShoppingCart get(String clientId); 
    public void add(IShoppingCart shoppingCart);
    public void remove(IShoppingCart shoppingCart);
=======

    public ShoppingCart getById(int id); 

    public void add(ShoppingCart shoppingCart);
    
    public void remove(ShoppingCart shoppingCart);
>>>>>>> 69407b79cb73b73fa33cd3a3be86c40c1091e0dc
}
