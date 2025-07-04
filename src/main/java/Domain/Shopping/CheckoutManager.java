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

import Application.utils.Response;
import Domain.Pair;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.Repos.IProductRepository;
import Domain.Repos.IReceiptRepository;
import Domain.Repos.IShoppingBasketRepository;
import Domain.Repos.IUserRepository;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.Policy;
import Domain.Store.Product;
import Domain.Store.Discounts.Discount;
import Domain.Store.Discounts.DiscountFacade;
import Domain.Store.Discounts.ItemPriceBreakdown;
import Domain.User.Member;
import Domain.management.PolicyFacade;

/**
 * Manages the checkout process including inventory updates, payment processing, and rollback operations.
 * This class handles the core business logic of the checkout workflow.
 */
@Component
public class CheckoutManager {
    private final IShoppingBasketRepository basketRepo;
    private final IExternalPaymentService paymentService;
    private final IExternalSupplyService supplyService;
    private final ItemFacade itemFacade;
    private final IProductRepository productRepo;
    private final ReceiptBuilder receiptBuilder;
    private final DiscountFacade discountFacade;
    private final PolicyFacade policyFacade;
    private final Function<String, Member> memberLookup;
    private final IReceiptRepository receiptRepo;

    @Autowired
    public CheckoutManager(IShoppingBasketRepository basketRepo, 
                          IExternalPaymentService paymentService,
                          ItemFacade itemFacade, 
                          IProductRepository productRepo,
                          ReceiptBuilder receiptBuilder,
                          DiscountFacade discountFacade, IExternalSupplyService supplyService,
                          PolicyFacade policyFacade,
                          IReceiptRepository receiptRepo,
                          IUserRepository userRepo) {
        this.memberLookup = userRepo::getMember; // Assuming IUserRepository has a method to get Member by ID
        this.supplyService = supplyService;
        this.basketRepo = basketRepo;
        this.paymentService = paymentService;
        this.itemFacade = itemFacade;
        this.productRepo = productRepo;
        this.receiptBuilder = receiptBuilder;
        this.discountFacade = discountFacade;
        this.policyFacade = policyFacade;
        this.receiptRepo = receiptRepo;
    }

