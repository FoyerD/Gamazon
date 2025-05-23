package Application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Application.DTOs.AuctionDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;

import Application.TokenService;
import Domain.ExternalServices.INotificationService;
import Domain.Store.Item;
import Domain.Store.Store;
import Domain.Store.StoreFacade;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;
import Domain.management.Permission;
import Domain.Shopping.IShoppingCartFacade;

@Service
public class StoreService {
    private static final String CLASS_NAME = StoreService.class.getSimpleName();
    private StoreFacade storeFacade;
    private TokenService tokenService;
    private PermissionManager permissionManager;
    private INotificationService notificationService;
    private IShoppingCartFacade shoppingCartFacade;

    public StoreService() {
        this.storeFacade = null;
        this.tokenService = null;
        this.permissionManager = null;
        this.notificationService = null;
        this.shoppingCartFacade = null;
    }

    @Autowired
    public StoreService(StoreFacade storeFacade, TokenService tokenService, PermissionManager permissionManager, 
                       INotificationService notificationService, IShoppingCartFacade shoppingCartFacade) {
        this.notificationService = notificationService;
        this.storeFacade = storeFacade;
        this.tokenService = tokenService;
        this.permissionManager = permissionManager;
        this.shoppingCartFacade = shoppingCartFacade;
        TradingLogger.logEvent(CLASS_NAME, "Constructor", "StoreService initialized with dependencies");
    }

    private boolean isInitialized() {
        return this.storeFacade != null && this.tokenService != null && this.permissionManager != null;
    }

