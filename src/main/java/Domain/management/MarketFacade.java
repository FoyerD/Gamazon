package Domain.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.utils.Response;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.ExternalServices.INotificationService;
import Domain.Repos.IStoreRepository;
import Domain.Repos.IUserRepository;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.Receipt;
import Domain.Store.Store;
import Domain.User.Member;


@Component
public class MarketFacade implements IMarketFacade {

    private IExternalPaymentService paymentService;
    private IExternalSupplyService supplyService;
    private INotificationService notificationService;
    private IUserRepository userRepository;
    private PermissionManager permissionManager;
    private IShoppingCartFacade shoppingCartFacade;
    private IStoreRepository storeRepository;


    private static final MarketFacade INSTANCE = new MarketFacade();

    public static synchronized MarketFacade getInstance() {
        return INSTANCE;
    }

    @Autowired
    public MarketFacade(IExternalPaymentService paymentService,
                        IExternalSupplyService supplyService,
                        INotificationService notificationService,
                        IUserRepository userRepository,
                        IShoppingCartFacade shoppingCartFacade,
                        PermissionManager permissionManager,
                        IStoreRepository storeRepository) {
        this.paymentService = paymentService;
        this.supplyService = supplyService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.shoppingCartFacade = shoppingCartFacade;
        this.permissionManager = permissionManager;
        this.storeRepository = storeRepository;
    }

    //Ask Amit if this is needed
    private MarketFacade() {}

    @Override
    public void initFacades(IUserRepository userRepository, IStoreRepository storeRepository, IShoppingCartFacade shoppingCartFacade, PermissionManager permissionManager) {
        this.userRepository = userRepository;
        this.shoppingCartFacade = shoppingCartFacade;
        this.permissionManager = permissionManager;
        this.storeRepository = storeRepository;
    }

    // For unit testing
    public Map<String, Map<String, Permission>> getStorePermissions() {
        return permissionManager.getAllStorePermissions();
    }

