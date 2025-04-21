package Domain.Shopping;
import Domain.Store.Item;

public class ShoppingCartFacade implements IShoppingCartFacade {
    private final IShoppingCartRepository cartRepo;

    public ShoppingCartFacade(IShoppingCartRepository cartRepo) {
        this.cartRepo = cartRepo;
    }

    @Override
    public IShoppingCart getCart(String clientId) {
        IShoppingCart cart = cartRepo.get(clientId);
        if (cart == null) {
            cart = new ShoppingCart(clientId);
            cartRepo.saveCart(cart);
        }
        return cart;
    }

    @Override
    public IShoppingBasket getBasket(String clientId, String storeId) {
        IShoppingCart cart = cartRepo.get(clientId);
        if (cart == null) return null;
        return cart.getBasketByStoreId(storeId);
    }

    @Override
    public void addProductToCart(Item item) {
        IShoppingCart cart = getCart(info.getClientId());
        IShoppingBasket basket = cart.getBasketByStoreId(info.getStoreId());
        if (basket == null) {
            basket = new ShoppingBasket(info.getStoreId());
            cart.addBasket(basket);
        }
        basket.addItem(info.getProductId(), info.getQuantity());
        cartRepo.saveCart(cart);
    }

    public void addProductToCart(String clientId, Item item) {
        IShoppingCart cart = getCart(clientId);
        cart.removeItem(item.getStoreId(), item.getProductId(), item.getAmount());
        cartRepo.saveCart(cart);
    }

    @Override
    public void removeProductFromCart(String clientId, Item item) {
        IShoppingCart cart = getCart(clientId);
        cart.removeItem(item.getStoreId(), item.getProductId(), item.getAmount());
        cartRepo.saveCart(cart);
    }
}

    @Override
    public void makeImmediatePurchase(String clientId, Item item) {
        IShoppingCart cart = getCart(clientId);
        IShoppingBasket basket = cart.getBasketByStoreId(item.getStoreId());
        if (basket == null) {
            basket = new ShoppingBasket(item.getStoreId());
            cart.addBasket(basket);
        }
        basket.addItem(item.getProductId(), item.getAmount());
        cartRepo.saveCart(cart);
        cart.checkout(item.getStoreId(), item.getProductId(), item.getAmount());
    }
}
