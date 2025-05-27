package Domain.Shopping;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.Pair;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.Policy;
import Domain.Store.Product;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;
import Domain.management.PolicyFacade;
import Domain.Store.IProductRepository;
import Domain.User.Member;

/**
 * Implementation of the IShoppingCartFacade interface.
 * Orchestrates interactions between shopping carts, baskets, payment services, and item management.
 */
@Component
public class ShoppingCartFacade implements IShoppingCartFacade {
    private final IShoppingCartRepository cartRepo;
    private final IShoppingBasketRepository basketRepo;
    private final IReceiptRepository receiptRepo;
    private final IExternalPaymentService paymentService;
    private final ItemFacade itemFacade;
    private final StoreFacade storeFacade;
    private final IProductRepository productRepo;
    private final PolicyFacade policyFacade;
    private final Function<String, Member> memberLookup;

    /**
     * Constructor to initialize the ShoppingCartFacade with required repositories and services.
     * 
     * @param cartRepo The repository for shopping carts
     * @param basketRepo The repository for shopping baskets
     * @param paymentService The payment service for processing payments
     * @param itemFacade The facade for item management
     * @param storeFacade The facade for store management
     * @param receiptRepo The repository for receipts
     * @param productRepository The repository for products
     * @param policyFacade The facade for policy management
     */
    @Autowired
    public ShoppingCartFacade(IShoppingCartRepository cartRepo, IShoppingBasketRepository basketRepo, IExternalPaymentService paymentService, ItemFacade itemFacade, StoreFacade storeFacade, IReceiptRepository receiptRepo, IProductRepository productRepository, PolicyFacade policyFacade, IUserRepository userRepository) {
        this.cartRepo = cartRepo;
        this.basketRepo = basketRepo;
        this.paymentService = paymentService;
        this.itemFacade = itemFacade;
        this.storeFacade = storeFacade;
        this.receiptRepo = receiptRepo;
        this.productRepo = productRepository;
        this.policyFacade = policyFacade;
        this.memberLookup = userRepository::getMember;
    }

    /**
     * Retrieves a shopping cart for a specific client, creating a new one if it doesn't exist.
     * 
     * @param clientId The ID of the client
     * @return The shopping cart for the specified client
     */
    private IShoppingCart getCart(String clientId) {
        IShoppingCart cart = cartRepo.get(clientId);
        if (cart == null) {
            cart = new ShoppingCart(clientId);
            cartRepo.add(clientId, cart);
        }
        return cart;
    }

    /**
     * Retrieves a shopping basket for a specific client and store, creating a new one if it doesn't exist.
     * 
     * @param clientId The ID of the client
     * @param storeId The ID of the store
     * @return The shopping basket for the specified client and store
     */
    private ShoppingBasket getBasket(String clientId, String storeId) {
        ShoppingBasket basket = basketRepo.get(new Pair<>(clientId, storeId));
        if (basket == null) {
            basket = new ShoppingBasket(storeId, clientId);
            basketRepo.add(new Pair<>(clientId, storeId), basket);
        }
        return basket;
    }
    
    /**
     * Adds a product to the client's shopping cart.
     * 
     * @param storeId The ID of the store where the product is sold
     * @param clientId The ID of the client adding the product
     * @param productId The ID of the product being added
     * @param quantity The amount of the product to add
     * @return true if the product was successfully added, false otherwise
     */
    @Override
    public boolean addProductToCart(String storeId, String clientId, String productId, int quantity) {
        
        // Check for valid arguments
        if (clientId == null || storeId == null || productId == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid arguments for adding product to cart");
        }
        // Check if the product exists in the store
        if (itemFacade.getItem(storeId, productId) == null) {
            throw new IllegalArgumentException("Product not found in store");
        }
        // Check if the store exists
        if (storeFacade.getStore(storeId) == null) {
            throw new IllegalArgumentException("Store not found");
        }
        
        
        IShoppingCart cart = getCart(clientId);
        ShoppingBasket basket = getBasket(clientId, storeId);
        
        basket.addOrder(productId, quantity);
        basketRepo.update(new Pair<>(clientId, storeId), basket);

        if (!cart.hasStore(storeId)) {
            cart.addStore(storeId);
            // Fix 1: Using update instead of add
            cartRepo.update(clientId, cart);
        }

        return true;
    }

