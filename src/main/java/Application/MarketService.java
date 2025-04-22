package Application;

import Domain.IMarketFacade;
import Domain.User.IUserRepository;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.PermissionType;
import Domain.Store.Feedback;
import Domain.Store.IItemRepository;
import Domain.Store.StoreFacade;
import Domain.Shopping.ShoppingBasket;
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

    public Response<Void> initFacades(String sessionToken, IUserRepository userRepository, IItemRepository itemRepository, StoreFacade storeFacade) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.initFacades(userRepository, itemRepository, storeFacade);
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
            marketFacade.addProductsToInventory(storeId, productQuantities, tokenService.extractId(sessionToken));
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updateProductQuantities(String sessionToken, String storeId, Map<Integer, Integer> productQuantities) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.updateProductQuantities(storeId, productQuantities, tokenService.extractId(sessionToken));
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> removeProductsFromInventory(String sessionToken, String storeId, Map<Integer, Integer> productQuantities) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.removeProductsFromInventory(storeId, productQuantities, tokenService.extractId(sessionToken));
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
            marketFacade.closeStore(storeId, tokenService.extractId(sessionToken));
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> marketCloseStore(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.marketCloseStore(storeId, tokenService.extractId(sessionToken));
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Map<String, List<PermissionType>>> getManagersPermissions(String sessionToken, String storeId) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            return new Response<>(marketFacade.getManagersPermissions(storeId, tokenService.extractId(sessionToken)));
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Boolean> respondToUserMessage(String sessionToken, String storeId, String productId, String userId, String comment) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            return new Response<>(marketFacade.respondToUserMessage(storeId, productId, userId, comment));
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Feedback> getUserMessage(String sessionToken, String storeId, String productId, String userId) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            return new Response<>(marketFacade.getUserMessage(storeId, productId, userId));
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<List<ShoppingBasket>> getStorePurchaseHistory(String sessionToken, String storeId, LocalDateTime from, LocalDateTime to) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            return new Response<>(marketFacade.getStorePurchaseHistory(storeId, from, to, tokenService.extractId(sessionToken)));
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> openMarket(String sessionToken) {
        if (isInvalid(sessionToken)) 
            return new Response<>(new Error("Invalid session token"));
        try {
            marketFacade.openMarket(tokenService.extractId(sessionToken));
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }
}
