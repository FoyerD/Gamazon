package Domain.Shopping;

public class ShoppingCartFacade implements IShoppingCartFacade {
    private final IShoppingCartRepository cartRepo;

    public ShoppingCartFacade(IShoppingCartRepository cartRepo) {
        this.cartRepo = cartRepo;
    }

    @Override
    public IShoppingCart getCart(String clientId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public IShoppingBasket getBasket(String clientId, String storeId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void addProductToCart(PurchaseInfo info) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
