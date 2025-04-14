package Domain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import Application.Response;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.Shopping.IShoppingBasket;
import Domain.Shopping.IShoppingCart;
import Domain.Shopping.ShoppingBasket;
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
    private IShoppingBasket shoppingBasket;
    private AtomicLong paymentID = new AtomicLong(0);

    private static class SingletoneHolder {
        private static final MarketFacade INSTANCE = new MarketFacade();
    }

    public synchronized static MarketFacade getInstance() {
        return SingletoneHolder.INSTANCE;
    }

    private MarketFacade() {}

    public synchronized void initFacades(IUserRepository userRepository, IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.shoppingBasket = new ShoppingBasket();
    }

    public IPaymentService getPaymentService() {
        return paymentService;
    }

    public ISupplyService getSupplyService() {
        return supplyService;
    }

    public IUserRepository getUserFacade() {
        return userRepository;
    }

    public IStoreRepository getStoreFacade() {
        return storeRepository;
    }

    public INotificationService getNotificationService() {
        return notificationService;
    }

    public int getShoppingBasketCount() {
        return shoppingBasket.getShoppingBasketCount();
    }

    public void checkProductsExist(int storeId, Map<Integer, Item> products) {
        storeRepository.checkProductsExist(storeId, products);
    }

    public synchronized void updatePaymentService(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public synchronized void updateNotificationService(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public synchronized void updateSupplyService(ISupplyService supplyService) {
        this.supplyService = supplyService;
    }

    public synchronized void updateshoppingBasket(IShoppingBasket shoppingBasket) {
        this.shoppingBasket = shoppingBasket;
    }

    private void checkPermission(User user, int storeId, PermissionType requiredPermission) {
        Permission permission = user.getStorePermission(storeId);
        if (permission == null || !permission.hasPermission(requiredPermission)) {
            throw new SecurityException("User " + user.getUserName() + " lacks permission: " + requiredPermission + " for store ID: " + storeId);
        }
    }

    private void checkGlobalPermission(User user, PermissionType requiredPermission) {
        Permission permission = user.getGlobalPermission();
        if (permission == null || !permission.hasPermission(requiredPermission)) {
            throw new SecurityException("User " + user.getUserName() + " lacks global permission: " + requiredPermission);
        }
    }

    public void purchase(String card_owner, String card_number, Date expiry_date, String cvv,
                         String deliveryAddress, User user,
                         IShoppingCart cart) {
        String sessionId = user.getSessionId();
        String name = userRepository.userIsMember(sessionId) ? user.getUserName() : sessionId;
        double price = calculateCartPrice(cart);
        storeRepository.removeCartQuantity(cart);

        Response<Boolean> res = paymentService.processPayment(card_owner, card_number, expiry_date, cvv, price, paymentID.getAndIncrement(), name, deliveryAddress);

        if (res.errorOccurred()) {
            throw new RuntimeException("Payment failed: " + res.getErrorMessage());
        }

        storeRepository.addCartQuantity(cart);
        for (IShoppingBasket basket : cart.getStoreBaskets()) {
            Store store = storeRepository.getStore(basket.getStoreId());
            notificationService.sendNotification(name, "Your order has been placed successfully. Order ID: " + paymentID.get(), deliveryAddress);
            supplyService.placeOrder(store, deliveryAddress, basket.getItems());
            shoppingBasket.addShoppingBasket(basket, name, storeRepository.calculateBasketPrice(basket));
        }
        user.removeUserCart();
    }

    public double calculateCartPrice(IShoppingCart cart) {
        double price = 0;
        for (IShoppingBasket basket : cart.getStoreBaskets()) {
            price += storeRepository.calculateBasketPrice(basket);
        }
        return price;
    }

    public void updatePaymentServiceURL(String url) throws IOException {
        paymentService.updatePaymentServiceURL(url);
    }

    public Map<Integer, IShoppingBasket> getShoppingBaskets() {
        return shoppingBasket.getShoppingBaskets();
    }

    public IShoppingBasket getShoppingBasket(int id) {
        return shoppingBasket.getShoppingBasket(id);
    }

    public List<IShoppingBasket> getStoreShoppingBaskets(int storeId) {
        return shoppingBasket.getStoreShoppingBaskets(storeId);
    }

    public List<IShoppingBasket> getStoreShoppingBasketsBetween(int storeId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return shoppingBasket.getStoreShoppingBasketsInRange(storeId, startDateTime, endDateTime);
    }

    public List<IShoppingBasket> getUserShoppingBasketsBetween(String userName, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            return shoppingBasket.getUserShoppingBasketsInRange(userName, startDateTime, endDateTime);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<IShoppingBasket> getMyShoppingBasketHistory(String sessionId, LocalDateTime startDateTime, LocalDateTime endDateTime, int storeId, User requester) {
        checkPermission(requester, storeId, PermissionType.ACCESS_PURCHASE_RECORDS);
        String userName = userRepository.getMemberUserName(sessionId);
        return shoppingBasket.getUserShoppingBasketsInRange(userName, startDateTime, endDateTime);
    }

    public void addShoppingBasket(IShoppingBasket basket, String userName, double price) {
        shoppingBasket.addShoppingBasket(basket, userName, price);
    }

    public synchronized void cleanShoppingBaskets() {
        shoppingBasket.clean();
    }

    public List<IShoppingBasket> getUserShoppingBaskets(String userName, LocalDateTime startDateTime, LocalDateTime endDateTime, int storeId, User requester) {
        checkPermission(requester, storeId, PermissionType.ACCESS_PURCHASE_RECORDS);
        return shoppingBasket.getUserShoppingBasketsInRange(userName, startDateTime, endDateTime);
    }

    public void closeStore(int storeId, User requester) {
        checkPermission(requester, storeId, PermissionType.DEACTIVATE_STORE);
        Store store = storeRepository.getStore(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store with ID " + storeId + " does not exist.");
        }

        List<User> storeOwnersAndManagers = store.getOwnersAndManagers();
        for (User user : storeOwnersAndManagers) {
            notificationService.sendNotification(
                user.getUserName(),
                "The store with ID " + storeId + " has been closed.",
                user.getEmail()
            );
        }

        store.cancelSubscriptions();
        storeRepository.closeStore(storeId);
        System.out.println("Store with ID " + storeId + " has been successfully closed.");
    }

    public void openMarket() {
        try {
            if (paymentService == null || supplyService == null || notificationService == null || userRepository == null) {
                throw new IllegalStateException("One or more services are not initialized.");
            }

            paymentService.initialize();
            supplyService.initialize();
            notificationService.initialize();

            User marketManager = userRepository.getMarketManager();
            if (marketManager == null) {
                throw new IllegalStateException("Market manager is not assigned.");
            }

            // Initialize global permission as founder
            Permission founderPermission = new Permission("SYSTEM", marketManager.getUserName());
            founderPermission.initStoreFounder();
            marketManager.setGlobalPermission(founderPermission);

            System.out.println("Market has been successfully opened, all services are initialized, and the market manager is assigned.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to open the market: " + e.getMessage(), e);
        }
    }
}
