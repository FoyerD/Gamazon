package Domain.Shopping;

public interface IShoppingBasketRepository {

    public ShoppingBasket getById(int id); 

    public void add(ShoppingBasket shoppingBasket);
    
    public void remove(ShoppingBasket shoppingBasket);
}
