package Domain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.Feedback;
import Domain.Store.IItemRepository;
import Domain.Store.Item;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;
import Domain.User.User;
import Domain.User.Member;

public class MarketFacade implements IMarketFacade {

    private IPaymentService paymentService;
    private ISupplyService supplyService;
    private INotificationService notificationService;
    private IUserRepository userRepository;
    private IItemRepository itemRepository; 
    private StoreFacade storeFacade; 

    // In-memory permissions store: storeId -> (username -> Permission)
    private final Map<String, Map<String, Permission>> storePermissions = new HashMap<>();

    private static final MarketFacade INSTANCE = new MarketFacade();

    public static synchronized MarketFacade getInstance() {
        return INSTANCE;
    }

    private MarketFacade() {}

    @Override
    public void initFacades(IUserRepository userRepository, IItemRepository itemRepository, StoreFacade storeFacade) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.storeFacade = storeFacade;
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
    public void addProductsToInventory(String storeId, Map<Integer, Integer> productQuantities, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.HANDLE_INVENTORY);
        for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();
            Item existingItem = itemRepository.getItem(storeId, String.valueOf(productId));
            if (existingItem == null) {
                Item newItem = new Item(storeId, String.valueOf(productId), 0, quantity, "New product");
                itemRepository.add(new Pair<>(storeId, String.valueOf(productId)), newItem);
                System.out.println("Added product " + productId + " with quantity " + quantity + " to store " + storeId);
            }
        }
    }
    
    @Override
    public void updateProductQuantities(String storeId, Map<Integer, Integer> productQuantities, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.HANDLE_INVENTORY);
        for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();    
            Item existingItem = itemRepository.getItem(storeId, String.valueOf(productId));
            if (existingItem != null) {
                existingItem.setAmount(quantity);
                itemRepository.update(new Pair<>(storeId, String.valueOf(productId)), existingItem);
                System.out.println("Updated product " + productId + " with new quantity " + quantity + " in store " + storeId);
            }
        }
    }
    
    @Override
    public void removeProductsFromInventory(String storeId, Map<Integer, Integer> productQuantities, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.HANDLE_INVENTORY);
        for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();
            Item existingItem = itemRepository.getItem(storeId, String.valueOf(productId));
            if (existingItem != null && quantity > 0) {
                itemRepository.remove(new Pair<>(storeId, String.valueOf(productId)));
                System.out.println("Removed product " + productId + " from store " + storeId);
            }
        }
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
    public void closeStore(String storeId, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.DEACTIVATE_STORE);
        Member marketManager = userRepository.getMemberByUsername(userRepository.get(userId).getName());
        if (marketManager == null) {
            throw new IllegalStateException("Market manager is not assigned.");
        }
        storeFacade.closeStore(storeId); 
        notificationService.sendNotification(
            marketManager.getName(),
            "Store " + storeId + " has been closed."
        );
        System.out.println("Store " + storeId + " has been successfully closed by the market manager.");
    }

    @Override
    public void marketCloseStore(String storeId, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.DEACTIVATE_STORE);
        Member marketManager = userRepository.getMemberByUsername(userRepository.get(userId).getName());
        if (marketManager == null) {
            throw new IllegalStateException("Market manager is not assigned.");
        }
        // Remove all users from the store permissions
        for (Map.Entry<String, Permission> entry : storePermissions.get(storeId).entrySet()) {
            String username = entry.getKey();
            Permission permission = entry.getValue();
            if (permission.isStoreManager() || permission.isStoreOwner() || permission.isStoreFounder()) {
                // Remove permissions for store managers and owners
                permission.setPermissions(Set.of());
                permission.setRole(null);
            }
            notificationService.sendNotification(
                username,
                "Store " + storeId + " has been closed."
            );
        }
        storeFacade.closeStore(storeId); 
        notificationService.sendNotification(
            marketManager.getName(),
            "Store " + storeId + " has been closed."
        );
    }

    @Override
    public Map<String, List<PermissionType>> getManagersPermissions(String storeId, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.SUPERVISE_MANAGERS);
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
    public boolean respondToUserMessage(String storeId, String productId, String userId, String response) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.RESPOND_TO_INQUIRIES);
        return storeFacade.addFeedback(storeId, productId, userId, response);
    }

    @Override
    public Feedback getUserMessage(String storeId, String productId, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.OVERSEE_OFFERS);
        return storeFacade.getFeedback(storeId, productId, userId);
    }

    @Override
    public List<ShoppingBasket> getStorePurchaseHistory(String storeId, LocalDateTime from, LocalDateTime to, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.ACCESS_PURCHASE_RECORDS);
        // TODO: Aviad should do it
        //return storeRepository.get(storeId).getStorePurchaseHistory(from, to);
        return null; // Placeholder for actual implementation
    }

    @Override
    public void openMarket(String userId) {
        if (paymentService == null || supplyService == null || notificationService == null || userRepository == null) {
            throw new IllegalStateException("Services not initialized.");
        }
        paymentService.initialize();
        supplyService.initialize();
        notificationService.initialize();
        Member marketManager = userRepository.getMemberByUsername(userId);        
        Permission founder = new Permission("system", marketManager.getName());
        founder.initTradingManager();
        storePermissions.putIfAbsent("market", new HashMap<>());
        storePermissions.get("market").put(marketManager.getName(), founder);
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
