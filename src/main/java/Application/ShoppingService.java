package Application;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import Application.DTOs.CartDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.OrderedItemDTO;
import Application.DTOs.ReceiptDTO;
import Application.DTOs.ShoppingBasketDTO;
import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Domain.Pair;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Shopping.Receipt;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Store.IProductRepository;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.Product;
import Domain.Store.StoreFacade;
import Domain.User.LoginManager;
import Domain.User.User;
import Domain.management.PermissionManager;

@Service
public class ShoppingService{
    private static final String CLASS_NAME = ShoppingService.class.getSimpleName();
    private final IShoppingCartFacade cartFacade;
    private final TokenService tokenService;
    private final LoginManager loginManager;
    private StoreFacade storeFacade;
    private PermissionManager permissionManager;

    @Autowired
    public ShoppingService(IShoppingCartFacade cartFacade, TokenService tokenService, StoreFacade storeFacade, PermissionManager permissionManager, LoginManager loginManager) {
        this.cartFacade = cartFacade;
        this.tokenService = tokenService;
        this.storeFacade = storeFacade;
        this.permissionManager = permissionManager;
        this.loginManager = loginManager;
        
        TradingLogger.logEvent(CLASS_NAME, "Constructor", "ShoppingService initialized with cart facade");
    }

    public Response<Boolean> addProductToCart(String storeId, String sessionToken, String productId, int quantity) {
        String method = "addProductToCart";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);

        try {
            if(this.cartFacade == null) {
                TradingLogger.logError(CLASS_NAME, method, "cartFacade is not initialized");
                return new Response<>(new Error("cartFacade is not initialized."));
            }
            if(this.permissionManager.isBanned(clientId)){
                throw new Exception("User is banned from adding products to cart.");
            }
            String userId = this.tokenService.extractId(sessionToken);
            if (permissionManager.isBanned(userId)) {
                throw new Exception("User is banned from adding products to cart.");
            }
            cartFacade.addProductToCart(storeId, clientId, productId, quantity);
            TradingLogger.logEvent(CLASS_NAME, method, "Product " + productId + " added to cart for user " + clientId + " with quantity " + quantity);
            return new Response<>(true);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error adding product to cart: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<CartDTO> viewCart(String sessionToken) {
        String method = "viewCart";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.cartFacade == null) {
                TradingLogger.logError(CLASS_NAME, method, "cartFacade is not initialized");
                return new Response<>(new Error("cartFacade is not initialized."));
            }
            Set<Pair<Item, Integer>> itemsMap = cartFacade.viewCart(clientId);
            Map<String, ShoppingBasketDTO> baskets = new HashMap<>();
            for (Pair<Item, Integer> item : itemsMap) {
                ItemDTO itemDTO = ItemDTO.fromItem(item.getFirst());
                itemDTO.setAmount(item.getSecond());
                
                if(baskets.containsKey(item.getFirst().getStoreId())){
                    baskets.get(item.getFirst().getStoreId()).getOrders().put(item.getFirst().getProductId(), itemDTO);
                } else {
                    String storeId = item.getFirst().getStoreId();
                    String storeName = this.cartFacade.getStoreName(storeId);
                    ShoppingBasketDTO basket = new ShoppingBasketDTO(item.getFirst().getStoreId(), clientId, new HashMap<>(), storeName);
                    basket.getOrders().put(item.getFirst().getProductId(), itemDTO);
                    baskets.put(item.getFirst().getStoreId(), basket);
                }
            }
            CartDTO cart = new CartDTO(clientId, baskets);
            TradingLogger.logEvent(CLASS_NAME, method, "Cart viewed for user " + clientId + " with " + itemsMap.size() + " items");
            return new Response<>(cart);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error viewing cart: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }


