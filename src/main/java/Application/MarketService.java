package Application;


import Domain.management.IMarketFacade;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;
import Domain.ExternalServices.INotificationService;
import Domain.Pair;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.Shopping.Receipt;

import Domain.User.Member;
import Domain.Store.Product;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import Application.DTOs.UserDTO;
import Application.DTOs.ClientItemDTO;
import Application.DTOs.ClientOrderDTO;
import Application.DTOs.EmployeeInfo;
import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;

@Service
public class MarketService {

    private final PermissionManager permissionManager;

    private static final String CLASS_NAME = MarketService.class.getSimpleName();

    private final IMarketFacade marketFacade;
    private final TokenService tokenService;

    public MarketService(IMarketFacade marketFacade, TokenService tokenService, PermissionManager permissionManager) {
        this.marketFacade = marketFacade;
        this.tokenService = tokenService;
        this.permissionManager = permissionManager;
    }

    private boolean isInvalid(String sessionToken) {
        return !tokenService.validateToken(sessionToken);
    }

    @Transactional
    public Response<Void> updatePaymentService(String sessionToken, IExternalPaymentService paymentService) {
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

    @Transactional
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

    @Transactional
    public Response<Void> updateSupplyService(String sessionToken, IExternalSupplyService supplyService) {
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

    @Transactional
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

    @Transactional
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

    @Transactional
    public Response<Void> appointStoreManager(String sessionToken, String appointeeId, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "appointStoreManager", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            String appointerId = tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(appointerId)){
                TradingLogger.logError(CLASS_NAME, "appointStoreManager", "User is banned from appointing store manager.");
                return new Response<>(new Error("User is banned from appointing store manager."));
            }
            marketFacade.appointStoreManager(appointerId, appointeeId, storeId);
            TradingLogger.logEvent(CLASS_NAME, "appointStoreManager", "Store manager appointed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "appointStoreManager", "Failed to appoint store manager: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    @Transactional
    public Response<Void> removeStoreOwner(String sessionToken, String ownerId, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "removeStoreManager", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            String removerId = tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(ownerId)){
                TradingLogger.logError(CLASS_NAME, "removeStoreManager", "User is banned from removing store manager.");
                return new Response<>(new Error("User is banned from removing store manager."));
            }
            marketFacade.removeStoreOwner(removerId, ownerId, storeId);
            TradingLogger.logEvent(CLASS_NAME, "removeStoreManager", "Store manager removed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "removeStoreManager", "Failed to remove store manager: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    @Transactional
    public Response<Void> appointStoreOwner(String sessionToken, String appointeeId, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "appointStoreOwner", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            String ownerId = tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(ownerId)){
                TradingLogger.logError(CLASS_NAME, "appointStoreOwner", "User is banned from appointing store owner.");
                return new Response<>(new Error("User is banned from appointing store owner."));
            }
            marketFacade.appointStoreOwner(ownerId, appointeeId, storeId);
            TradingLogger.logEvent(CLASS_NAME, "appointStoreOwner", "Store owner appointed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "appointStoreOwner", "Failed to appoint store owner: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    @Transactional
    public Response<Void> changeManagerPermissions(String sessionToken, String managerId, String storeId, List<PermissionType> newPermissions) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "changeManagerPermissions", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            String userId = tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(userId)){
                TradingLogger.logError(CLASS_NAME, "changeManagerPermissions", "User is banned from changing manager permissions.");
                return new Response<>(new Error("User is banned from changing manager permissions."));
            }
            marketFacade.changeManagerPermissions(userId, managerId, storeId, newPermissions);
            TradingLogger.logEvent(CLASS_NAME, "changeManagerPermissions", "Manager permissions changed successfully.");
            return new Response<>(null);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "changeManagerPermissions", "Failed to change manager permissions: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    @Transactional
    public Response<EmployeeInfo> getEmployeeInfo(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "getEmployeeInfo", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            String userId = tokenService.extractId(sessionToken);
            Map<Member, List<PermissionType>> permissions = marketFacade.getManagersPermissions(storeId, userId);
            Map<UserDTO, List<PermissionType>> userPermissions = new HashMap<>();
            for (Map.Entry<Member, List<PermissionType>> entry : permissions.entrySet()) {
                Member member = entry.getKey();
                List<PermissionType> permissionTypes = entry.getValue();
                UserDTO user = new UserDTO(member);
                userPermissions.put(user, permissionTypes);
            }

            List<UserDTO> owners = marketFacade.getOwners(storeId, userId).stream().map(UserDTO::new).toList();
            EmployeeInfo employeeInfo = new EmployeeInfo(owners, userPermissions);
            TradingLogger.logEvent(CLASS_NAME, "getEmployeeInfo", userPermissions.entrySet().size() + " manager permissions fetched successfully.");

            return Response.success(employeeInfo);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "getEmployeeInfo", "Failed to fetch manager permissions: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }


    @Transactional
    public Response<List<ClientOrderDTO>> getStorePurchaseHistory(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "getStorePurchaseHistory", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            List<Receipt> history = marketFacade.getStorePurchaseHistory(storeId, tokenService.extractId(sessionToken));
            List<ClientOrderDTO> purchaseHistoryDTO = convertReceiptstoClientOrderDTOs(history);
            TradingLogger.logEvent(CLASS_NAME, "getStorePurchaseHistory", "Store purchase history fetched successfully.");
            return new Response<>(purchaseHistoryDTO);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "getStorePurchaseHistory", "Failed to get store purchase history: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }


    private List<ClientOrderDTO> convertReceiptstoClientOrderDTOs(List<Receipt> receipts) {
        List<ClientOrderDTO> purchaseHistoryDTO = new ArrayList<>();
        for (Receipt receipt : receipts) {
            String clientName;
            try {
                clientName = this.marketFacade.getUsername(receipt.getClientId());
            } catch (NoSuchElementException e) {
                clientName = "Unknown";
            }

            List<ClientItemDTO> items = new ArrayList<>();
            for (Map.Entry<Product, Pair<Integer, Double>> entry : receipt.getProducts().entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue().getFirst();
                double price = entry.getValue().getSecond();
                ClientItemDTO itemDTO = new ClientItemDTO(product, 
                                                            clientName, 
                                                            quantity,
                                                            price
                                                        );
                items.add(itemDTO);
            }

            ClientOrderDTO receiptDTO = new ClientOrderDTO(receipt.getReceiptId(),
                                                    clientName,
                                                    items);
            purchaseHistoryDTO.add(receiptDTO);
        }
        return purchaseHistoryDTO;
    }


    @Transactional
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

    @Transactional
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

    @Transactional
    public Response<Boolean> banUser(String sessionToken, String userId, Date endDate) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "banUser", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            String bannerId = tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(bannerId)) {
                TradingLogger.logError(CLASS_NAME, "banUser", "User is banned from banning other users.");
                return new Response<>(new Error("User is banned from banning other users."));
            }
            marketFacade.checkPermission(bannerId, "1", PermissionType.BAN_USERS);
            boolean success = marketFacade.banUser(bannerId, userId, endDate);
            TradingLogger.logEvent(CLASS_NAME, "banUser", "User banned successfully: " + userId);
            return new Response<>(success);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "banUser", "Failed to ban user: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    @Transactional
    public Response<Map<String, Date>> getBannedUsers(String sessionToken) {
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "getBannedUsers", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            String userId = tokenService.extractId(sessionToken);
            marketFacade.checkPermission(userId, "1", PermissionType.BAN_USERS);
            Map<String, Date> bannedUsers = marketFacade.getBannedUsers();
            TradingLogger.logEvent(CLASS_NAME, "getBannedUsers", "Retrieved banned users successfully");
            return new Response<>(bannedUsers);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "getBannedUsers", "Failed to get banned users: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    @Transactional
    public Response<Boolean> unbanUser(String sessionToken, String userId){
        if (isInvalid(sessionToken)) {
            TradingLogger.logError(CLASS_NAME, "banUser", "Invalid session token");
            return new Response<>(new Error("Invalid session token"));
        }
        try {
            String unbannerId = tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(unbannerId)) {
                TradingLogger.logError(CLASS_NAME, "banUser", "User is banned from banning other users.");
                return new Response<>(new Error("User is banned from banning other users."));
            }
            marketFacade.checkPermission(unbannerId, "1", PermissionType.BAN_USERS);
            boolean success = marketFacade.unbanUser(unbannerId, userId);
            TradingLogger.logEvent(CLASS_NAME, "banUser", "User unbanned successfully: " + userId);
            return new Response<>(success);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "banUser", "Failed to unban user: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

    @Transactional
    public Response<Boolean> isBanned(String userId) {
        try {
            boolean isBanned = permissionManager.isBanned(userId);
            TradingLogger.logEvent(CLASS_NAME, "isBanned", "Checked ban status for user " + userId);
            return Response.success(isBanned);
        } catch (Exception e) {
            TradingLogger.logError(CLASS_NAME, "isBanned", "Failed to check ban status: %s", e.getMessage());
            return new Response<>(new Error(e.getMessage()));
        }
    }

}