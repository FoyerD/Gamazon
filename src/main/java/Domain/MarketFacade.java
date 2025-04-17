package Domain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import javax.print.DocFlavor.STRING;

import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.IShoppingBasket;
import Domain.Store.IItemRepository;
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
    private IItemRepository itemRepository; // Assuming this is defined somewhere in your code

    // In-memory permissions store: storeId -> (username -> Permission)
    private final Map<String, Map<String, Permission>> storePermissions = new HashMap<>();

    private static final MarketFacade INSTANCE = new MarketFacade();

    public static synchronized MarketFacade getInstance() {
        return INSTANCE;
    }

    private MarketFacade() {}

    @Override
    public void initFacades(IUserRepository userFacade, IStoreRepository storeFacade, IItemRepository itemFacade) {
        this.userRepository = userFacade;
        this.storeRepository = storeFacade;
        this.itemRepository = itemFacade;
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
    public void manageStoreInventory(String storeId, Map<Integer, Integer> productQuantities) {
        Store store = storeRepository.get(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store with ID " + storeId + " does not exist.");
        }
        for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();
            Item item = itemRepository.getItem(storeId, String.valueOf(productId));
            if (item == null) {
                throw new IllegalArgumentException("Product with ID " + productId + " does not exist in store " + storeId);
            }
            item.setAmount(quantity);
            itemRepository.update(new Pair<>(storeId, String.valueOf(productId)), item);
        }
        System.out.println("Inventory for store " + storeId + " has been successfully updated.");
    }

    @Override
    public void appointStoreManager(String appointerUsername, String appointeeUsername, String storeId) {
        checkPermission(appointerUsername, storeId, PermissionType.SUPERVISE_MANAGERS);
        Permission perm = getOrCreatePermission(appointerUsername, appointeeUsername, storeId);
        perm.initStoreManager();
    }

    @Override
    public void removeStoreManager(String removerUsername, String managerUsername, String storeId) {
        checkPermission(removerUsername, storeId, PermissionType.SUPERVISE_MANAGERS);
        Permission permission = getPermissionOrThrow(managerUsername, storeId);
        if (!permission.isStoreManager()) 
            throw new IllegalStateException(managerUsername + " is not a manager.");
        permission.setPermissions(Set.of());
        permission.setRole(null);
    }

    @Override
    public void appointStoreOwner(String appointerUsername, String appointeeUsername, String storeId) {
        checkPermission(appointerUsername, storeId, PermissionType.ASSIGN_OR_REMOVE_OWNERS);
        Permission perm = getOrCreatePermission(appointerUsername, appointeeUsername, storeId);
        perm.initStoreOwner();
    }

    @Override
    public void changeManagerPermissions(String ownerUsername, String managerUsername, String storeId, List<PermissionType> newPermissions) {
        checkPermission(ownerUsername, storeId, PermissionType.MODIFY_OWNER_RIGHTS);
        Permission permission = getPermissionOrThrow(managerUsername, storeId);
        if (!permission.isStoreManager()) 
            throw new IllegalStateException(managerUsername + " is not a manager.");
        permission.setPermissions(PermissionType.collectionToSet(newPermissions));
    }

    @Override
    public void closeStore(String storeId) {
        User marketManager = userRepository.getMarketManager();
        if (marketManager == null) {
            throw new IllegalStateException("Market manager is not assigned.");
        }
        checkPermission(marketManager.getName(), storeId, PermissionType.DEACTIVATE_STORE);
        Store store = storeRepository.get(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store not found.");
        }
        store.setOpen(false);
        notificationService.sendNotification(
            marketManager.getName(),
            "Store " + storeId + " has been closed."
        );
        System.out.println("Store " + storeId + " has been successfully closed by the market manager.");
    }

    @Override
    public void marketCloseStore(String storeId) {
        User marketManager = userRepository.getMarketManager();
        if (marketManager == null) {
            throw new IllegalStateException("Market manager is not assigned.");
        }
        checkPermission(marketManager.getName(), storeId, PermissionType.DEACTIVATE_STORE);
        Store store = storeRepository.get(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store not found.");
        }
        // TODO: Amit should do it?
        //store.cancelSubscriptions();
        store.setOpen(false);
        notificationService.sendNotification(
            marketManager.getName(),
            "Store " + storeId + " has been closed."
        );
        System.out.println("Store " + storeId + " has been successfully closed by the market manager.");
    }

    @Override
    public Map<String, List<PermissionType>> getManagersPermissions(String storeId) {
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
    public void respondToUserMessage(String storeId, int messageId, String response) {
        Store store = storeRepository.get(storeId);
        // TODO: Amit should do it?
        //store.respondToMessage(messageId, response);
    }

    @Override
    public List<IShoppingBasket> getStorePurchaseHistory(String storeId, LocalDateTime from, LocalDateTime to) {
        // TODO: Amit or Aviad should do it?
        //return storeRepository.get(storeId).getStorePurchaseHistory(from, to);
        return null; // Placeholder for actual implementation
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
        Permission founder = new Permission("system", manager.getName());
        founder.initTradingManager();
        storePermissions.putIfAbsent("market", new HashMap<>());
        storePermissions.get("market").put(manager.getName(), founder);
        System.out.println("Market opened.");
    }

    private Permission getOrCreatePermission(String giver, String member, String storeId) {
        storePermissions.putIfAbsent(storeId, new HashMap<>());
        return storePermissions.get(storeId).computeIfAbsent(member, u -> new Permission(giver, member));
    }

    private Permission getPermissionOrThrow(String username, String storeId) {
        Map<String, Permission> perms = storePermissions.get(storeId);
        if (perms == null || !perms.containsKey(username)) {
            throw new IllegalArgumentException("No permissions found for user " + username + " in store " + storeId);
        }
        return perms.get(username);
    }

    private void checkPermission(String username, String storeId, PermissionType requiredPermission) {
        Permission permission = getPermissionOrThrow(username, storeId);
        if (!permission.hasPermission(requiredPermission)) {
            throw new SecurityException("User " + username + " lacks permission " + requiredPermission + " for store " + storeId);
        }
    }
}