    /**
     * Processes the checkout for a shopping cart.
     * 
     * @param clientId The client ID
     * @param cart The shopping cart
     * @param cardNumber The payment card number
     * @param expiryDate The card expiry date
     * @param cvv The card CVV
     * @param clientName The client's name
     * @param deliveryAddress The delivery address
     * @return CheckoutResult containing success status and any error messages
     */
    public CheckoutResult processCheckout(String clientId, IShoppingCart cart, 
                                        String userSSN, String cardNumber, Date expiryDate, String cvv,
                                        String clientName, String deliveryAddress, String city, 
                                        String country, String zipCode) {
        
        Map<Pair<String, String>, Integer> itemsRollbackData = new HashMap<>();
        Set<ShoppingBasket> basketsRollbackData = new HashSet<>();
        Set<String> cartRollbackData = new HashSet<>();
        Map<String, Map<Product, Integer>> storeProductsMap = new HashMap<>();
        Map<String, Map<Product, Double>> storeProductPricesMap = new HashMap<>(); // Store discounted prices

        Response<Integer> paymentResponse = new Response<>(-1);
        Response<Integer> supplyResponse = new Response<>(-1);
        
        try {
            boolean purchaseSuccess = false;
            double totalPrice = 0;
            Set<String> storeIds = cart.getCart();
            
            if (storeIds != null) {
                for (String storeId : storeIds) {
                    ShoppingBasket basket = basketRepo.get(new Pair<>(clientId, storeId));

                    if (basket != null && !basket.isEmpty()) {
                        CheckoutStoreResult storeResult = processStoreBasket(storeId, basket, itemsRollbackData, basketsRollbackData);
                        // Check if cart abids by policies
                        List<Policy> policies = policyFacade.getAllStorePolicies(storeId);
                        Member member = memberLookup.apply(clientId);
                        for (Policy policy : policies) {
                            if (!policy.isApplicable(basket, member)) {
                                throw new RuntimeException("Checkout failed: Basket does not comply with store policies");
                            }
                        }
                        if (storeResult.isSuccess()) {
                            purchaseSuccess = true;
                            totalPrice += storeResult.getTotalPrice();
                            storeProductsMap.put(storeId, storeResult.getProducts());
                            storeProductPricesMap.put(storeId, storeResult.getProductPrices());
                        }
                        
                        // Clear the basket
                        basket.clear();
                        basketRepo.update(new Pair<>(clientId, storeId), basket);
                    }
                }
            }
            else{
                throw new RuntimeException("Checkout failed: Cart is empty");
            }
            // Process payment if there are items to checkout
            if (purchaseSuccess) {
                paymentResponse = paymentService.processPayment(
                    userSSN, cardNumber, expiryDate, cvv, 
                    clientName, totalPrice
                );
                
                if (paymentResponse == null || paymentResponse.errorOccurred()) {
                    String errorMsg = paymentResponse != null ? "An error has occurred while processing payment\n and therefore you haven't been charged" 
                    : "service returned null response";
                    throw new RuntimeException("Payment failed: " + errorMsg);
                }

                if(paymentResponse.getValue() == -1) {
                    throw new RuntimeException("Payment failed: Invalid transaction ID");
                }
            }

            // Clear the cart
            if (storeIds != null) {
                cartRollbackData.addAll(storeIds);
            }
            cart.clear();
            
            

            supplyResponse = supplyService.supplyOrder(
                clientName, deliveryAddress, city, country, zipCode
            );
            
            if (supplyResponse == null || supplyResponse.errorOccurred()) {
                String errorMsg = supplyResponse != null ? "An error has occurred while attempting to ship the item" 
                : "service returned null response";
                throw new RuntimeException("Supply failed: " + errorMsg);
            }

            if(supplyResponse.getValue() == -1) {
                throw new RuntimeException("Supply failed: Invalid transaction ID");
            }

            for (String storeId : storeIds) {
                Map<Product, Double> productPrices = storeProductPricesMap.get(storeId);
                Map<Product, Integer> productQuantities = storeProductsMap.get(storeId);
                Map<Product, Pair<Integer, Double>> productPricesWithQuantities = new HashMap<>();
                if (productPrices != null && productQuantities != null) {
                    for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
                        Product product = entry.getKey();
                        Integer quantity = entry.getValue();
                        if (product != null && quantity != null) {
                            Double price = productPrices.get(product);
                            if (price != null) {
                                productPricesWithQuantities.put(product, new Pair<>(quantity, price));
                            }
                        }
                    }
                }
            }
            
            // Create receipts with discounted prices
            if (purchaseSuccess) {
                String maskedCardNumber = "xxxx-xxxx-xxxx-" + cardNumber.substring(cardNumber.length() - 4);
                String address = deliveryAddress + ", " + city + ", " + country + ", " + zipCode;
                receiptBuilder.createReceiptsWithDiscounts(clientId, storeProductsMap, storeProductPricesMap, maskedCardNumber, address);
            }
            return new CheckoutResult(true, null, itemsRollbackData, cartRollbackData, basketsRollbackData, paymentResponse.getValue(), supplyResponse.getValue());
            
        } catch (Exception e) {
            return new CheckoutResult(false, e.getMessage(), itemsRollbackData, cartRollbackData, basketsRollbackData, paymentResponse.getValue(), supplyResponse.getValue());
        }
    }

    /**
     * Processes a single store's basket during checkout.
     */
    private CheckoutStoreResult processStoreBasket(String storeId, ShoppingBasket basket, 
                                                  Map<Pair<String, String>, Integer> itemsRollbackData,
                                                  Set<ShoppingBasket> basketsRollbackData) {
        Map<Product, Integer> storeProducts = new HashMap<>();
        Map<Product, Double> productPrices = new HashMap<>(); // Store final prices for receipt
        double totalPrice = 0;
        
        basketsRollbackData.add(basket);
        
        // Calculate discounted prices for all products in the basket
        Map<String, ItemPriceBreakdown> priceBreakdowns = null;
        try {
            List<Discount> discounts = this.discountFacade.getStoreDiscounts(storeId);
            priceBreakdowns = basket.getBestPrice(itemFacade::getItem, discounts);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate best prices for store " + storeId + ": " + e.getMessage());
        }
        
        Map<String, Integer> orders = basket.getOrders();
        if (orders != null) {
            try {
                for (Map.Entry<String, Integer> entry : orders.entrySet()) {
                    if (entry != null) {
                        String productId = entry.getKey();
                        Integer quantityObj = entry.getValue();
                        
                        if (productId != null && quantityObj != null) {
                            int quantity = quantityObj;
                            
                            // Decrease item quantity
                            itemFacade.decreaseAmount(new Pair<>(storeId, productId), quantity);
                            
                            // Get product and calculate discounted price
                            Product product = productRepo.get(productId);
                            if (product != null) {
                                Product productCopy = new Product(product);
                                Item item = itemFacade.getItem(storeId, productCopy.getProductId());
                                if (item != null) {
                                    double unitPrice;
                                    
                                    // Use discounted price if available, otherwise fall back to original price
                                    ItemPriceBreakdown priceBreakdown = priceBreakdowns.get(productId);
                                    if (priceBreakdown != null) {
                                        unitPrice = priceBreakdown.getFinalPrice();
                                        
                                        // Log discount application for debugging
                                        if (priceBreakdown.getDiscount() > 0) {
                                            System.out.println("Applied discount to product " + productId + 
                                                             ": Original price " + priceBreakdown.getOriginalPrice() + 
                                                             ", Discount " + (priceBreakdown.getDiscount() * 100) + "%" +
                                                             ", Final price " + unitPrice);
                                        }
                                    } else {
                                        unitPrice = item.getPrice();
                                        System.out.println("No discounts found for product " + productId + 
                                                         ", using original price: " + unitPrice);
                                    }
                                    
                                    double productTotalPrice = unitPrice * quantity;
                                    totalPrice += productTotalPrice;
                                    
                                    storeProducts.put(productCopy, quantity);
                                    productPrices.put(productCopy, unitPrice); // Store the final unit price
                                    itemsRollbackData.put(new Pair<>(storeId, productCopy.getProductId()), quantity);
                                }
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                System.err.println("Caught NPE during checkout processing: " + e.getMessage());
            }
        }
        
        return new CheckoutStoreResult(!storeProducts.isEmpty(), totalPrice, storeProducts, productPrices);
    }

    /**
     * Performs rollback operations when checkout fails.
     */
    public void performRollback(String clientId, IShoppingCart cart, CheckoutResult result) {
        try {
            // Restore item quantities
            for (Map.Entry<Pair<String, String>, Integer> entry : result.getItemsRollbackData().entrySet()) {
                try {
                    Pair<String, String> key = entry.getKey();
                    String storeId = key.getFirst();
                    String productId = key.getSecond();
                    int quantity = entry.getValue();

                    itemFacade.increaseAmount(new Pair<>(storeId, productId), quantity);
                } catch (Exception e) {
                    System.err.println("Error during item quantity rollback: " + e.getMessage());
                }
            }

            // Restore baskets
            for (ShoppingBasket basket : result.getBasketsRollbackData()) {
                try {
                    Map<String, Integer> orders = basket.getOrders();
                    if (orders != null) {
                        for (Map.Entry<String, Integer> entry : orders.entrySet()) {
                            if (entry != null) {
                                String productId = entry.getKey();
                                Integer quantity = entry.getValue();
                                
                                if (productId != null && quantity != null) {
                                    basket.addOrder(productId, quantity);
                                }
                            }
                        }
                    }
                    basketRepo.update(new Pair<>(clientId, basket.getStoreId()), basket);
                } catch (Exception e) {
                    System.err.println("Error during basket rollback: " + e.getMessage());
                }
            }
            
            // Restore cart
            try {
                for (String storeId : result.getCartRollbackData()) {
                    cart.addStore(storeId);
                }
            } catch (Exception e) {
                System.err.println("Error during cart rollback: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error during checkout rollback: " + e.getMessage());
        }
    }

    /**
     * Helper class to hold the result of processing a single store's basket.
     */
    private static class CheckoutStoreResult {
        private final boolean success;
        private final double totalPrice;
        private final Map<Product, Integer> products;
        private final Map<Product, Double> productPrices; // Final unit prices after discounts

        public CheckoutStoreResult(boolean success, double totalPrice, Map<Product, Integer> products) {
            this.success = success;
            this.totalPrice = totalPrice;
            this.products = products;
            this.productPrices = new HashMap<>();
        }

        public CheckoutStoreResult(boolean success, double totalPrice, Map<Product, Integer> products, Map<Product, Double> productPrices) {
            this.success = success;
            this.totalPrice = totalPrice;
            this.products = products;
            this.productPrices = productPrices != null ? productPrices : new HashMap<>();
        }

        public boolean isSuccess() { return success; }
        public double getTotalPrice() { return totalPrice; }
        public Map<Product, Integer> getProducts() { return products; }
        public Map<Product, Double> getProductPrices() { return productPrices; }
    }

    /**
     * Result class for checkout operations.
     */
    public static class CheckoutResult {
        private final boolean success;
        private final String errorMessage;
        private final Map<Pair<String, String>, Integer> itemsRollbackData;
        private final Set<String> cartRollbackData;
        private final Set<ShoppingBasket> basketsRollbackData;
        private final Integer paymentTransactionId; // ID of the processed payment, if successful
        private final Integer supplyTransactionId; // ID of the processed supply, if successful

        public CheckoutResult(boolean success, String errorMessage,
                            Map<Pair<String, String>, Integer> itemsRollbackData,
                            Set<String> cartRollbackData,
                            Set<ShoppingBasket> basketsRollbackData, Integer paymentTransactionId, Integer supplyTransactionId) {
            this.paymentTransactionId = paymentTransactionId;
            this.supplyTransactionId = supplyTransactionId;
            this.success = success;
            this.errorMessage = errorMessage;
            this.itemsRollbackData = itemsRollbackData;
            this.cartRollbackData = cartRollbackData;
            this.basketsRollbackData = basketsRollbackData;
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public Map<Pair<String, String>, Integer> getItemsRollbackData() { return itemsRollbackData; }
        public Set<String> getCartRollbackData() { return cartRollbackData; }
        public Set<ShoppingBasket> getBasketsRollbackData() { return basketsRollbackData; }
        public Integer getPaymentTransactionId() { return paymentTransactionId; }
        public Integer getSupplyTransactionId() { return supplyTransactionId; }
    }
}