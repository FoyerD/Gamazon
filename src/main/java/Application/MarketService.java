package Application;

import Domain.IMarketFacade;
import Domain.User.User;
import Domain.User.IUserRepository;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.PermissionType;
import Domain.Store.IStoreRepository;
import Domain.Shopping.IShoppingBasket;
import Domain.Store.Item;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MarketService {

    private final IMarketFacade marketFacade;
    private final IUserRepository userFacade;

    public MarketService(IMarketFacade marketFacade, IUserRepository userFacade) {
        this.marketFacade = marketFacade;
        this.userFacade = userFacade;
    }


    public Response<Void> updatePaymentService(IPaymentService paymentService) {
        try {
            marketFacade.updatePaymentService(paymentService);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updateNotificationService(INotificationService notificationService) {
        try {
            marketFacade.updateNotificationService(notificationService);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updateSupplyService(ISupplyService supplyService) {
        try {
            marketFacade.updateSupplyService(supplyService);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> updatePaymentServiceURL(String url) {
        try {
            marketFacade.updatePaymentServiceURL(url);
            return new Response<>(null);
        } catch (IOException e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> initFacades(IUserRepository userFacade, IStoreRepository storeFacade) {
        try {
            marketFacade.initFacades(userFacade, storeFacade);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<INotificationService> getNotificationService() {
        try {
            return new Response<>(marketFacade.getNotificationService());
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> manageStoreInventory(int storeId, Map<Integer, Integer> productQuantities) {
        try {
            marketFacade.manageStoreInventory(storeId, productQuantities);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> appointStoreManager(String appointerUsername, String appointeeUsername, int storeId) {
        try {
            marketFacade.appointStoreManager(appointerUsername, appointeeUsername, storeId);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> removeStoreManager(String removerUsername, String managerUsername, int storeId) {
        try {
            marketFacade.removeStoreManager(removerUsername, managerUsername, storeId);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> appointStoreOwner(String appointerUsername, String appointeeUsername, int storeId) {
        try {
            marketFacade.appointStoreOwner(appointerUsername, appointeeUsername, storeId);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> changeManagerPermissions(String ownerUsername, String managerUsername, int storeId, List<PermissionType> newPermissions) {
        try {
            marketFacade.changeManagerPermissions(ownerUsername, managerUsername, storeId, newPermissions);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> closeStore(int storeId, User user) {
        try {
            marketFacade.closeStore(storeId, user);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> marketCloseStore(int storeId, User user) {
        try {
            marketFacade.marketCloseStore(storeId, user);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Map<String, List<PermissionType>>> getManagersPermissions(int storeId) {
        try {
            return new Response<>(marketFacade.getManagersPermissions(storeId));
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> respondToUserMessage(int storeId, int messageId, String response) {
        try {
            marketFacade.respondToUserMessage(storeId, messageId, response);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<List<IShoppingBasket>> getStorePurchaseHistory(int storeId, LocalDateTime from, LocalDateTime to) {
        try {
            return new Response<>(marketFacade.getStorePurchaseHistory(storeId, from, to));
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }

    public Response<Void> openMarket() {
        try {
            marketFacade.openMarket();
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(new Error(e.getMessage()));
        }
    }
}
