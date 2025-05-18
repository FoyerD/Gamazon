package Domain.management;

import java.io.IOException;
import java.util.*;
import org.springframework.stereotype.Component;

import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.Receipt;
import Domain.User.IUserRepository;
import Domain.User.Member;

@Component
public class MarketFacade implements IMarketFacade {

    private IPaymentService paymentService;
    private ISupplyService supplyService;
    private INotificationService notificationService;
    private IUserRepository userRepository;
    private PermissionManager permissionManager;
    private IShoppingCartFacade shoppingCartFacade;


    private static final MarketFacade INSTANCE = new MarketFacade();

    public static synchronized MarketFacade getInstance() {
        return INSTANCE;
    }

    private MarketFacade() {}

    @Override
    public void initFacades(IUserRepository userRepository, IShoppingCartFacade shoppingCartFacade, PermissionManager permissionManager) {
        this.userRepository = userRepository;
        this.shoppingCartFacade = shoppingCartFacade;
        this.permissionManager = permissionManager;
    }

    // For unit testing
    public Map<String, Map<String, Permission>> getStorePermissions() {
        return permissionManager.getAllStorePermissions();
    }

    @Override
    public void updatePaymentService(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public void updateNotificationService(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void updateSupplyService(ISupplyService supplyService) {
        this.supplyService = supplyService;
    }

    @Override
    public void updatePaymentServiceURL(String url) throws IOException {
        paymentService.updatePaymentServiceURL(url);
    }

    @Override
    public INotificationService getNotificationService() {
        return notificationService;
    }


    @Override
    public void appointStoreManager(String appointerId, String appointeeId, String storeId) {
        permissionManager.appointStoreManager(appointerId, appointeeId, storeId);
    }

    @Override
    public void removeStoreManager(String removerId, String managerId, String storeId) {
        permissionManager.removeStoreManager(removerId, managerId, storeId);
    }

    @Override
    public void appointStoreOwner(String appointerId, String appointeeId, String storeId) {
        permissionManager.appointStoreOwner(appointerId, appointeeId, storeId);
    }

    @Override
    public void changeManagerPermissions(String ownerId, String managerId, String storeId, List<PermissionType> newPermissions) {
        permissionManager.changeManagerPermissions(ownerId, managerId, storeId, newPermissions);
    }

    @Override
    public Map<String, List<PermissionType>> getManagersPermissions(String storeId, String userId) {
        permissionManager.checkPermission(userId, storeId, PermissionType.SUPERVISE_MANAGERS);
        Map<String, List<PermissionType>> result = new HashMap<>();
        Map<String, Permission> storePermissions = permissionManager.getAllPermissionsForStore(storeId);
        if (storePermissions != null) {
            for (Map.Entry<String, Permission> entry : storePermissions.entrySet()) {
                String username = entry.getKey();
                Permission p = entry.getValue();
                if (p.isStoreManager()) {
                    result.put(username, List.copyOf(p.getPermissions()));
                }
            }
        }
        return result;
    }


    @Override
    public List<Receipt> getStorePurchaseHistory(String storeId, String userId) {
        permissionManager.checkPermission(userId, storeId, PermissionType.ACCESS_PURCHASE_RECORDS);
        return shoppingCartFacade.getStorePurchaseHistory(storeId);
    }

    @Override
    public void openMarket(String userId) {
        if (paymentService == null || supplyService == null || notificationService == null || userRepository == null) {
            throw new IllegalStateException("Services not initialized.");
        }
        paymentService.initialize();
        supplyService.initialize();
        notificationService.initialize();
        Member manager = userRepository.getMember(userId);
        permissionManager.addMarketManager(manager);
    }
}
