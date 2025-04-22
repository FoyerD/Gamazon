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
            cartRepo.add(cart);
        }
        return cart;
    }

    @Override
    public ShoppingBasket getBasket(String clientId, String storeId) {
        ShoppingBasket basket = basketRepo.get(clientId, storeId);
        if (basket == null) {
            basket = new ShoppingBasket(storeId, clientId);
            basketRepo.add(basket);
        }
        return basket;
    }
    
    @Override
    public void addProductToCart(String storeId, String clientId, String productId, int quantity) {
        IShoppingCart cart = getCart(clientId);
        ShoppingBasket basket = getBasket(clientId, storeId);
        if (basket == null) {
            basket = new ShoppingBasket(storeId, clientId);
        }
        basket.addOrder(productId, quantity);
        basketRepo.update(basket);

        if (!cart.hasStore(storeId)) {
            cart.addStore(storeId);
            cartRepo.add(cart);
        }
    }


    @Override
    public void removeProductFromCart(String storeId, String clientId, String productId, int quantity) {
        IShoppingCart cart = getCart(clientId);
        if (!cart.hasStore(storeId)) {
            throw new IllegalArgumentException("No orders in this store");
        }
        
        ShoppingBasket basket = getBasket(clientId, storeId);
        basket.removeItem(productId, quantity);
        basketRepo.add(basket);
        
        if (basket.isEmpty()) {
            cart.removeStore(storeId);
            cartRepo.update(cart);
        }
    }

    @Override
    public void removeProductFromCart(String storeId, String clientId, String productId) {
        IShoppingCart cart = getCart(clientId);
        if (!cart.hasStore(storeId)) {
            throw new IllegalArgumentException("No orders in this store");
        }
        
        ShoppingBasket basket = getBasket(clientId, storeId);
        basket.removeItem(productId);
        basketRepo.add(basket);
        
        if (basket.isEmpty()) {
            cart.removeStore(storeId);
            cartRepo.update(cart);
        }
    }

    @Override
    public void checkout(String clientId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkout'");
    }
}
