package Application;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Application.DTOs.OfferDTO;
import Application.DTOs.CartDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.ItemPriceBreakdownDTO;
import Application.DTOs.OrderedItemDTO;
import Application.DTOs.PaymentDetailsDTO;
import Application.DTOs.ReceiptDTO;
import Application.DTOs.ShoppingBasketDTO;
import Application.DTOs.UserDTO;
import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.Pair;
import Domain.ExternalServices.INotificationService;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.Offer;
import Domain.Shopping.OfferManager;
import Domain.Shopping.Receipt;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.Product;
import Domain.Store.StoreFacade;
import Domain.Store.Discounts.ItemPriceBreakdown;
import Domain.User.LoginManager;
import Domain.User.User;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;

@Service
public class ShoppingService{
    private static final String CLASS_NAME = ShoppingService.class.getSimpleName();
    private final IShoppingCartFacade cartFacade;
    private final TokenService tokenService;
    private final INotificationService notificationService;
    private final LoginManager loginManager;
    private final StoreFacade storeFacade;
    private final PermissionManager permissionManager;
    private final OfferManager offerManager;
    private final ItemFacade itemFacade;

    @Autowired
    public ShoppingService(IShoppingCartFacade cartFacade, 
                            TokenService tokenService,
                            INotificationService notificationService, 
                            StoreFacade storeFacade, 
                            PermissionManager permissionManager, 
                            LoginManager loginManager,
                            OfferManager offerManager,
                            ItemFacade itemFacade) {
        this.cartFacade = cartFacade;
        this.tokenService = tokenService;
        this.notificationService = notificationService;
        this.storeFacade = storeFacade;
        this.permissionManager = permissionManager;
        this.loginManager = loginManager;
        this.offerManager = offerManager;
        this.itemFacade = itemFacade;
        
        TradingLogger.logEvent(CLASS_NAME, "Constructor", "ShoppingService initialized with cart facade");
    }

    @Transactional
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

    @Transactional
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

            // Get the cart and baskets
            Set<Pair<Item, Integer>> itemsMap = cartFacade.viewCart(clientId);
            Map<String, ShoppingBasketDTO> baskets = new HashMap<>();

            // Creation of BasketDTO's
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

            for(ShoppingBasketDTO basket : baskets.values()) {
                Map<String, ItemPriceBreakdown> priceBreakDowns = this.cartFacade.getBestPrice(basket.getClientId(), basket.getStoreId());
                basket.getOrders().forEach((productId, item) -> {
                    if(priceBreakDowns.containsKey(productId)) {
                        ItemPriceBreakdownDTO priceBreakDownDTO = ItemPriceBreakdownDTO.fromPriceBreakDown(priceBreakDowns.get(productId));
                        item.setPriceBreakDown(priceBreakDownDTO);
                    }
                });
            }

