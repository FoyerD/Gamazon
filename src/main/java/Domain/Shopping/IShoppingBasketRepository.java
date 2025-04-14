package Domain.Shopping;

public interface IShoppingBasketRepository {
    public IShoppingBasket getById(int id); 
    public void add(IShoppingBasket shoppingBasket);
    public void remove(IShoppingBasket shoppingBasket);
}