    /**
     * Removes a specific quantity of a product from the client's cart.
     * 
     * @param storeId The ID of the store where the product is sold
     * @param clientId The ID of the client removing the product
     * @param productId The ID of the product being removed
     * @param quantity The amount of the product to remove
     * @return true if the product was successfully removed, false otherwise
     * @throws RuntimeException if the store is not found in the cart
     */
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

    /**
     * Completely removes a product from the client's cart regardless of quantity.
     * 
     * @param storeId The ID of the store where the product is sold
     * @param clientId The ID of the client removing the product
     * @param productId The ID of the product being removed
     * @return true if the product was successfully removed, false otherwise
     * @throws RuntimeException if the store is not found in the cart
     */
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

    /**
     * Places a bid on the specified auction with deferred payment details.
     * The bid will be stored along with the provided payment information.
     * If the bid is later accepted by a store manager, the payment will be processed automatically.
     *
     * @param auctionId The ID of the auction to bid on
     * @param clientId The ID of the client placing the bid
     * @param price The amount of the bid
     * @param cardNumber The client's credit card number (used for payment if bid is accepted)
     * @param expiryDate The expiration date of the credit card
     * @param cvv The CVV code of the credit card
     * @param andIncrement An identifier used for secure payment tracking
     * @param clientName The name of the client (used for billing)
     * @param deliveryAddress The address for delivery if the bid is accepted
     * @return true if the bid was successfully placed
     */
    @Override
    public boolean makeBid(String auctionId, String clientId, float price,
                        String cardNumber, Date expiryDate, String cvv,
                        long andIncrement, String clientName, String deliveryAddress) {

        Supplier<Boolean> chargeSupplier = () -> {
            if (!isCardNumber(cardNumber)) {
                throw new IllegalArgumentException("Invalid card number");
            }

            paymentService.processPayment(
                clientId, cardNumber, expiryDate, cvv, 
                clientName, price
            );

            return true;
        };

        storeFacade.addBid(auctionId, clientId, price, chargeSupplier);
        return true;
    }


