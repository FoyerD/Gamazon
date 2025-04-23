package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;

import Domain.Pair;

public class ShoppingCartFacade implements IShoppingCartFacade {
    private final IShoppingCartRepository cartRepo;
    private final IShoppingBasketRepository basketRepo;

    public ShoppingCartFacade(IShoppingCartRepository cartRepo, IShoppingBasketRepository basketRepo) {
        this.cartRepo = cartRepo;
        this.basketRepo = basketRepo;
    }

    private IShoppingCart getCart(String clientId) {
        IShoppingCart cart = cartRepo.get(clientId);
        if (cart == null) {
            cart = new ShoppingCart(clientId);
            cartRepo.add(clientId, cart);
        }
        return cart;
    }

    private ShoppingBasket getBasket(String clientId, String storeId) {
        ShoppingBasket basket = basketRepo.get(new Pair<>(clientId, storeId));
        if (basket == null) {
            basket = new ShoppingBasket(storeId, clientId);
            basketRepo.add(new Pair<>(clientId, storeId), basket);
        }
        return basket;
    }
    
    @Override
    public boolean addProductToCart(String storeId, String clientId, String productId, int quantity) {
        IShoppingCart cart = getCart(clientId);
        ShoppingBasket basket = getBasket(clientId, storeId);
        if (basket == null) {
            basket = new ShoppingBasket(storeId, clientId);
        }
        basket.addOrder(productId, quantity);
        basketRepo.update(new Pair<>(clientId, storeId), basket);

        if (!cart.hasStore(storeId)) {
            cart.addStore(storeId);
            cartRepo.add(clientId, cart);
        }

        return true;
    }


    @Override
    public boolean removeProductFromCart(String storeId, String clientId, String productId, int quantity) {
        IShoppingCart cart = getCart(clientId);
        if (!cart.hasStore(storeId)) {
            throw new RuntimeException("Store not found in cart");
        }
        
        ShoppingBasket basket = getBasket(clientId, storeId);
        basket.removeItem(productId, quantity);
        basketRepo.add(new Pair<>(clientId, storeId), basket);
        
        if (basket.isEmpty()) {
            cart.removeStore(storeId);
            cartRepo.update(clientId, cart);
        }

        return true;
    }

    @Override
    public boolean removeProductFromCart(String storeId, String clientId, String productId) {
        IShoppingCart cart = getCart(clientId);
        if (!cart.hasStore(storeId)) {
            throw new RuntimeException("Store not found in cart");
        }
        
        ShoppingBasket basket = getBasket(clientId, storeId);
        basket.removeItem(productId);
        basketRepo.add(new Pair<>(clientId, storeId), basket);
        
        if (basket.isEmpty()) {
            cart.removeStore(storeId);
            cartRepo.update(clientId, cart);
        }

        return true;
    }

    @Override
    public boolean checkout(String clientId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkout'");
    }

    @Override
    public int getTotalItems(String clientId) {
        IShoppingCart cart = getCart(clientId);

        int total = 0;
        for(String storeId : cart.getCart()){
            ShoppingBasket basket = basketRepo.get(new Pair<>(clientId, storeId));
            if (basket == null) {
                continue; // Skip if basket is not found
            }
            total += basket.getQuantity();
        }
        return total;
    }

    @Override
    public boolean isEmpty(String clientId) {
        IShoppingCart cart = getCart(clientId);
        if (cart == null || cart.isEmpty()) {
            return true;
        }
        return getTotalItems(clientId) == 0;
    }

    @Override
    public boolean clearCart(String clientId) {
        IShoppingCart cart = getCart(clientId);
        if (cart != null) {
            cart.clear();
            cartRepo.update(clientId, cart);
        
            return true;
        }
        
        return false;
    }

    @Override
    public boolean clearBasket(String clientId, String storeId) {
        ShoppingBasket basket = getBasket(clientId, storeId);
        if (basket != null) {
            basket.clear();
            basketRepo.update(new Pair<>(clientId, storeId), basket);

            return true;
        }

        return false;
    }

    @Override
    public Map<String, Map<String, Integer>> viewCart(String clientId) {
        IShoppingCart userCart = getCart(clientId);
        if (userCart == null) {
            return new HashMap<>(); // Return empty map if cart is not found
        }
        Map<String, Map<String, Integer>> viewCart = new HashMap<String,Map<String,Integer>>();
        for(String storeId : userCart.getCart()) {
            ShoppingBasket basket = getBasket(clientId, storeId);
            if (basket == null) {
                continue; // Skip if basket is not found
            }
            viewCart.put(storeId, basket.getOrders());
        }
        return viewCart;
    }
}
