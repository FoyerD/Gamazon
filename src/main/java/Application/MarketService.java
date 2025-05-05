package Application;

import Domain.User.IUserRepository;
import Domain.management.IMarketFacade;
import Domain.management.PermissionType;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Store.Feedback;
import Domain.Store.IItemRepository;
import Domain.Store.StoreFacade;
import Domain.Shopping.Receipt;
import Domain.Shopping.ShoppingCartFacadeTest;
import Domain.TokenService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;

public class MarketService {

    private static final String CLASS_NAME = MarketService.class.getSimpleName();

    private final IMarketFacade marketFacade;
    private final TokenService tokenService;

    public MarketService(IMarketFacade marketFacade, TokenService tokenService) {
        this.marketFacade = marketFacade;
        this.tokenService = tokenService;
    }

    private boolean isInvalid(String sessionToken) {
        return !tokenService.validateToken(sessionToken);
    }

    public Response<Void> updatePaymentService(String sessionToken, IPaymentService paymentService) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "updatePaymentService", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.updatePaymentService(paymentService);
            TradingLogger.logEvent(CLASS_NAME, "updatePaymentService", "Payment service updated successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "updatePaymentService", "Failed to update payment service: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updateNotificationService(String sessionToken, INotificationService notificationService) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "updateNotificationService", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.updateNotificationService(notificationService);
            TradingLogger.logEvent(CLASS_NAME, "updateNotificationService", "Notification service updated successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "updateNotificationService", "Failed to update notification service: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updateSupplyService(String sessionToken, ISupplyService supplyService) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "updateSupplyService", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.updateSupplyService(supplyService);
            TradingLogger.logEvent(CLASS_NAME, "updateSupplyService", "Supply service updated successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "updateSupplyService", "Failed to update supply service: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updatePaymentServiceURL(String sessionToken, String url) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "updatePaymentServiceURL", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.updatePaymentServiceURL(url);
            TradingLogger.logEvent(CLASS_NAME, "updatePaymentServiceURL", "Payment service URL updated successfully.");
            return new Response<>(null);
        } catch (IOException e) {
            TradingLogger.logError(CLASS_NAME, "updatePaymentServiceURL", "Failed to update payment service URL: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> initFacades(String sessionToken, IUserRepository userRepository, IItemRepository itemRepository, StoreFacade storeFacade, ShoppingCartFacadeTest shoppingCartFacade) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "initFacades", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.initFacades(userRepository, itemRepository, storeFacade, shoppingCartFacade);
            TradingLogger.logEvent(CLASS_NAME, "initFacades", "Facades initialized successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "initFacades", "Failed to initialize facades: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<INotificationService> getNotificationService(String sessionToken) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "getNotificationService", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            INotificationService service = marketFacade.getNotificationService();
            TradingLogger.logEvent(CLASS_NAME, "getNotificationService", "Notification service fetched successfully.");
            return new Response<>(service);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "getNotificationService", "Failed to fetch notification service: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> addProductsToInventory(String sessionToken, String storeId, Map<Integer, Integer> productQuantities) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "addProductsToInventory", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.addProductsToInventory(storeId, productQuantities, tokenService.extractId(sessionToken));
            TradingLogger.logEvent(CLASS_NAME, "addProductsToInventory", "Products added to inventory successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "addProductsToInventory", "Failed to add products to inventory: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updateProductQuantities(String sessionToken, String storeId, Map<Integer, Integer> productQuantities) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "updateProductQuantities", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.updateProductQuantities(storeId, productQuantities, tokenService.extractId(sessionToken));
            TradingLogger.logEvent(CLASS_NAME, "updateProductQuantities", "Product quantities updated successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "updateProductQuantities", "Failed to update product quantities: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> removeProductsFromInventory(String sessionToken, String storeId, Map<Integer, Integer> productQuantities) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "removeProductsFromInventory", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.removeProductsFromInventory(storeId, productQuantities, tokenService.extractId(sessionToken));
            TradingLogger.logEvent(CLASS_NAME, "removeProductsFromInventory", "Products removed from inventory successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "removeProductsFromInventory", "Failed to remove products from inventory: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> appointStoreManager(String sessionToken, String appointerUsername, String appointeeUsername, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "appointStoreManager", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.appointStoreManager(appointerUsername, appointeeUsername, storeId);
            TradingLogger.logEvent(CLASS_NAME, "appointStoreManager", "Store manager appointed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "appointStoreManager", "Failed to appoint store manager: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> removeStoreManager(String sessionToken, String removerUsername, String managerUsername, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "removeStoreManager", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.removeStoreManager(removerUsername, managerUsername, storeId);
            TradingLogger.logEvent(CLASS_NAME, "removeStoreManager", "Store manager removed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "removeStoreManager", "Failed to remove store manager: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> appointStoreOwner(String sessionToken, String appointerUsername, String appointeeUsername, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "appointStoreOwner", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.appointStoreOwner(appointerUsername, appointeeUsername, storeId);
            TradingLogger.logEvent(CLASS_NAME, "appointStoreOwner", "Store owner appointed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "appointStoreOwner", "Failed to appoint store owner: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> changeManagerPermissions(String sessionToken, String ownerUsername, String managerUsername, String storeId, List<PermissionType> newPermissions) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "changeManagerPermissions", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.changeManagerPermissions(ownerUsername, managerUsername, storeId, newPermissions);
            TradingLogger.logEvent(CLASS_NAME, "changeManagerPermissions", "Manager permissions changed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "changeManagerPermissions", "Failed to change manager permissions: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> closeStore(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "closeStore", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.closeStore(storeId, tokenService.extractId(sessionToken));
            TradingLogger.logEvent(CLASS_NAME, "closeStore", "Store closed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "closeStore", "Failed to close store: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> marketCloseStore(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "marketCloseStore", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.marketCloseStore(storeId, tokenService.extractId(sessionToken));
            TradingLogger.logEvent(CLASS_NAME, "marketCloseStore", "Market closed store successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "marketCloseStore", "Failed to close store by market: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Map<String, List<PermissionType>>> getManagersPermissions(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "getManagersPermissions", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            Map<String, List<PermissionType>> permissions = marketFacade.getManagersPermissions(storeId, tokenService.extractId(sessionToken));
            TradingLogger.logEvent(CLASS_NAME, "getManagersPermissions", "Manager permissions fetched successfully.");
            return new Response<>(permissions);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "getManagersPermissions", "Failed to fetch manager permissions: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Boolean> respondToUserMessage(String sessionToken, String storeId, String productId, String userId, String comment) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "respondToUserMessage", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            Boolean result = marketFacade.respondToUserMessage(storeId, productId, userId, comment);
            TradingLogger.logEvent(CLASS_NAME, "respondToUserMessage", "Responded to user message successfully.");
            return new Response<>(result);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "respondToUserMessage", "Failed to respond to user message: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Feedback> getUserMessage(String sessionToken, String storeId, String productId, String userId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "getUserMessage", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            Feedback feedback = marketFacade.getUserMessage(storeId, productId, userId);
            TradingLogger.logEvent(CLASS_NAME, "getUserMessage", "User message fetched successfully.");
            return new Response<>(feedback);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "getUserMessage", "Failed to get user message: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<List<Receipt>> getStorePurchaseHistory(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "getStorePurchaseHistory", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            List<Receipt> history = marketFacade.getStorePurchaseHistory(storeId, tokenService.extractId(sessionToken));
            TradingLogger.logEvent(CLASS_NAME, "getStorePurchaseHistory", "Store purchase history fetched successfully.");
            return new Response<>(history);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "getStorePurchaseHistory", "Failed to get store purchase history: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> openMarket(String sessionToken) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "openMarket", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.openMarket(tokenService.extractId(sessionToken));
            TradingLogger.logEvent(CLASS_NAME, "openMarket", "Market opened successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "openMarket", "Failed to open market: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }
}
