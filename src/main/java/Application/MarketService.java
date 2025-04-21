package Application;

import Domain.IMarketFacade;
import Domain.User.IUserRepository;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.PermissionType;
import Domain.Store.IItemRepository;
import Domain.Store.IStoreRepository;
import Domain.Shopping.IShoppingBasket;
import Domain.TokenService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MarketService {

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
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.updatePaymentService(paymentService);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updateNotificationService(String sessionToken, INotificationService notificationService) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.updateNotificationService(notificationService);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updateSupplyService(String sessionToken, ISupplyService supplyService) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.updateSupplyService(supplyService);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updatePaymentServiceURL(String sessionToken, String url) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.updatePaymentServiceURL(url);
            return new Response<>(null);
        } catch (IOException e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> initFacades(String sessionToken, IUserRepository userFacade, IStoreRepository storeFacade, IItemRepository itemFacade) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.initFacades(userFacade, storeFacade, itemFacade);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<INotificationService> getNotificationService(String sessionToken) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            return new Response<>(marketFacade.getNotificationService());
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> addProductsToInventory(String sessionToken, String storeId, Map<Integer, Integer> productQuantities) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.addProductsToInventory(storeId, productQuantities);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updateProductQuantities(String sessionToken, String storeId, Map<Integer, Integer> productQuantities) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.updateProductQuantities(storeId, productQuantities);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> removeProductsFromInventory(String sessionToken, String storeId, Map<Integer, Integer> productQuantities) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.removeProductsFromInventory(storeId, productQuantities);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> appointStoreManager(String sessionToken, String appointerUsername, String appointeeUsername, String storeId) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.appointStoreManager(appointerUsername, appointeeUsername, storeId);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> removeStoreManager(String sessionToken, String removerUsername, String managerUsername, String storeId) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.removeStoreManager(removerUsername, managerUsername, storeId);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> appointStoreOwner(String sessionToken, String appointerUsername, String appointeeUsername, String storeId) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.appointStoreOwner(appointerUsername, appointeeUsername, storeId);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> changeManagerPermissions(String sessionToken, String ownerUsername, String managerUsername, String storeId, List<PermissionType> newPermissions) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.changeManagerPermissions(ownerUsername, managerUsername, storeId, newPermissions);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> closeStore(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.closeStore(storeId);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> marketCloseStore(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.marketCloseStore(storeId);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Map<String, List<PermissionType>>> getManagersPermissions(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            return new Response<>(marketFacade.getManagersPermissions(storeId));
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> respondToUserMessage(String sessionToken, String storeId, int messageId, String response) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.respondToUserMessage(storeId, messageId, response);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<List<IShoppingBasket>> getStorePurchaseHistory(String sessionToken, String storeId, LocalDateTime from, LocalDateTime to) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            return new Response<>(marketFacade.getStorePurchaseHistory(storeId, from, to));
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> openMarket(String sessionToken) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.openMarket();
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }
}
