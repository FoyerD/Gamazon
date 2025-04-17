package Domain.Shopping;

public class ShoppingCartFacade implements IShoppingCartFacade {
    private final IShoppingCartRepository cartRepo;

    public ShoppingCartFacade(IShoppingCartRepository cartRepo) {
        this.cartRepo = cartRepo;
    }

    @Override
    public IShoppingCart getCart(String clientId) {
        IShoppingCart cart = cartRepo.getCart(clientId);
        if (cart == null) {
            cart = new ShoppingCart(clientId);
            cartRepo.saveCart(cart);
        }
        return cart;
    }

    @Override
    public IShoppingBasket getBasket(String clientId, String storeId) {
        IShoppingCart cart = cartRepo.getCart(clientId);
        if (cart == null) return null;
        return cart.getBasketByStoreId(storeId);
    }

    @Override
    public void addProductToCart(PurchaseInfo info) {
        IShoppingCart cart = getCart(info.getClientId());
        IShoppingBasket basket = cart.getBasketByStoreId(info.getStoreId());
        if (basket == null) {
            basket = new ShoppingBasket(info.getStoreId());
            cart.addBasket(basket);
        }
        basket.addItem(info.getProductId(), info.getQuantity());
        cartRepo.saveCart(cart);
    }
}