    @Override
    public void updatePaymentService(IExternalPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public void updateNotificationService(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void updateSupplyService(IExternalSupplyService supplyService) {
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
    public boolean userExists(String username) {
        return userRepository.getMemberByUsername(username) != null;
    }

    @Override
    public String getUsername(String userId) {
        Member member = userRepository.getMember(userId);
        if (member == null) {
            throw new NoSuchElementException("User not found: " + userId);
        }
        return member.getName();
    }

    @Override
    public void appointStoreManager(String appointerId, String appointeeId, String storeId) {
        if (userRepository.getMember(appointeeId) == null) {
            throw new IllegalArgumentException("Appointee not found.");
        } else if (storeRepository.get(storeId) == null) {
            throw new IllegalArgumentException("Store not found: " + storeId);
        } else if (isStoreOwner(appointeeId, storeId)) {
            throw new IllegalArgumentException("Appointee is already a store owner.");
        }
        else if (isStoreManager(appointeeId, storeId)) {
            throw new IllegalArgumentException("Appointee is already a store manager.");
        }
        permissionManager.appointStoreManager(appointerId, appointeeId, storeId);
        Store store = storeRepository.get(storeId);
        store.addManager(appointeeId);
        storeRepository.update(storeId, store);
    }

    @Override
    public void removeStoreOwner(String removerId, String ownerId, String storeId) {
        permissionManager.removeStoreOwner(removerId, ownerId, storeId);
    }

    @Override
    public void appointStoreOwner(String appointerId, String appointeeId, String storeId) {
        if (userRepository.getMember(appointeeId) == null) {
            throw new IllegalArgumentException("Appointee not found.");
        } else if (isStoreOwner(appointeeId, storeId)) {
            throw new IllegalArgumentException("Appointee is already a store owner.");
        }
        permissionManager.appointStoreOwner(appointerId, appointeeId, storeId);
    }

    @Override
    public void changeManagerPermissions(String ownerId, String managerId, String storeId, List<PermissionType> newPermissions) {
        permissionManager.changeManagerPermissions(ownerId, managerId, storeId, newPermissions);
    }

    @Override
    public Map<Member, List<PermissionType>> getManagersPermissions(String storeId, String userId) {
        permissionManager.checkPermission(userId, storeId, PermissionType.SUPERVISE_MANAGERS);
        Map<Member, List<PermissionType>> result = new HashMap<>();
        Map<String, Permission> storePermissions = permissionManager.getAllPermissionsForStore(storeId);
        if (storePermissions != null) {
            for (Map.Entry<String, Permission> entry : storePermissions.entrySet()) {
                String username = entry.getKey();
                Permission p = entry.getValue();
                if (p.isStoreManager()) {
                    Member member = userRepository.getMember(username);
                    if (member == null) {
                        throw new NoSuchElementException("Member not found: " + username);
                    }
                    result.put(member, List.copyOf(p.getPermissions()));
                }
            }
        }
        return result;
    }

    @Override
    public List<Member> getOwners(String storeId, String userId) {
        permissionManager.checkPermission(userId, storeId, PermissionType.SUPERVISE_MANAGERS);
        List<String> ownersIds = permissionManager.getAllPermissionsForStore(storeId).entrySet()
                                .stream().filter(entry -> entry.getValue().isStoreOwner()).map(entry -> entry.getKey()).toList();

        List<Member> members = new ArrayList<>();
        for (String id : ownersIds) {
            Member member = userRepository.getMember(id);
            if (member == null) {
                throw new NoSuchElementException("Member not found: " + userId);
            }
            members.add(member);
        }
        return members;
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
        
        Response<Boolean> paymentCheck = paymentService.handshake();
        Response<Boolean> supplyCheck = supplyService.handshake();
        System.out.println("Handshake with external services");

        if (paymentCheck.errorOccurred() || supplyCheck.errorOccurred() ||
            !Boolean.TRUE.equals(paymentCheck.getValue()) ||
            !Boolean.TRUE.equals(supplyCheck.getValue())) {
            //TODO: remove when ready to use VPN :(
            throw new IllegalStateException("Handshake failed with external API.");
        }


        Member manager = userRepository.getMember(userId);
        permissionManager.addMarketManager(manager);
    }

    /**
     * Checks if a user is a store manager for the specified store.
     * 
     * @param userId The user ID to check
     * @param storeId The store ID to check
     * @return true if the user is a store manager, false otherwise
     */
    public boolean isStoreManager(String userId, String storeId) {
        Permission permission = permissionManager.getPermission(storeId, userId);
        return permission != null && permission.isStoreManager();
    }

    /**
     * Checks if a user is a store owner for the specified store.
     * 
     * @param userId The user ID to check
     * @param storeId The store ID to check
     * @return true if the user is a store owner, false otherwise
     */
    public boolean isStoreOwner(String userId, String storeId) {
        Permission permission = permissionManager.getPermission(storeId, userId);
        return permission != null && permission.isStoreOwner();
    }
    

    @Override
    public void checkPermission(String userId, String storeId, PermissionType permissionType) {
        permissionManager.checkPermission(userId, storeId, permissionType);
    }

    @Override
    public boolean banUser(String bannerId, String userId, Date endDate) {
        Member member = userRepository.getMember(userId);
        if (member == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        boolean result = permissionManager.banUser(bannerId, userId, endDate);
        if (result) {
            String banMessage = String.format(
                "{\"type\": \"USER_BANNED\", \"message\": \"You are banned until %s\"}",
                endDate.toString()
            );
            
            notificationService.sendNotification(userId, banMessage);
            

            //String message = String.format("You have been banned until %s", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate));
            //notificationService.sendNotification(userId, message);
        }
        return result;
    }

    @Override
    public boolean unbanUser(String unbannerId, String userId) {
        Member member = userRepository.getMember(userId);
        if (member == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        boolean result = permissionManager.unbanUser(unbannerId, userId);
        if (result) {
            String unbanMessage = "{\"type\": \"USER_UNBANNED\", \"message\": \"Your ban has been lifted. You can now use the system again.\"}";
            notificationService.sendNotification(userId, unbanMessage);
        }
        return result;
    }

    @Override
    public Map<String, Date> getBannedUsers() {
        Map<String, Date> bannedUsers = new HashMap<>();
        Map<String, Permission> systemPermissions = permissionManager.getAllPermissionsForStore("1");
        
        for (Map.Entry<String, Permission> entry : systemPermissions.entrySet()) {
            String userId = entry.getKey();
            Permission permission = entry.getValue();
            
            if (permission.hasPermission(PermissionType.BANNED)) {
                Member member = userRepository.getMember(userId);
                if (member != null) {
                    bannedUsers.put(member.getName(), permission.getExpirationDate());
                }
            }
        }
        
        return bannedUsers;
    }

}
