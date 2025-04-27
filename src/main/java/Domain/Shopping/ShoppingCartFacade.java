package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import Domain.Pair;
import Domain.ExternalServices.IPaymentService;
import Domain.Store.ItemFacade;
import Domain.Store.StoreFacade;

public class ShoppingCartFacade implements IShoppingCartFacade {
    private final IShoppingCartRepository cartRepo;
    private final IShoppingBasketRepository basketRepo;
    private final IReceiptRepository receiptRepo;
        private final IPaymentService paymentService;
    private final ItemFacade itemFacade;
    private final StoreFacade storeFacade;


    public ShoppingCartFacade(IShoppingCartRepository cartRepo, IShoppingBasketRepository basketRepo, IReceiptRepository receiptRepo, IPaymentService paymentService, ItemFacade itemFacade, StoreFacade storeFacade) {

        this.cartRepo = cartRepo;
        this.basketRepo = basketRepo;
        this.paymentService = paymentService;
        this.itemFacade = itemFacade;
        this.receiptRepo = receiptRepo;
        this.storeFacade = storeFacade;

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
    public boolean makeBid(String auctionId, String clientId, float price) {
        storeFacade.addBid(auctionId, clientId, price);
        return true;
    }


    @Override
    public boolean checkout(String clientId, String card_number, Date expiry_date, String cvv,
                        long andIncrement, String clientName, String deliveryAddress) {
        IShoppingCart cart = getCart(clientId);

        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        Map<Pair<String, String>, Integer> itemsrollbackData = new HashMap<>(); // To store rollback information
        Set<ShoppingBasket> basketsrollbackdata = new HashSet<>();
        Set<String> cartrollbackdata = new HashSet<>();
        
        // Store purchase information for receipt creation
        Map<String, Map<String, Object>> purchaseData = new HashMap<>();

        try {
            // Iterate over all stores in the cart
            double totalPrice = 0;
            for (String storeId : cart.getCart()) {
                ShoppingBasket basket = basketRepo.get(new Pair<>(clientId, storeId));
                if (basket != null) {
                    // Prepare store purchase data
                    Map<String, Object> storePurchase = new HashMap<>();
                    Map<String, Integer> storeProducts = new HashMap<>();
                    double storeTotalPrice = 0;
                    
                    // Iterate over all items in the basket
                    for (Map.Entry<String, Integer> entry : basket.getOrders().entrySet()) {
                        String productId = entry.getKey();
                        int quantity = entry.getValue();

                        // Attempt to decrease the item quantity
                        itemFacade.decreaseAmount(new Pair<String,String>(storeId, productId), quantity);
                        
                        // Get product price and calculate totals
                        double productPrice = itemFacade.getItem(storeId, productId).getPrice();
                        double productTotal = productPrice * quantity;
                        storeTotalPrice += productTotal;
                        totalPrice += productTotal;

                        // Store product info for receipt
                        storeProducts.put(productId, quantity);
                        
                        // Store rollback data
                        itemsrollbackData.put(new Pair<>(storeId, productId), quantity);
                    }

                    // Store the purchase data for later receipt creation
                    storePurchase.put("products", storeProducts);
                    storePurchase.put("totalPrice", storeTotalPrice);
                    purchaseData.put(storeId, storePurchase);

                    // Mark the basket as checked out
                    basketsrollbackdata.add(basket);
                    basket.clear();
                    basketRepo.update(new Pair<>(clientId, storeId), basket);
                }
            }

            // Process payment
            paymentService.processPayment(clientName, card_number, expiry_date, cvv, 
                                        totalPrice, andIncrement, clientName, deliveryAddress);

            // Clear the cart
            cartrollbackdata.addAll(cart.getCart());
            cart.clear();
            cartRepo.update(clientId, cart);
            
            // Now that everything has succeeded, create the receipt records
            for (Map.Entry<String, Map<String, Object>> entry : purchaseData.entrySet()) {
                String storeId = entry.getKey();
                Map<String, Object> storePurchase = entry.getValue();
                
                Map<String, Integer> products = (Map<String, Integer>) storePurchase.get("products");
                double storeTotal = (double) storePurchase.get("totalPrice");
                
                // Save the receipt
                receiptRepo.savePurchase(clientId, storeId, products, storeTotal);
            }

            return true;
        } catch (Exception e) {
            // Rollback: Increase the quantities back
            checkoutRollBack(clientId, cart, itemsrollbackData, cartrollbackdata, basketsrollbackdata);
            // Throw the exception to indicate failure
            throw new RuntimeException("Checkout failed: " + e.getMessage(), e);
            
        }
    }


    private void checkoutRollBack(String clientId, IShoppingCart cart, Map<Pair<String, String>, Integer> itemsrollbackData, Set<String> cartrollbackdata, Set<ShoppingBasket> basketsrollbackdata) {
        for (Map.Entry<Pair<String, String>, Integer> entry : itemsrollbackData.entrySet()) {
            Pair<String, String> key = entry.getKey();
            String storeId = key.getFirst();
            String productId = key.getSecond();
            int quantity = entry.getValue();

            itemFacade.increaseAmount(new Pair<String,String>(storeId, productId), quantity);
        }

        for (ShoppingBasket basket : basketsrollbackdata) {
            // Re-add the items to the basket
            for (Map.Entry<String, Integer> entry : basket.getOrders().entrySet()) {
                String productId = entry.getKey();
                int quantity = entry.getValue();

                // Re-add the item to the basket
                basket.addOrder(productId, quantity);
            }
            basketRepo.update(new Pair<>(clientId, basket.getStoreId()), basket);
        }
        
        for (String storeId : cartrollbackdata) {
            // Re-add the store to the cart
            cart.addStore(storeId);
        }
        cartRepo.update(clientId, cart);
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
            for (String storeId : cart.getCart()) {
                clearBasket(clientId, storeId);
            }
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