            CartDTO cart = new CartDTO(clientId, baskets);
            TradingLogger.logEvent(CLASS_NAME, method, "Cart viewed for user " + clientId + " with " + itemsMap.size() + " items");
            return new Response<>(cart);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, method, "Error viewing cart: " + e.getMessage());
            return new Response<>(new Error("Error viewing cart: " + e.getMessage()));
        }
    }

    @Transactional
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

    @Transactional
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

    @Transactional 
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

    @Transactional
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
    @Transactional
    public Response<Boolean> checkout(String sessionToken, String userSSN, String cardNumber, Date expiryDate, String cvv,
                           String clientName, String deliveryAddress, String city, String country, String zipCode) {
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

            cartFacade.checkout(clientId, userSSN, cardNumber, expiryDate, cvv, clientName, deliveryAddress, city, country, zipCode);
            TradingLogger.logEvent(CLASS_NAME, method, "Checkout completed successfully for user " + clientId);
            return new Response<>(true);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error during checkout: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    @Transactional
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
    @Transactional
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

    /**
     * Requirement 3.9
     * Member offers new price for product
     * @param sessionToken Identifier for user
     * @param storeId Store identifier from which user wants to bargain
     * @param productId Identifier of the product that the user wants to bargain about.
     * @param newPrice The new price that the user offers
     * @return {@link OfferDTO}
     */
    @Transactional
    public Response<OfferDTO> makeOffer(String sessionToken, String storeId, String productId, double newPrice, PaymentDetailsDTO paymentDetailsDTO) {
        String method = "makeOffer";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }

        String clientId = this.tokenService.extractId(sessionToken);

        try {
            UserDTO member = new UserDTO(loginManager.getLoggedInMember(clientId)); // assure real member exists
            ItemDTO item = ItemDTO.fromItem(itemFacade.getItem(storeId, productId)); // assure real item exists

            
            Offer offer = offerManager.makeOffer(clientId, storeId, productId, newPrice, paymentDetailsDTO.toPaymentDetails());
            OfferDTO offerDTO = convertOfferToDTO(offer);
            offerDTO.getEmployeeApprovers().stream().forEach(e -> {
                notificationService.sendNotification(e.getId(), "🔔 You've received a new offer from " + offerDTO.getMember().getUsername() + " for a " + offerDTO.getItem().getProductName() + " in store " + storeFacade.getStoreName(storeId) + "!");
            });
            TradingLogger.logEvent(CLASS_NAME, method, "Offer made by " + member.getUsername() + " on " + item.getProductName() + " for " + newPrice + "$");
            return Response.success(offerDTO);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error making offer: %s", ex.getMessage());
            return Response.error(ex.getMessage());
        }
    }

    /**
     * Requirement that you forgot to implement
     * Member accepts an offer made by him or by an employee
     * @param sessionToken Identifier for user
     * @param offerId Identifier of the offer that the user wants to accept
     * @return {@link OfferDTO} with updated information
     */
    @Transactional
    public Response<OfferDTO> acceptOffer(String sessionToken, String offerId){
        String method = "acceptOffer";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }

        String userId = this.tokenService.extractId(sessionToken);

        try {
            Offer acceptedOffer = offerManager.acceptOfferByMember(userId, offerId);
            
            OfferDTO offerDTO = convertOfferToDTO(acceptedOffer);
            TradingLogger.logEvent(CLASS_NAME, method, "Offer accepted by " + userId + ": " + offerId);
            return Response.success(offerDTO);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error accepting offer: %s", ex.getMessage());
            return Response.error(ex.getMessage());
        }
    }


    @Transactional
    public Response<OfferDTO> rejectCounterOffer(String sessionToken, String offerId) {
        String method = "rejectCounterOffer";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }

        String userId = this.tokenService.extractId(sessionToken);

        try {
            Offer rejectedOffer = offerManager.rejectOfferByMember(userId, offerId);

            OfferDTO offerDTO = convertOfferToDTO(rejectedOffer);
            TradingLogger.logEvent(CLASS_NAME, method, "Offer rejected by " + offerDTO.getMember().getUsername() + ": " + offerId);
            return Response.success(offerDTO);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error rejecting offer: %s", ex.getMessage());
            return Response.error(ex.getMessage());
        }
    }

    @Transactional
    public Response<List<OfferDTO>> getAllOffersOfUser(String sessionToken) {
        String method = "getAllOffersOfUser";
        try {

            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return new Response<>(new Error("Invalid token"));
            }

            
            String userId = tokenService.extractId(sessionToken);
            List<OfferDTO> offers = offerManager.getOffersOfMember(userId).stream().map(o -> {
                String offerId = o.getId();
                UserDTO member = UserDTO.from(loginManager.getMember(o.getMemberId()));
                Set<UserDTO> approvedBy = o.getApprovedBy().stream().map(this.loginManager::getMember).map(UserDTO::from).collect(Collectors.toSet());
                Set<UserDTO> approvers = new HashSet<>(permissionManager.getUsersWithPermission(o.getStoreId(), PermissionType.OVERSEE_OFFERS).stream().map(loginManager::getMember).map(UserDTO::from).collect(Collectors.toSet()));
                approvers.add(member);

                Item item = itemFacade.getItem(o.getStoreId(), o.getProductId());

                return new OfferDTO(offerId, 
                            member,
                            approvedBy,
                            approvers,
                            ItemDTO.fromItem(item),
                            o.getPrices(), 
                            o.isCounterOffer(),
                            o.isAccepted());
            }).toList();

            User user = loginManager.getUser(userId);
            TradingLogger.logEvent(CLASS_NAME, method, "Retrieved " + offers.size() + " offers of user " + user.getName());
            return Response.success(offers);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error retrieving offers from user.  ", ex.getMessage());
            return Response.error(ex.getMessage());
        }

    }

    @Transactional
    /**
     * Requirement 3.10
     * Member counters an offer made by him or by an employee
     * @param sessionToken Identifier for user
     * @param offerId Identifier of the offer that the user wants to counter
     * @param newPrice The new price that the user offers
     * @param paymentDetailsDTO Payment details for the counter offer
     * @return {@link OfferDTO} with updated information
     */
    public Response<OfferDTO> counterOffer(String sessionToken, String offerId, double newPrice) {
        String method = "counterOffer";
        if (!tokenService.validateToken(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, method, "Invalid token");
            return Response.error("Invalid token");
        }

        String userId = this.tokenService.extractId(sessionToken);

        try {

            Offer counteredOffer = offerManager.counterOfferByMember(userId, offerId, newPrice);
            OfferDTO offerDTO = convertOfferToDTO(counteredOffer);
            offerDTO.getEmployeeApprovers().stream().forEach(e -> {
                notificationService.sendNotification(e.getId(), "🔔 You've received a new counter offer from " + offerDTO.getMember().getUsername() + " for a " + offerDTO.getItem().getProductName() + " in store " + storeFacade.getStoreName(offerDTO.getItem().getStoreId()) + "!");
            });

            TradingLogger.logEvent(CLASS_NAME, method, "Counter offer made by " + offerDTO.getMember().getUsername() + " on " + offerDTO.getItem().getProductName() + " for " + newPrice + "$");
            return Response.success(offerDTO);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error making counter offer: %s", ex.getMessage());
            return Response.error(ex.getMessage());
        }

    }

    private OfferDTO convertOfferToDTO(Offer offer) {
        UserDTO member = new UserDTO(loginManager.getLoggedInMember(offer.getMemberId()));
        ItemDTO item = ItemDTO.fromItem(itemFacade.getItem(offer.getStoreId(), offer.getProductId()));
        Set<UserDTO> approvedBy = offer.getApprovedBy().stream().map(this.loginManager::getMember).map(UserDTO::from).collect(Collectors.toSet());
        Set<UserDTO> approvers = new HashSet<>(permissionManager.getUsersWithPermission(offer.getStoreId(), PermissionType.OVERSEE_OFFERS).stream().map(loginManager::getMember).map(UserDTO::from).collect(Collectors.toSet()));
        approvers.add(member); // Add the member who made the counter offer to the approvers list
        return new OfferDTO(offer.getId(), member, approvedBy, approvers, item, offer.getPrices(), offer.isCounterOffer(), offer.isAccepted());
    }
}