    public Response<StoreDTO> addStore(String sessionToken, String name, String description) {
        String method = "addStore";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "StoreService is not initialized");
                return new Response<>(new Error("StoreService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(userId)){
                throw new Exception("User is banned from creating stores.");
            }
            Store store = storeFacade.addStore(name, description, userId);
            if(store == null) {
                TradingLogger.logError(CLASS_NAME, method, "Failed to create store with name %s", name);
                return new Response<>(new Error("Failed to create store."));
            }
            permissionManager.appointFirstStoreOwner(userId, store.getId());
            TradingLogger.logEvent(CLASS_NAME, method, "Store created successfully: " + store.getId() + " with name: " + name);
            return new Response<>(new StoreDTO(store));

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error creating store: %s", ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> openStore(String sessionToken, String storeId){
        String method = "openStore";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "StoreService is not initialized");
                return new Response<>(new Error("StoreService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(userId)){
                throw new Exception("User is banned from opening stores.");
            }
            permissionManager.checkPermission(userId, storeId, PermissionType.OPEN_DEACTIVATE_STORE);
            boolean result = this.storeFacade.openStore(storeId);
            TradingLogger.logEvent(CLASS_NAME, method, "Store " + storeId + " opened by user " + userId);
            return new Response<>(result);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error opening store %s: %s", storeId, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }
    
    public Response<Boolean> closeStore(String sessionToken, String storeId){
        String method = "closeStore";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "StoreService is not initialized");
                return new Response<>(new Error("StoreService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(userId)){
                throw new Exception("User is banned from closing stores.");
            }
            permissionManager.checkPermission(userId, storeId, PermissionType.OPEN_DEACTIVATE_STORE);
            boolean result = this.storeFacade.closeStore(storeId);
            Map<String, Permission> storePermissions = permissionManager.getStorePermissions(storeId);
            if (storePermissions != null) {
                for (Map.Entry<String, Permission> entry : storePermissions.entrySet()) {
                    String currId = entry.getKey();
                    Permission permission = entry.getValue();
                    if (permission.isStoreManager() || permission.isStoreOwner()) {
                        permissionManager.removeAllPermissions(storeId, userId);
                        notificationService.sendNotification(currId, "Store " + storeId + " has been closed.");
                    }
                }
            }
            TradingLogger.logEvent(CLASS_NAME, method, "Store " + storeId + " closed by user " + userId);
            return new Response<>(result);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error closing store %s: %s", storeId, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> closeStoreNotPermanent(String sessionToken, String storeId){
        String method = "closeStoreNotPermanent";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "StoreService is not initialized");
                return new Response<>(new Error("StoreService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(userId)){
                throw new Exception("User is banned from closing stores.");
            }
            permissionManager.checkPermission(userId, storeId, PermissionType.OPEN_DEACTIVATE_STORE);
            boolean result = this.storeFacade.closeStoreNotPermanent(storeId);
            TradingLogger.logEvent(CLASS_NAME, method, "Store " + storeId + " closed by user " + userId);
            return new Response<>(result);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error closing store %s: %s", storeId, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<StoreDTO> getStoreByName(String sessionToken, String name) {
        String method = "getStoreByName";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "StoreService is not initialized");
                return new Response<>(new Error("StoreService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            Store store = this.storeFacade.getStoreByName(name);
            if(store == null) {
                TradingLogger.logError(CLASS_NAME, method, "Store not found with name %s", name);
                return new Response<>(new Error("Store not found."));
            }
            TradingLogger.logEvent(CLASS_NAME, method, "Retrieved store by name: " + name);
            return new Response<>(new StoreDTO(store));
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error retrieving store by name %s: %s", name, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<AuctionDTO> addAuction(String sessionToken, String storeId, String productId, String auctionEndDate, double startPrice) {
        String method = "addAuction";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "StoreService is not initialized");
                return new Response<>(new Error("StoreService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return new Response<>(new Error("Invalid token"));
            }
            String userId = this.tokenService.extractId(sessionToken);
            if(permissionManager.isBanned(userId)){
                throw new Exception("User is banned from adding auctions.");
            }
            permissionManager.checkPermission(userId, storeId, PermissionType.OVERSEE_OFFERS);
            AuctionDTO auction = new AuctionDTO(this.storeFacade.addAuction(storeId, productId, auctionEndDate, startPrice));
            TradingLogger.logEvent(CLASS_NAME, method, "Auction added for product " + productId + " in store " + storeId + " with end date " + auctionEndDate);
            return new Response<>(auction);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error adding auction for store %s, product %s: %s", storeId, productId, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<AuctionDTO>> getAllStoreAuctions(String sessionToken, String storeId) {
        String method = "getAllStoreAuctions";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "StoreService is not initialized");
                return new Response<>(new Error("StoreService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return new Response<>(new Error("Invalid token"));
            }
            List<AuctionDTO> auctions = this.storeFacade.getAllStoreAuctions(storeId).stream().map(AuctionDTO::new).collect(Collectors.toList());
            TradingLogger.logEvent(CLASS_NAME, method, "Retrieved " + auctions.size() + " auctions for store " + storeId);
            return new Response<>(auctions);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error retrieving auctions for store %s: %s", storeId, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<AuctionDTO>> getAllProductAuctions(String sessionToken, String productId) {
        String method = "getAllProductAuctions";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "StoreService is not initialized");
                return new Response<>(new Error("StoreService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return new Response<>(new Error("Invalid token"));
            }
            List<AuctionDTO> auctions = this.storeFacade.getAllProductAuctions(productId).stream().map(AuctionDTO::new).collect(Collectors.toList());
            TradingLogger.logEvent(CLASS_NAME, method, "Retrieved " + auctions.size() + " auctions for product " + productId);
            return new Response<>(auctions);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error retrieving auctions for product %s: %s", productId, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<ItemDTO> acceptBid(String sessionToken, String storeId, String productId, String auctionId) {
        String method = "acceptBid";
        try {
            if (!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "StoreService is not initialized");
                return new Response<>(new Error("StoreService is not initialized."));
            }

            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return new Response<>(new Error("Invalid token"));
            }

            String userId = tokenService.extractId(sessionToken);
            if (permissionManager.isBanned(userId)) {
                throw new Exception("User is banned from accepting bids.");
            }
            // Check that the user has permission to accept bids
            permissionManager.checkPermission(userId, storeId, PermissionType.OVERSEE_OFFERS);

            // Accept the bid for the given auction and product
            Item item = storeFacade.acceptBid(storeId, productId, auctionId);
            if (item == null) {
                TradingLogger.logError(CLASS_NAME, method, "Failed to accept bid for auction %s", auctionId);
                return new Response<>(new Error("Failed to accept bid. It may not exist or the auction is closed."));
            }

            TradingLogger.logEvent(CLASS_NAME, method, "Bid accepted for auction " + auctionId + " on product " + productId + " in store " + storeId);
            return new Response<>(ItemDTO.fromItem(item));

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error accepting bid for auction %s: %s", auctionId, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    /**
     * Gets all users who have shopping baskets in a specific store.
     * 
     * @param storeId The ID of the store
     * @return A set of user IDs who have baskets in the store
     */
    public Set<String> getUsersWithBaskets(String storeId) {
        String method = "getUsersWithBaskets";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "StoreService is not initialized");
                throw new RuntimeException("StoreService is not initialized.");
            }
            
            return shoppingCartFacade.getUsersWithBaskets(storeId);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, "Error getting users with baskets for store %s: %s", storeId, ex.getMessage());
            return new HashSet<>(); // Return empty set in case of error
        }
    }
}