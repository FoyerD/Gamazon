package Domain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.IShoppingBasket;
import Domain.Store.IStoreRepository;
import Domain.Store.Item;
import Domain.Store.Store;
import Domain.User.IUserRepository;
import Domain.User.User;

public class MarketFacade implements IMarketFacade {

    private IPaymentService paymentService;
    private ISupplyService supplyService;
    private INotificationService notificationService;
    private IUserRepository userRepository;
    private IStoreRepository storeRepository;

    // In-memory permissions store: storeId -> (username -> Permission)
    private final Map<Integer, Map<String, Permission>> storePermissions = new HashMap<>();

    private static final MarketFacade INSTANCE = new MarketFacade();

    public static synchronized MarketFacade getInstance() {
        return INSTANCE;
    }

    private MarketFacade() {}

    @Override
    public void initFacades(IUserRepository userFacade, IStoreRepository storeFacade) {
        this.userRepository = userFacade;
        this.storeRepository = storeFacade;
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
    public void manageStoreInventory(int storeId, Map<Integer, Integer> productQuantities) {
        Store store = storeRepository.getStore(storeId);
        if (store == null) 
            throw new IllegalArgumentException("Store not found.");
        for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
            Item item = store.getItemById(entry.getKey());
            if (item != null) 
                item.setQuantity(entry.getValue());
        }
    }

    @Override
    public void appointStoreManager(String appointerUsername, String appointeeUsername, int storeId) {
        checkPermission(appointerUsername, storeId, PermissionType.SUPERVISE_MANAGERS);
        Permission perm = getOrCreatePermission(appointerUsername, appointeeUsername, storeId);
        perm.initStoreManager();
    }

    @Override
    public void removeStoreManager(String removerUsername, String managerUsername, int storeId) {
        checkPermission(removerUsername, storeId, PermissionType.SUPERVISE_MANAGERS);
        Permission permission = getPermissionOrThrow(managerUsername, storeId);
        if (!permission.isStoreManager()) 
            throw new IllegalStateException(managerUsername + " is not a manager.");
        permission.setPermissions(Set.of());
        permission.setRole(null);
    }

    @Override
    public void appointStoreOwner(String appointerUsername, String appointeeUsername, int storeId) {
        checkPermission(appointerUsername, storeId, PermissionType.ASSIGN_OR_REMOVE_OWNERS);
        Permission perm = getOrCreatePermission(appointerUsername, appointeeUsername, storeId);
        perm.initStoreOwner();
    }

    @Override
    public void changeManagerPermissions(String ownerUsername, String managerUsername, int storeId, List<PermissionType> newPermissions) {
        checkPermission(ownerUsername, storeId, PermissionType.MODIFY_OWNER_RIGHTS);
        Permission permission = getPermissionOrThrow(managerUsername, storeId);
        if (!permission.isStoreManager()) 
            throw new IllegalStateException(managerUsername + " is not a manager.");
        permission.setPermissions(PermissionType.collectionToSet(newPermissions));
    }

    @Override
    public void closeStore(int storeId, User user) {
        checkPermission(user.getUserName(), storeId, PermissionType.DEACTIVATE_STORE);
        Store store = storeRepository.getStore(storeId);
        if (store == null) 
            throw new IllegalArgumentException("Store not found.");
        storeRepository.closeStore(String.valueOf(storeId));
        notificationService.sendNotification(user.getUserName(), "Store " + storeId + " has been closed.");
    }

    @Override
    public void marketCloseStore(int storeId, User user) {
        checkPermission(user.getUserName(), storeId, PermissionType.DEACTIVATE_STORE);
        Store store = storeRepository.getStore(storeId);
        if (store == null) 
            throw new IllegalArgumentException("Store not found.");
        store.cancelSubscriptions();
        storeRepository.closeStore(String.valueOf(storeId));
        notificationService.sendNotification(user.getUserName(), "Store " + storeId + " has been closed.");
    }

    @Override
    public Map<String, List<PermissionType>> getManagersPermissions(int storeId) {
        Map<String, List<PermissionType>> result = new HashMap<>();
        Map<String, Permission> storeMap = storePermissions.get(storeId);
        if (storeMap != null) {
            for (var entry : storeMap.entrySet()) {
                if (entry.getValue().isStoreManager()) {
                    result.put(entry.getKey(), List.copyOf(entry.getValue().getPermissions()));
                }
            }
        }
        return result;
    }

    @Override
    public void respondToUserMessage(int storeId, int messageId, String response) {
        Store store = storeRepository.getStore(storeId);
        store.respondToMessage(messageId, response);
    }

    @Override
    public List<IShoppingBasket> getStorePurchaseHistory(int storeId, LocalDateTime from, LocalDateTime to) {
        return storeRepository.getStore(storeId).getStorePurchaseHistory(from, to);
    }

    @Override
    public void openMarket() {
        if (paymentService == null || supplyService == null || notificationService == null || userRepository == null) {
            throw new IllegalStateException("Services not initialized.");
        }
        paymentService.initialize();
        supplyService.initialize();
        notificationService.initialize();
        User manager = userRepository.getMarketManager();
        Permission founder = new Permission("system", manager.getUserName());
        founder.initStoreFounder();
        storePermissions.computeIfAbsent(-1, x -> new HashMap<>()).put(manager.getUserName(), founder);
        System.out.println("Market opened.");
    }

    private Permission getOrCreatePermission(String giver, String member, int storeId) {
        storePermissions.putIfAbsent(storeId, new HashMap<>());
        return storePermissions.get(storeId).computeIfAbsent(member, u -> new Permission(giver, member));
    }

    private Permission getPermissionOrThrow(String username, int storeId) {
        Map<String, Permission> perms = storePermissions.get(storeId);
        if (perms == null || !perms.containsKey(username)) {
            throw new IllegalArgumentException("No permissions found for user " + username + " in store " + storeId);
        }
        return perms.get(username);
    }

    private void checkPermission(String username, int storeId, PermissionType requiredPermission) {
        Permission permission = getPermissionOrThrow(username, storeId);
        if (!permission.hasPermission(requiredPermission)) {
            throw new SecurityException("User " + username + " lacks permission " + requiredPermission + " for store " + storeId);
        }
    }
}
