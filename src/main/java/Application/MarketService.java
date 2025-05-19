package Application;

import Domain.management.IMarketFacade;
import Domain.management.PermissionType;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.Receipt;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;


import org.springframework.stereotype.Service;

import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;

@Service
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

    public Response<Void> appointStoreManager(String sessionToken, String appointerId, String appointeeId, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "appointStoreManager", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.appointStoreManager(appointerId, appointeeId, storeId);
            TradingLogger.logEvent(CLASS_NAME, "appointStoreManager", "Store manager appointed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "appointStoreManager", "Failed to appoint store manager: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> removeStoreManager(String sessionToken, String removerId, String managerId, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "removeStoreManager", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.removeStoreManager(removerId, managerId, storeId);
            TradingLogger.logEvent(CLASS_NAME, "removeStoreManager", "Store manager removed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "removeStoreManager", "Failed to remove store manager: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> appointStoreOwner(String sessionToken, String appointerId, String appointeeId, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "appointStoreOwner", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.appointStoreOwner(appointerId, appointeeId, storeId);
            TradingLogger.logEvent(CLASS_NAME, "appointStoreOwner", "Store owner appointed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "appointStoreOwner", "Failed to appoint store owner: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> changeManagerPermissions(String sessionToken, String ownerId, String managerId, String storeId, List<PermissionType> newPermissions) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "changeManagerPermissions", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.changeManagerPermissions(ownerId, managerId, storeId, newPermissions);
            TradingLogger.logEvent(CLASS_NAME, "changeManagerPermissions", "Manager permissions changed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "changeManagerPermissions", "Failed to change manager permissions: %s", e.getMessage());
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

    public Response<Boolean> userExists(String username) {
        try {
            boolean exists = marketFacade.userExists(username);
            TradingLogger.logEvent(CLASS_NAME, "userExists", "Checked if user exists: " + username);
            return new Response<>(exists);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "userExists", "Failed to check if user exists: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Boolean> banUser(String sessionToken, String userId, Date endDate) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "banUser", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            marketFacade.checkPermission(tokenService.extractId(sessionToken), "1", PermissionType.BAN_USERS);
            marketFacade.banUser(userId, endDate);
            TradingLogger.logEvent(CLASS_NAME, "banUser", "User banned successfully: " + userId);
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "banUser", "Failed to ban user: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }
}
