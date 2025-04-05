package Domain;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import Application.Responses.Response;
import Domain.Store.IShoppingBasket;
import Domain.Store.IShoppingCart;
import Domain.Store.IStore;
import Domain.Store.IStoreFacade;
import Domain.Store.Item;
import Domain.Store.ShoppingBasket;
import Domain.User.IUser;
import Domain.User.IUserFacade;
import Infrastructure.INotificationService;
import Infrastructure.IPaymentService;
import Infrastructure.ISupplyService;

public class MarketFacade implements IMarketFacade {
    private IPaymentService paymentService;
    private ISupplyService supplyService;
    private INotificationService notificationService;
    private IUserFacade userFacade;
    private IStoreFacade storeFacade;
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

    public IUserFacade getUserFacade() { 
        return userFacade; 
    }

    public IStoreFacade getStoreFacade() { 
        return storeFacade; 
    }

    private MarketFacade() {}

    public synchronized void initFacades(IUserFacade userFacade,
                                            IStoreFacade storeFacade) {
        this.storeFacade = storeFacade;
        this.userFacade = userFacade;
        this.shoppingBasket = new ShoppingBasket();
    }

    public INotificationService getNotificationService() {
        return notificationService;
    }

    public int getShoppingBasketCount() {
        return shoppingBasket.getShoppingBasketCount();
    }

    public void checkProductsExist(int storeId, Map<Integer, Item> products) {
        storeFacade.checkProductsExist(storeId, products);
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
                        String deliveryAddress, IUser user,
                        IShoppingCart cart) {
        String sessionId = user.getSessionId();
        String name = userFacade.userIsMember(sessionId) ? user.getUserName() : sessionId;
        double price = calculateCartPrice(cart);
        try {
            storeFacade.removeCartQuantity(cart);
        } catch (Exception e) {
            throw e;
        }
        Response res = paymentService.processPayment(card_owner, card_number, expiry_date, cvv, price, paymentID.getAndIncrement(), name, deliveryAddress);

        if (res.errorOccurred()) {
            throw new RuntimeException("Payment failed: " + res.getErrorMessage());
        }
        storeFacade.addCartQuantity(cart);
        for (IShoppingBasket basket : cart.getStoreBaskets()) {
            IStore store = storeFacade.getStore(basket.getStoreId());
            notificationService.sendNotification(name, "Your order has been placed successfully. Order ID: " + paymentID.get(), deliveryAddress);
            supplyService.placeOrder(store, deliveryAddress, basket.getItems());
            shoppingBasket.addShoppingBasket(basket, name, storeFacade.calculateBasketPrice(basket));
        }
        user.removeUserCart();
    }

    public double calculateCartPrice(IShoppingCart cart) {
        double price = 0;
        for (IShoppingBasket basket : cart.getStoreBaskets()) {
            try {
                price += storeFacade.calculateBasketPrice(basket);
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
        String userName = userFacade.getMemberUserName(sessionId);
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
}