    @Override
    public boolean checkout(String clientId, String card_number, Date expiry_date, String cvv,
                long andIncrement, String clientName, String deliveryAddress) {
    
    
    // check for valid arguments
    if (clientId == null || card_number == null || expiry_date == null || cvv == null) {
        throw new IllegalArgumentException("Invalid arguments for checkout");
    }
    if (!isCardNumber(card_number)){
        throw new IllegalArgumentException("Invalid card number");
    }

    IShoppingCart cart = getCart(clientId);

    Map<Pair<String, String>, Integer> itemsrollbackData = new HashMap<>(); // To store rollback information
    Set<ShoppingBasket> basketsrollbackdata = new HashSet<>();
    Set<String> cartrollbackdata = new HashSet<>();
    
    // Store purchase information for receipt creation
    Map<String, Map<Product, Integer>> storeProductsMap = new HashMap<>();

    Response<Integer> paymentResponse = new Response<>(-1);

    try {
        // Iterate over all stores in the cart
        boolean purchaseSuccess = false;
        double totalPrice = 0;
        Set<String> storeIds = cart.getCart();
        
        // Added null check for cart.getCart()
        if (storeIds != null) {
            for (String storeId : storeIds) {
                ShoppingBasket basket = basketRepo.get(new Pair<>(clientId, storeId));
                if (basket != null && !basket.isEmpty()) {
                    // Check if cart abids by policies
                    List<Policy> policies = policyFacade.getAllStorePolicies(storeId);
                    Member member = this.memberLookup.apply(clientId);
                    for (Policy policy : policies) {
                        if (!policy.isApplicable(basket, member)) {
                            throw new RuntimeException("Checkout failed: Basket does not comply with store policies");
                        }
                    }


                    // Track products and prices for this store
                    Map<Product, Integer> storeProducts = new HashMap<>();
                    
                    // Added null check for basket.getOrders()
                    Map<String, Integer> orders = basket.getOrders();
                    if (orders != null) {
                        // Fix: Wrap everything in try/catch to handle any potential NPEs due to null collections
                        try {
                            // Iterate over all items in the basket with defensive coding
                            for (Map.Entry<String, Integer> entry : orders.entrySet()) {
                                if (entry != null) {
                                    String productId = entry.getKey();
                                    Integer quantityObj = entry.getValue();
                                    
                                    if (productId != null && quantityObj != null) {
                                        int quantity = quantityObj;
                                        
                                        // Attempt to decrease the item quantity
                                        itemFacade.decreaseAmount(new Pair<>(storeId, productId), quantity);
                                        
                                        // Get the product object and calculate price
                                        Product product = productRepo.get(productId);
                                        if (product != null) {
                                            // Create a new product object to avoid modifying the original
                                            Product productCopy = new Product(product);
                                            Item item = itemFacade.getItem(storeId, productCopy.getProductId());
                                            if (item != null) {
                                                purchaseSuccess = true;
                                                double productPrice = item.getPrice() * quantity;
                                                totalPrice += productPrice;
                                                
                                                // Store product in the store's product map
                                                storeProducts.put(productCopy, quantity);
                                                
                                                // Store rollback data
                                                itemsrollbackData.put(new Pair<>(storeId, productCopy.getProductId()), quantity);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (NullPointerException e) {
                            // Catch any NPEs that might occur due to null collections
                            // Log the error and continue with empty collections
                            System.err.println("Caught NPE during checkout processing: " + e.getMessage());
                        }
                    }

                    // Store products for this store
                    if (!storeProducts.isEmpty()) {
                        storeProductsMap.put(storeId, storeProducts);
                    }
                    
                    // Mark the basket as checked out
                    basketsrollbackdata.add(basket);
                    basket.clear();
                    basketRepo.update(new Pair<>(clientId, storeId), basket);
                }
            }
        }
        // Process payment only if there are items to checkout
        if (purchaseSuccess) {
            // Call payment service and check for error response
            paymentResponse = paymentService.processPayment(
                clientId, card_number, expiry_date, cvv, 
                clientName, totalPrice
            );

            
            if (paymentResponse == null) {
                throw new RuntimeException("Payment failed: service returned null response");
            }
            
            if(paymentResponse.getValue() == null)
                throw new RuntimeException("Payment failed: value is null");

            // If payment service returned an error, throw an exception to trigger rollback
            if (paymentResponse.errorOccurred()) {
                throw new RuntimeException(paymentResponse.getErrorMessage());
            }
        }

        // Clear the cart
        if (storeIds != null) {
            cartrollbackdata.addAll(storeIds);
        }
        cart.clear();
        cartRepo.update(clientId, cart);
        
        // Create receipts only if there was a purchase
        if (purchaseSuccess) {
            // Create masked payment details (only show last 4 digits of card)
            String maskedCardNumber = "xxxx-xxxx-xxxx-" + card_number.substring(card_number.length() - 4);
            String paymentDetails = "Card: " + maskedCardNumber;
            
            // Now that everything has succeeded, create the receipt records
            for (Map.Entry<String, Map<Product, Integer>> entry : storeProductsMap.entrySet()) {
                String storeId = entry.getKey();
                Map<Product, Integer> products = entry.getValue();
                
                // Calculate store-specific total safely with null checks
                double storeTotal = 0.0;
                Map<Product, Pair<Integer, Double>> productPrices = new HashMap<>();
                if (products != null) {

                    for (Map.Entry<Product, Integer> productEntry : products.entrySet()) {
                        if (productEntry.getKey() != null && productEntry.getValue() != null) {
                            Product product = productEntry.getKey();
                            int qty = productEntry.getValue();
                            Item item = itemFacade.getItem(storeId, product.getProductId());
                            if (item != null) {
                                productPrices.put(product, new Pair<>(qty, item.getPrice()));
                                storeTotal += item.getPrice() * qty;
                            }
                        }
                    }
                }
                
                // Create and save receipt
                receiptRepo.savePurchase(clientId, storeId, productPrices, storeTotal, paymentDetails);
            }
        }

        return true;
    } catch (Exception e) {
        // Rollback all changes and cancel payment if necessary
        if (paymentResponse.getValue() != -1) {
           paymentService.cancelPayment(paymentResponse.getValue());
        }
        checkoutRollBack(clientId, cart, itemsrollbackData, cartrollbackdata, basketsrollbackdata);
        
        // Throw the exception to indicate failure
        throw new RuntimeException("Checkout failed: " + e.getMessage(), e);
    }
}

    /**
     * Helper method to handle rollback operations during checkout failure
     * 
     * @param clientId The client ID
     * @param cart The shopping cart
     * @param itemsrollbackData Map of store-product pairs to quantities to restore
     * @param cartrollbackdata Set of store IDs to add back to cart
     * @param basketsrollbackdata Set of baskets to restore
     */
    private void checkoutRollBack(String clientId, IShoppingCart cart, 
                             Map<Pair<String, String>, Integer> itemsrollbackData, 
                             Set<String> cartrollbackdata, 
                             Set<ShoppingBasket> basketsrollbackdata) {
        try {
            // Restore item quantities
            for (Map.Entry<Pair<String, String>, Integer> entry : itemsrollbackData.entrySet()) {
                try {
                    Pair<String, String> key = entry.getKey();
                    String storeId = key.getFirst();
                    String productId = key.getSecond();
                    int quantity = entry.getValue();
    
                    itemFacade.increaseAmount(new Pair<>(storeId, productId), quantity);
                } catch (Exception e) {
                    // Catch and log individual failures during rollback
                    System.err.println("Error during item quantity rollback: " + e.getMessage());
                }
            }
    
            // Restore baskets
            for (ShoppingBasket basket : basketsrollbackdata) {
                try {
                    // Add null check for basket.getOrders()
                    Map<String, Integer> orders = basket.getOrders();
                    if (orders != null) {
                        // Re-add the items to the basket
                        for (Map.Entry<String, Integer> entry : orders.entrySet()) {
                            if (entry != null) {
                                String productId = entry.getKey();
                                Integer quantity = entry.getValue();
                                
                                if (productId != null && quantity != null) {
                                    // Re-add the item to the basket
                                    basket.addOrder(productId, quantity);
                                }
                            }
                        }
                    }
                    basketRepo.update(new Pair<>(clientId, basket.getStoreId()), basket);
                } catch (Exception e) {
                    // Catch and log individual failures during rollback
                    System.err.println("Error during basket rollback: " + e.getMessage());
                }
            }
            
            // Restore cart
            try {
                for (String storeId : cartrollbackdata) {
                    // Re-add the store to the cart
                    cart.addStore(storeId);
                }
                cartRepo.update(clientId, cart);
            } catch (Exception e) {
                // Catch and log individual failures during rollback
                System.err.println("Error during cart rollback: " + e.getMessage());
            }
        } catch (Exception e) {
            // Catch any overall failures during rollback
            System.err.println("Error during checkout rollback: " + e.getMessage());
        }
    }


    @Override
    public int getTotalItems(String clientId) {
        IShoppingCart cart = getCart(clientId);

        int total = 0;
        // Add null check for cart.getCart()
        Set<String> storeIds = cart.getCart();
        if (storeIds != null) {
            for(String storeId : storeIds) {
                ShoppingBasket basket = basketRepo.get(new Pair<>(clientId, storeId));
                if (basket == null) {
                    continue; // Skip if basket is not found
                }
                total += basket.getQuantity();
            }
        }
        return total;
    }

    /**
     * Checks if the client's cart is empty.
     * 
     * @param clientId The ID of the client
     * @return true if the cart is empty or has no items, false otherwise
     */
    @Override
    public boolean isEmpty(String clientId) {
        IShoppingCart cart = getCart(clientId);
        if (cart == null || cart.isEmpty()) {
            return true;
        }
        return getTotalItems(clientId) == 0;
    }

    /**
     * Removes all items from the client's cart across all stores.
     * 
     * @param clientId The ID of the client
     * @return true if the cart was successfully cleared, false otherwise
     */
    @Override
    public boolean clearCart(String clientId) {
        IShoppingCart cart = getCart(clientId);
        // Since getCart always returns a cart (creating one if needed),
        // we don't need to check if cart is null here
        
        // Add null check for cart.getCart()
        Set<String> storeIds = cart.getCart();
        if (storeIds != null) {
            for (String storeId : storeIds) {
                clearBasket(clientId, storeId);
            }
        }
        
        cart.clear();
        cartRepo.update(clientId, cart);
        
        return true; // Always return true since we always have a cart (even if empty)
    }

    /**
     * Removes all items from a specific store in the client's cart.
     * 
     * @param clientId The ID of the client
     * @param storeId The ID of the store to clear from the cart
     * @return true if the store's basket was successfully cleared, false otherwise
     */
    @Override
    public boolean clearBasket(String clientId, String storeId) {
        ShoppingBasket basket = getBasket(clientId, storeId);
        // Since getBasket always returns a basket (creating one if needed),
        // basket will never be null here
        
        basket.clear();
        basketRepo.update(new Pair<>(clientId, storeId), basket);
        
        return true; // Always return true since we always have a basket (even if empty)
    }

    /**
     * Provides a view of all items in the client's cart organized by store.
     * 
     * @param clientId The ID of the client
     * @return A set of pairs where each pair contains an item and its quantity
     */
    @Override
    public Set<Pair<Item,Integer>> viewCart(String clientId) {
        IShoppingCart userCart = getCart(clientId);
        Set<Pair<Item, Integer>> viewCart = new HashSet<>();
        
        // Add null check for userCart.getCart()
        Set<String> storeIds = userCart.getCart();
        if (storeIds != null) {
            for (String storeId : storeIds) {
                ShoppingBasket basket = getBasket(clientId, storeId);
                if (basket == null) {
                    continue;
                }
                
                // Add null check for basket.getOrders()
                Map<String, Integer> orders = basket.getOrders();
                if (orders != null) {
                    for (Map.Entry<String, Integer> entry : orders.entrySet()) {
                        String productId = entry.getKey();
                        int quantity = entry.getValue();
                        Item item = itemFacade.getItem(storeId, productId);
                        // Add null check for item
                        if (item != null) {
                            viewCart.add(new Pair<>(item, quantity));
                        }
                    }
                }
            }
        }
        
        return viewCart;
    }

    // Method to get a client's purchase history
    public List<Receipt> getClientPurchaseHistory(String clientId) {
        return receiptRepo.getClientReceipts(clientId);
    }
    
    // Method to get a store's purchase history
    public List<Receipt> getStorePurchaseHistory(String storeId) {
        return receiptRepo.getStoreReceipts(storeId);
    }

    /**
     * Checks if a string is a valid card number.
     * 
     * @param str The string to check
     * @return true if the string contains only digits, false otherwise
     */
    public boolean isCardNumber(String str) {
        // Check for null or empty string
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        // Check each character
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        
        return true;
    } 

    public String getStoreName(String storeId) {
        return storeFacade.getStore(storeId).getName();
    }

    /**
     * Gets all users who have shopping baskets in a specific store.
     * 
     * @param storeId The ID of the store
     * @return A set of user IDs who have baskets in the store
     */
    @Override
    public Set<String> getUsersWithBaskets(String storeId) {
        Set<String> usersWithBaskets = new HashSet<>();
        
        // Get all carts
        Map<String, IShoppingCart> allCarts = cartRepo.getAll();
        
        // For each cart, check if it contains the store
        for (Map.Entry<String, IShoppingCart> entry : allCarts.entrySet()) {
            String userId = entry.getKey();
            IShoppingCart cart = entry.getValue();
            
            if (cart.hasStore(storeId)) {
                usersWithBaskets.add(userId);
            }
        }
        
        return usersWithBaskets;
    }
}