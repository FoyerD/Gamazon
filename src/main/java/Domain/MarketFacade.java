package Domain;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import Application.Response;
import Domain.Store.IStoreRepository;
import Domain.Shopping.IShoppingBasket;
import Domain.Shopping.IShoppingCart;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.Store;
import Domain.Store.Item;
import Domain.User.User;
import Domain.User.IUserRepository;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;

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

    private MarketFacade() {}

    public synchronized void initFacades(IUserRepository userRepository, IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.shoppingBasket = new ShoppingBasket();
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

    public synchronized void updateNotificationService(INotificationService notificationService){
        this.notificationService = notificationService;
    }

    public synchronized void updateSupplyService(ISupplyService supplyService) {
        this.supplyService = supplyService;
    }

    public synchronized void updateshoppingBasket(IShoppingBasket shoppingBasket) {
        this.shoppingBasket = shoppingBasket;
    }

    public void purchase(String card_owner, String card_number, Date expiry_date, String cvv,
                        String deliveryAddress, User user,
                        IShoppingCart cart) {
        String sessionId = user.getSessionId();
        String name = userRepository.userIsMember(sessionId) ? user.getUserName() : sessionId;
        double price = calculateCartPrice(cart);
        try {
            storeRepository.removeCartQuantity(cart);
        } catch (Exception e) {
            throw e;
        }
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
            try {
                price += storeRepository.calculateBasketPrice(basket);
            } catch (Exception e) {
                throw e;
            }
        }
        return price;
    }

    public void updatePaymentServiceURL(String url) throws IOException {
        paymentService.updatePaymentServiceURL(url);
    }

    public Map<Integer, IShoppingBasket> getShoppingBaskets() {
        try {
            return shoppingBasket.getShoppingBaskets();
        } catch (Exception e) {
            throw e;
        }
    }

    public IShoppingBasket getShoppingBasket(int id) {
        try {
        return shoppingBasket.getShoppingBasket(id);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<IShoppingBasket> getStoreShoppingBaskets(int storeId) {
        try {
            return shoppingBasket.getStoreShoppingBaskets(storeId);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<IShoppingBasket> getStoreShoppingBasketsBetween(int storeId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
        return shoppingBasket.getStoreShoppingBasketsInRange(storeId, startDateTime, endDateTime);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<IShoppingBasket> getMyShoppingBasketHistory(String sessionId, LocalDateTime StartDateTime, LocalDateTime EndDateTime) {
        String userName = userRepository.getMemberUserName(sessionId);
        try {
            return shoppingBasket.getUserShoppingBasketsInRange(userName, StartDateTime, EndDateTime);
        } catch (Exception e) {
            throw e;
        }
    }

    public void addShoppingBasket(IShoppingBasket basket, String userName, double price) {
        try {
            shoppingBasket.addShoppingBasket(basket, userName, price);
        } catch (Exception e) {
            throw e;
        }
    }

    public synchronized void cleanShoppingBaskets() {
        try {
            shoppingBasket.clean();
        } finally {
        }
    }

    public List<IShoppingBasket> getUserShoppingBaskets(String userName, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            return shoppingBasket.getUserShoppingBasketsInRange(userName, startDateTime, endDateTime);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<IShoppingBasket> getUserShoppingBasketsBetween(String userName, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            return shoppingBasket.getUserShoppingBasketsInRange(userName, startDateTime, endDateTime);
        } catch (Exception e) {
            throw e;
        }
    }

    public void closeStore(int storeId) {
        try {
            if (userRepository == null) {
                throw new IllegalStateException("User repository is not initialized.");
            }
    
            if (notificationService == null) {
                throw new IllegalStateException("Notification service is not initialized.");
            }
    
            Store store = storeRepository.getStore(storeId);
            if (store == null) {
                throw new IllegalArgumentException("Store with ID " + storeId + " does not exist.");
            }
    
            // Notify store owners and managers
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to close the store: " + e.getMessage(), e);
        }
    }

    public void openMarket() {
        try {
            if (paymentService == null) {
                throw new IllegalStateException("Payment service is not initialized.");
            }
            paymentService.initialize();
    
            if (supplyService == null) {
                throw new IllegalStateException("Supply service is not initialized.");
            }
            supplyService.initialize();
    
            if (notificationService == null) {
                throw new IllegalStateException("Notification service is not initialized.");
            }
            notificationService.initialize();
    
            if (userRepository == null) {
                throw new IllegalStateException("User repository is not initialized.");
            }
            User marketManager = userRepository.getMarketManager();
            if (marketManager == null) {
                throw new IllegalStateException("Market manager is not assigned.");
            }
    
            System.out.println("Market has been successfully opened, all services are initialized, and the market manager is assigned.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to open the market: " + e.getMessage(), e);
        }
    }
}
