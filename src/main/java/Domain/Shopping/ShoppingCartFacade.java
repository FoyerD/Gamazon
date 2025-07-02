package Domain.Shopping;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.Pair;
import Domain.Repos.IProductRepository;
import Domain.Repos.IReceiptRepository;
import Domain.Repos.IShoppingBasketRepository;
import Domain.Repos.IShoppingCartRepository;
import Domain.Repos.IUserRepository;
import Domain.Store.Discounts.Discount;
import Domain.Store.Discounts.DiscountFacade;
import Domain.Store.Discounts.ItemPriceBreakdown;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.Policy;
import Domain.Store.StoreFacade;
import Domain.User.Member;
import Domain.management.PolicyFacade;


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
    private final IExternalSupplyService supplyService;
    private final ItemFacade itemFacade;
    private final StoreFacade storeFacade;
    private CheckoutManager checkoutManager;
    private final DiscountFacade discountFacade;
    private final Function<String, Member> memberLookup;
    private final PolicyFacade policyFacade;

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
    public ShoppingCartFacade(IShoppingCartRepository cartRepo, IShoppingBasketRepository basketRepo,
     IExternalPaymentService paymentService, ItemFacade itemFacade, StoreFacade storeFacade,
      IReceiptRepository receiptRepo, IProductRepository productRepository, DiscountFacade discountFacade, PolicyFacade policyFacade, IUserRepository userRepository, IExternalSupplyService supplyService, IReceiptRepository receiptRepository) {
        this.supplyService = supplyService;
        this.cartRepo = cartRepo;
        this.basketRepo = basketRepo;
        this.paymentService = paymentService;   
        this.itemFacade = itemFacade;
        this.storeFacade = storeFacade;
        this.receiptRepo = receiptRepo;
        this.discountFacade = discountFacade;
        this.policyFacade = policyFacade;
        this.memberLookup = userRepository::getMember; // Assuming userRepository has a method to get Member by ID
        this.checkoutManager = new CheckoutManager(basketRepo, paymentService, itemFacade, productRepository,

         new ReceiptBuilder(receiptRepo, itemFacade), discountFacade, supplyService, policyFacade, receiptRepository, userRepository);
    }

    @Override
    public IShoppingCart getCart(String clientId) {
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
    @Override
    public ShoppingBasket getBasket(String clientId, String storeId) {
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
    @org.springframework.transaction.annotation.Transactional
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
        
        try {
            // Get or create the cart
            IShoppingCart cart = getCart(clientId);
            
            // Get or create the basket
            ShoppingBasket basket = basketRepo.get(new Pair<>(clientId, storeId));
            if (basket == null) {
                basket = new ShoppingBasket(storeId, clientId);
                basketRepo.add(new Pair<>(clientId, storeId), basket);
            }
            
            // Add the product to the basket
            basket.addOrder(productId, quantity);
            basketRepo.update(new Pair<>(clientId, storeId), basket);

            // Add the store to the cart if it's not already there
            if (!cart.hasStore(storeId)) {
                cart.addStore(storeId);
                cartRepo.update(clientId, cart);
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error adding product to cart: " + e.getMessage());
            return false;
        }
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
                        long andIncrement, String clientName, String deliveryAddress, String city, String country, String zipCode) {

        

        storeFacade.addBid(auctionId, clientId, price, 
                cardNumber, expiryDate, cvv, clientName, deliveryAddress, city, country, zipCode);
        return true;
    }



    /**
     * Processes checkout for a client's shopping cart.
     * This method has been refactored to use CheckoutManager for better separation of concerns.
     * 
     * @param clientId The ID of the client
     * @param userSSN The client's social security number
     * @param cardNumber The payment card number
     * @param expiryDate The card expiry date
     * @param cvv The card CVV
     * @param clientName The client's name
     * @param deliveryAddress The delivery address
     * @return true if checkout was successful
     * @throws IllegalArgumentException if arguments are invalid
     * @throws RuntimeException if checkout fails
     */
    @Override
    public boolean checkout(String clientId, String userSSN, String cardNumber, Date expiryDate, String cvv,
                           String clientName, String deliveryAddress, String city, String country, String zipCode) {
        
        // Validate arguments
        if (clientId == null || cardNumber == null || expiryDate == null || cvv == null) {
            throw new IllegalArgumentException("Invalid arguments for checkout");
        }
        if (!isCardNumber(cardNumber)) {
            throw new IllegalArgumentException("Invalid card number");
        }

        IShoppingCart cart = getCart(clientId);
        
        // Process checkout using CheckoutManager
        CheckoutManager.CheckoutResult result = checkoutManager.processCheckout(
            clientId, cart, userSSN, cardNumber, expiryDate, cvv, 
            clientName, deliveryAddress, city, country, zipCode
        );
        
        if (result.isSuccess()) {
            // Update cart in repository
            cartRepo.update(clientId, cart);
            return true;
        } else {
            // Perform rollback and throw exception
            Integer paymentTransactionId = result.getPaymentTransactionId();
            Integer supplyTransactionId = result.getSupplyTransactionId();
            if (paymentTransactionId == null || supplyTransactionId == null) {}
            else {
                if(paymentTransactionId != -1 && supplyTransactionId != -1){
                paymentService.cancelPayment(paymentTransactionId);
                supplyService.cancelSupply(supplyTransactionId);
                }
                else{
                    if(paymentTransactionId != -1 && supplyTransactionId == -1)
                        paymentService.cancelPayment(paymentTransactionId);
                    else
                        supplyService.cancelSupply(supplyTransactionId);
                }
            }
            checkoutManager.performRollback(clientId, cart, result);
            cartRepo.update(clientId, cart);
            throw new RuntimeException("Checkout failed: " + result.getErrorMessage());
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
                // Get the basket from the repository
                ShoppingBasket basket = basketRepo.get(new Pair<>(clientId, storeId));
                if (basket == null) {
                    continue;
                }
                
                // Add null check for basket.getOrders()
                Map<String, Integer> orders = basket.getOrders();
                if (orders != null) {
                    for (Map.Entry<String, Integer> entry : orders.entrySet()) {
                        String productId = entry.getKey();
                        int quantity = entry.getValue();
                        
                        // Get the item from the store
                        Item item = itemFacade.getItem(storeId, productId);
                        if (item != null && quantity > 0) {
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

    @Override
    public Map<String, ItemPriceBreakdown> getBestPrice(String clientId, String storeId) {
        ShoppingBasket basket = getBasket(clientId, storeId);
        if (basket == null) {
            return new HashMap<>(); // Return empty map if no basket exists
        }
        List<Discount> discounts = discountFacade.getStoreDiscounts(storeId);
        return basket.getBestPrice(itemFacade::getItem, discounts);
    }
    
    public List<Policy> getViolatedPolicies(String memberId) {
        Member member = memberLookup.apply(memberId);
        return this.getCart(memberId) .getCart().stream() // get stores ids
            .flatMap(storeId -> policyFacade.getAllStorePolicies(storeId).stream()) // get all policies for all stores
            .filter(p -> !p.isApplicable( // check if basket is a applicable
                this.getBasket(memberId, p.getStoreId()), // get basket
                member)
            ).toList();
    }
}