    public Response<Boolean> removeProductFromCart(String storeId, String sessionToken, String productId, int quantity) {
        String method = "removeProductFromCart";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.cartFacade == null) {
                TradingLogger.logError(CLASS_NAME, method, "cartFacade is not initialized");
                return new Response<>(new Error("cartFacade is not initialized."));
            }            if(this.permissionManager == null) return new Response<>(new Error("permissionManager is not initialized."));
            if(permissionManager.isBanned(clientId)){
                throw new Exception("User is banned from removing products from cart.");
            }
            cartFacade.removeProductFromCart(storeId, clientId, productId, quantity);
            TradingLogger.logEvent(CLASS_NAME, method, "Removed " + quantity + " units of product " + productId + " from cart for user " + clientId);
            return new Response<>(true);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error removing product from cart: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> removeProductFromCart(String storeId, String sessionToken, String productId) {
        String method = "removeProductFromCart";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.cartFacade == null) {
                TradingLogger.logError(CLASS_NAME, method, "cartFacade is not initialized");
                return new Response<>(new Error("cartFacade is not initialized."));
            }            if(this.permissionManager == null) return new Response<>(new Error("permissionManager is not initialized."));
            if(permissionManager.isBanned(clientId)){
                throw new Exception("User is banned from removing products from cart.");
            }
            cartFacade.removeProductFromCart(storeId, clientId, productId);
            TradingLogger.logEvent(CLASS_NAME, method, "Completely removed product " + productId + " from cart for user " + clientId);
            return new Response<>(true);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error removing product from cart: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> clearCart(String sessionToken) {
        String method = "clearCart";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.cartFacade == null) {
                TradingLogger.logError(CLASS_NAME, method, "cartFacade is not initialized");
                return new Response<>(new Error("cartFacade is not initialized."));
            }
            if(this.permissionManager == null) return new Response<>(new Error("permissionManager is not initialized."));
            if(permissionManager.isBanned(clientId)){
                throw new Exception("User is banned from clearing cart.");
            }
            cartFacade.clearCart(clientId);
            TradingLogger.logEvent(CLASS_NAME, method, "Cart cleared for user " + clientId);
            return new Response<>(true);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error clearing cart: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> clearBasket(String sessionToken, String storeId) {
        String method = "clearBasket";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        String userId = this.tokenService.extractId(sessionToken);
        if (permissionManager.isBanned(userId)) {
            return Response.error("User is banned from clearing basket.");
        }
        try {
            if(this.cartFacade == null) {
                TradingLogger.logError(CLASS_NAME, method, "cartFacade is not initialized");
                return new Response<>(new Error("cartFacade is not initialized."));
            }

            cartFacade.clearBasket(clientId, storeId);
            TradingLogger.logEvent(CLASS_NAME, method, "Basket cleared for user " + clientId + " and store " + storeId);
            return new Response<>(true);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error clearing basket: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    

    // Make Immidiate Purchase Use Case 2.5
    public Response<Boolean> checkout(String sessionToken, String cardNumber, Date expiryDate, String cvv, long andIncrement,
         String clientName, String deliveryAddress) {
        String method = "checkout";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.permissionManager == null) return new Response<>(new Error("permissionManager is not initialized."));
            if(permissionManager.isBanned(clientId)){
                throw new Exception("User is banned from checking out.");
            }
            if(this.cartFacade == null) {
                TradingLogger.logError(CLASS_NAME, method, "cartFacade is not initialized");
                return new Response<>(new Error("cartFacade is not initialized."));
            }

            cartFacade.checkout(clientId, cardNumber, expiryDate, cvv, andIncrement, clientName, deliveryAddress);
            TradingLogger.logEvent(CLASS_NAME, method, "Checkout completed successfully for user " + clientId);
            return new Response<>(true);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error during checkout: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    
    public Response<Boolean> makeBid(String auctionId, String sessionToken, float price,
                                    String cardNumber, Date expiryDate, String cvv,
                                    long andIncrement, String clientName, String deliveryAddress) {
        String method = "makeBid";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }

        String clientId = this.tokenService.extractId(sessionToken);

        try {
            if (this.cartFacade == null) {
                TradingLogger.logError(CLASS_NAME, method, "cartFacade is not initialized");
                return new Response<>(new Error("cartFacade is not initialized."));
            }

            cartFacade.makeBid(auctionId, clientId, price,
                            cardNumber, expiryDate, cvv,
                            andIncrement, clientName, deliveryAddress);
            TradingLogger.logEvent(CLASS_NAME, method, "Bid made successfully for auction " + auctionId + " by user " + clientId + " with price " + price);
            return new Response<>(true);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error making bid: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }


    // View personal purchase history 3.7
    public Response<List<ReceiptDTO>> getUserPurchaseHistory(String sessionToken) {
        String method = "getUserPurchaseHistory";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            
            if(this.cartFacade == null) {
                TradingLogger.logError(CLASS_NAME, method, "cartFacade is not initialized");
                return new Response<>(new Error("cartFacade is not initialized."));
            }
 

            List<Receipt> purchaseHistory = cartFacade.getClientPurchaseHistory(clientId);
            List<ReceiptDTO> receiptDTOs = converReceiptstoDTOs(purchaseHistory);
            TradingLogger.logEvent(CLASS_NAME, method, "Purchase history retrieved for user " + clientId);
            return new Response<>(receiptDTOs);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error retrieving purchase history: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    
    private List<ReceiptDTO> converReceiptstoDTOs(List<Receipt> receipts) {
        List<ReceiptDTO> purchaseHistoryDTO = new ArrayList<>();
        for (Receipt receipt : receipts) {
            List<OrderedItemDTO> items = new ArrayList<>();
            for (Map.Entry<Product, Pair<Integer, Double>> entry : receipt.getProducts().entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue().getFirst();
                double price = entry.getValue().getSecond();
                OrderedItemDTO itemDTO = new OrderedItemDTO(product, 
                                                            quantity, 
                                                            this.storeFacade.getStoreName(receipt.getStoreId()), 
                                                            price
                                                        );
                items.add(itemDTO);
            }

            String clientName = "Unknown"; // Default name if user not found
            User user = loginManager.getUser(receipt.getClientId());
            if (user != null) {
                clientName = user.getName();
            }
            ReceiptDTO receiptDTO = new ReceiptDTO(receipt.getReceiptId(),
                                                    clientName,
                                                    this.storeFacade.getStoreName(receipt.getStoreId()),
                                                    items);
            purchaseHistoryDTO.add(receiptDTO);
        }
        return purchaseHistoryDTO;
    }

    class MockPaymentService implements IPaymentService {
        @Override
        public Response<Boolean> processPayment(String card_owner, String card_number, Date expiry_date, String cvv,
            double price, long andIncrement, String name, String deliveryAddress) {
            // Mock payment processing logic
            TradingLogger.logEvent(CLASS_NAME, "MockPaymentService.processPayment", "Processing mock payment for " + card_owner + " of $" + price);
            return new Response<>(true); // Assume payment is always successful for testing
        }

        @Override
        public void updatePaymentServiceURL(String url) {
            // Mock implementation for updating payment service URL
            TradingLogger.logEvent(CLASS_NAME, "MockPaymentService.updatePaymentServiceURL", "Payment service URL updated to: " + url);
        }

        @Override
        public void initialize() {
            // Mock initialization logic
            TradingLogger.logEvent(CLASS_NAME, "MockPaymentService.initialize", "MockPaymentService initialized");
        }
    }
}