package Domain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import Domain.User.IUserRepository;
import Domain.User.User;
import Domain.Store.IStoreRepository;
import Domain.Shopping.IShoppingBasket;
import Domain.Shopping.IShoppingCart;
import Domain.Store.Item;

public interface IMarketFacade {
    
    void updatePaymentService(IPaymentService paymentService);

    void updateNotificationService(INotificationService notificationService);

    void updateSupplyService(ISupplyService supplyService);

    void purchase(String card_owner, String card_number, Date expiry_date, String cvv,
                        String deliveryAddress, User user,
                        IShoppingCart cart);

    double calculateCartPrice(IShoppingCart cart); 

    void updatePaymentServiceURL(String url) throws IOException;

    Map<Integer, IShoppingBasket> getShoppingBaskets();

    IShoppingBasket getShoppingBasket(int id);

    List<IShoppingBasket> getStoreShoppingBaskets(int storeId);

    List<IShoppingBasket> getUserShoppingBaskets(String userName, LocalDateTime startDateTime,
                             LocalDateTime endDateTime);

    List<IShoppingBasket> getUserShoppingBasketsBetween(String userName, LocalDateTime startDateTime,
                             LocalDateTime endDateTime);

    void addShoppingBasket(IShoppingBasket basket, String userName, double price);

    void initFacades(IUserRepository userFacade, IStoreRepository storeFacade);

    int getShoppingBasketCount();

    void checkProductsExist(int storeId, Map<Integer, Item> productsId);
    
    INotificationService getNotificationService();

    IStoreRepository getStoreFacade();

    List<IShoppingBasket> getMyShoppingBasketHistory(String sessionId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    void closeStore(int storeId);

    void openMarket();
}
