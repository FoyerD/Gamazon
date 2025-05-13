package Domain.management;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Domain.Pair;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.Receipt;
import Domain.Store.Feedback;
import Domain.Store.IItemRepository;
import Domain.Store.Item;
import Domain.Store.Store;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;
import Domain.User.Member;
import Infrastructure.Repositories.MemoryPermissionRepository;

@Component
public class MarketFacade implements IMarketFacade {

    private IPaymentService paymentService;
    private ISupplyService supplyService;
    private INotificationService notificationService;
    private IUserRepository userRepository;
    private IItemRepository itemRepository; 
    private StoreFacade storeFacade; 
    private IShoppingCartFacade shoppingCartFacade;


    private IPermissionRepository permissionRepository = new MemoryPermissionRepository();

    private static final MarketFacade INSTANCE = new MarketFacade();

    public static synchronized MarketFacade getInstance() {
        return INSTANCE;
    }

    private MarketFacade() {}

    @Override
    public void initFacades(IUserRepository userRepository, IItemRepository itemRepository, StoreFacade storeFacade, IShoppingCartFacade shoppingCartFacade) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.storeFacade = storeFacade;
        this.shoppingCartFacade = shoppingCartFacade;
    }

    // For unit testing
    public Map<String, Map<String, Permission>> getStorePermissions() {
        return permissionRepository.getAllPermissions();
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
    public void addProductsToInventory(String storeId, Map<String, Integer> productQuantities, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.HANDLE_INVENTORY);
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();
            Item existingItem = itemRepository.getItem(storeId, productId);
            if (existingItem == null) {
                Item newItem = new Item(storeId, String.valueOf(productId), 0, quantity, "New product");
                itemRepository.add(new Pair<>(storeId, productId), newItem);
            }
        }
    }

    @Override
    public void updateProductQuantities(String storeId, Map<String, Integer> productQuantities, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.HANDLE_INVENTORY);
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();
            Item existingItem = itemRepository.getItem(storeId, productId);
            if (existingItem != null) {
                existingItem.setAmount(quantity);
                itemRepository.update(new Pair<>(storeId, productId), existingItem);
            }
        }
    }

    @Override
    public void removeProductsFromInventory(String storeId, Map<String, Integer> productQuantities, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.HANDLE_INVENTORY);
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();
            Item existingItem = itemRepository.getItem(storeId, productId);
            if (existingItem != null && quantity > 0) {
                itemRepository.remove(new Pair<>(storeId, productId));
            }
        }
    }

    @Override
    public void appointStoreManager(String appointerUsername, String appointeeUsername, String storeId) {
        checkPermission(appointerUsername, storeId, PermissionType.SUPERVISE_MANAGERS);
        getOrCreatePermission(appointerUsername, appointeeUsername, storeId, RoleType.STORE_MANAGER);
    }

    @Override
    public void removeStoreManager(String removerUsername, String managerUsername, String storeId) {
        checkPermission(removerUsername, storeId, PermissionType.SUPERVISE_MANAGERS);
        Permission permission = permissionRepository.get(storeId, managerUsername);
        if (permission == null || !permission.isStoreManager()) {
            throw new IllegalStateException(managerUsername + " is not a manager.");
        }
        permission.setPermissions(Set.of());
        permission.setRole(null);
        permissionRepository.update(storeId, managerUsername, permission);
    }

    @Override
    public void appointStoreOwner(String appointerUsername, String appointeeUsername, String storeId) {
        checkPermission(appointerUsername, storeId, PermissionType.ASSIGN_OR_REMOVE_OWNERS);
        getOrCreatePermission(appointerUsername, appointeeUsername, storeId, RoleType.STORE_OWNER);
    }

    // Currently assigns the first store owner as the permission giver of himself
    private void appointFirstStoreOwner(String appointeeUsername, String storeId) {
        getOrCreatePermission(appointeeUsername, appointeeUsername, storeId, RoleType.STORE_OWNER);
    }


    @Override
    public void changeManagerPermissions(String ownerUsername, String managerUsername, String storeId, List<PermissionType> newPermissions) {
        checkPermission(ownerUsername, storeId, PermissionType.MODIFY_OWNER_RIGHTS);
        Permission permission = permissionRepository.get(storeId, managerUsername);
        if (permission == null || !permission.isStoreManager()) {
            throw new IllegalStateException(managerUsername + " is not a manager.");
        }
        permission.setPermissions(PermissionType.collectionToSet(newPermissions));
        permissionRepository.update(storeId, managerUsername, permission);
    }

    @Override
    public Store addStore(String name, String description, String founderId) {
        Member storeOwner = userRepository.getMember(founderId);
        if (storeOwner == null) {
            throw new IllegalStateException("User does not exist.");
        }
        Store store = storeFacade.addStore(name, description, founderId);
        String storeId = store.getId();
        appointFirstStoreOwner(userRepository.getMember(founderId).getName(), storeId);
        return store;
    }

    @Override
    public void closeStore(String storeId, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.DEACTIVATE_STORE);
        storeFacade.closeStore(storeId);
        Member manager = userRepository.getMemberByUsername(userRepository.get(userId).getName());
        notificationService.sendNotification(manager.getName(), "Store " + storeId + " has been closed.");
    }

    @Override
    public void marketCloseStore(String storeId, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.DEACTIVATE_STORE);
        Member manager = userRepository.getMemberByUsername(userRepository.get(userId).getName());
        Map<String, Permission> storePermissions = permissionRepository.getAllPermissionsForStore(storeId);
        if (storePermissions != null) {
            for (Map.Entry<String, Permission> entry : storePermissions.entrySet()) {
                String username = entry.getKey();
                Permission permission = entry.getValue();
                if (permission.isStoreManager() || permission.isStoreOwner() || permission.isStoreFounder()) {
                    permission.setPermissions(Set.of());
                    permission.setRole(null);
                    permissionRepository.update(storeId, username, permission);
                    notificationService.sendNotification(username, "Store " + storeId + " has been closed.");
                }
            }
        }
        storeFacade.closeStore(storeId);
        notificationService.sendNotification(manager.getName(), "Store " + storeId + " has been closed.");
    }

    @Override
    public Map<String, List<PermissionType>> getManagersPermissions(String storeId, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.SUPERVISE_MANAGERS);
        Map<String, List<PermissionType>> result = new HashMap<>();
        Map<String, Permission> storePermissions = permissionRepository.getAllPermissionsForStore(storeId);
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
    public boolean respondToUserMessage(String storeId, String productId, String userId, String response) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.RESPOND_TO_INQUIRIES);
        return storeFacade.addFeedback(storeId, productId, userId, response);
    }

    @Override
    public Feedback getUserMessage(String storeId, String userId, String feedbackId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.OVERSEE_OFFERS);
        return storeFacade.getFeedback(feedbackId);
    }

    @Override
    public List<Receipt> getStorePurchaseHistory(String storeId, String userId) {
        checkPermission(userRepository.get(userId).getName(), storeId, PermissionType.ACCESS_PURCHASE_RECORDS);
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
        Permission founder = new Permission("system", manager.getName());
        PermissionFactory.initPermissionAsRole(founder, RoleType.TRADING_MANAGER);
        permissionRepository.add("1", manager.getName(), founder);
    }

    private Permission getOrCreatePermission(String giver, String member, String storeId, RoleType role) {
        Permission permission = permissionRepository.get(storeId, member);
        if (permission == null) {
            permission = new Permission(giver, member);
            PermissionFactory.initPermissionAsRole(permission, role);
            permissionRepository.add(storeId, member, permission);
        }
        return permission;
    }

    private void checkPermission(String username, String storeId, PermissionType requiredPermission) {
        Permission permission = permissionRepository.get(storeId, username);
        if (permission == null || !permission.hasPermission(requiredPermission)) {
            throw new SecurityException("User " + username + " lacks permission " + requiredPermission + " for store " + storeId);
        }
    }
}
