package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;

import Domain.Pair;

public class ShoppingCartFacade implements IShoppingCartFacade {
    private final IShoppingCartRepository cartRepo;
    private final IShoppingBasketRepository basketRepo;
    private final IReceiptRepository receiptRepo;
    
    public ShoppingCartFacade(IShoppingCartRepository cartRepo, IShoppingBasketRepository basketRepo, IReceiptRepository receiptRepo) {
        this.cartRepo = cartRepo;
        this.basketRepo = basketRepo;
        this.receiptRepo = receiptRepo;
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
        // Get the client's shopping cart
        IShoppingCart cart = cartRepo.get(clientId);
        if (cart == null || cart.isEmpty()) {
            return false;
        }
        
        boolean anyPurchaseMade = false;
        
        // Process each store's basket
        for (String storeId : cart.getCart()) {
            // Get the basket for this store
            Pair<String, String> basketId = new Pair<>(clientId, storeId);
            ShoppingBasket basket = basketRepo.get(basketId);
            
            if (basket != null && !basket.isEmpty()) {
                // This would typically involve additional steps like:
                // 1. Payment processing
                // 2. Inventory updates
                // 3. Order creation
                
                // Get the products from the basket
                Map<String, Integer> products = new HashMap<>(basket.getBasketProducts());
                
                // Calculate the total price (this is a simplified example)
                // In a real implementation, you would get prices from a product service
                double totalPrice = calculateTotalPrice(products, storeId);
                
                // Save the purchase record in the receipt repository
                receiptRepo.savePurchase(clientId, storeId, products, totalPrice);
                
                // Clear the basket for this store
                basket.clear();
                basketRepo.update(basketId, basket);
                
                anyPurchaseMade = true;
            }
        }
        
        // Clear the entire cart if any purchase was made
        if (anyPurchaseMade) {
            cart.clear();
            cartRepo.update(clientId, cart);
            return true;
        }
        
        return false;
    }
    
    // Simplified method to calculate total price
    // In a real implementation, you would get prices from a product service
    private double calculateTotalPrice(Map<String, Integer> products, String storeId) {
        // This is a placeholder - you would replace with actual price calculation
        // using a product service or other mechanism to get prices
        return products.values().stream().mapToDouble(quantity -> quantity * 10.0).sum();
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
