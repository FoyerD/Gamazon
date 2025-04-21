package Domain.Shopping;

import Domain.Store.Item;

public class ShoppingCartFacade implements IShoppingCartFacade {
    private final IShoppingCartRepository cartRepo;
    private final IShoppingBasketRepository basketRepo;

    public ShoppingCartFacade(IShoppingCartRepository cartRepo) {
        this.cartRepo = cartRepo;
        this.basketRepo = new ShoppingBasketRepository();
    }

    @Override
    public IShoppingCart getCart(String clientId) {
        IShoppingCart cart = cartRepo.get(clientId);
        if (cart == null) {
            cart = new ShoppingCart(clientId);
        }
        return cart;
    }


    @Override
    public void addProductToCart(String storeId, String clientId, String productId, int quantity) {
        IShoppingCart cart = getCart(clientId);
        ShoppingBasket basket = cart.getBasket(storeId);
        if (basket == null) {
            basket = new ShoppingBasket();
            basket.addOrder(productId, quantity);
        }
        cart.addItem(storeId, productId, quantity);
        cartRepo.add(cart);
        basketRepo.add(basket);
    }


    @Override
    public void removeProductFromCart(String storeId, String clientId, String productId, int quantity) {
        IShoppingCart cart = getCart(clientId);
        ShoppingBasket basket = cart.getBasket(storeId);
        if (basket == null) {
            throw new IllegalArgumentException("No orders in this store");
        }

        cart.removeItem(storeId, productId, quantity);
        
        cartRepo.add(cart);
        basketRepo.add(basket);
    }

    @Override
    public void removeProductFromCart(String storeId, String clientId, String productId) {
        IShoppingCart cart = getCart(clientId);
        ShoppingBasket basket = cart.getBasket(storeId);
        if (basket == null) {
            throw new IllegalArgumentException("No orders in this store");
        }

        cart.removeItem(storeId, productId);
        cartRepo.add(cart);
        basketRepo.add(basket);
    }

    @Override
    public void checkout(String clientId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkout'");
    }
}
