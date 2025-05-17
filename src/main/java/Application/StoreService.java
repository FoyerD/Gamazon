package Application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import Application.DTOs.AuctionDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Error;
import Application.utils.Response;
import Domain.TokenService;
import Domain.Store.Store;
import Domain.Store.StoreFacade;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;
import Infrastructure.NotificationService;
import Domain.management.Permission;

public class StoreService {

    private StoreFacade storeFacade;
    private TokenService tokenService;
    private PermissionManager permissionManager;
    private NotificationService notificationService;


    public StoreService() {
        this.storeFacade = null;
        this.tokenService = null;
        this.permissionManager = null;
        this.notificationService = null;
    }

    public StoreService(StoreFacade storeFacade, TokenService tokenService, PermissionManager permissionManager, NotificationService notificationService) {
        this.notificationService = notificationService;
        this.storeFacade = storeFacade;
        this.tokenService = tokenService;
        this.permissionManager = permissionManager;
    }

    private boolean isInitialized() {
        return this.storeFacade != null && this.tokenService != null && this.permissionManager != null;
    }

    public Response<StoreDTO> addStore(String sessionToken, String name, String description) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);

            Store store = storeFacade.addStore(name, description, userId);
            if(store == null) {
                return new Response<>(new Error("Failed to create store."));
            }
            permissionManager.appointFirstStoreOwner(userId, store.getId());
            return new Response<>(new StoreDTO(store));

        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }



    public Response<Boolean> openStore(String sessionToken, String storeId){
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            permissionManager.checkPermission(userId, storeId, PermissionType.OPEN_DEACTIVATE_STORE);
            boolean result = this.storeFacade.openStore(storeId);
            return new Response<>(result);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }
    public Response<Boolean> closeStore(String sessionToken, String storeId){
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
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
            return new Response<>(result);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<StoreDTO> getStoreByName(String sessionToken, String name) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            Store store = this.storeFacade.getStoreByName(name);
            if(store == null) {
                return new Response<>(new Error("Store not found."));
            }
            return new Response<>(new StoreDTO(store));
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<AuctionDTO> addAuction(String sessionToken, String storeId, String productId, String auctionEndDate, double startPrice) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return new Response<>(new Error("Invalid token"));
            }
            String userId = this.tokenService.extractId(sessionToken);
            permissionManager.checkPermission(userId, storeId, PermissionType.OVERSEE_OFFERS);
            return new Response<>(new AuctionDTO(this.storeFacade.addAuction(storeId, productId, auctionEndDate, startPrice)));
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<AuctionDTO>> getAllStoreAuctions(String sessionToken, String storeId) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return new Response<>(new Error("Invalid token"));
            }
            List<AuctionDTO> auctions = this.storeFacade.getAllStoreAuctions(storeId).stream().map(AuctionDTO::new).collect(Collectors.toList());
            return new Response<>(auctions);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<AuctionDTO>> getAllProductAuctions(String sessionToken, String productId) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return new Response<>(new Error("Invalid token"));
            }
            List<AuctionDTO> auctions = this.storeFacade.getAllProductAuctions(productId).stream().map(AuctionDTO::new).collect(Collectors.toList());
            return new Response<>(auctions);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}
