package Application;

import Domain.IMarketFacade;
import Domain.User.User;
import Domain.User.IUserRepository;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import Domain.Store.IStoreRepository;
import Domain.Shopping.IShoppingBasket;
import Domain.Store.Item;

public class MarketService {

    private final IMarketFacade marketFacade;
    private final IUserRepository userFacade;

    public MarketService(IMarketFacade marketFacade, IUserRepository userFacade) {
        this.marketFacade = marketFacade;
        this.userFacade = userFacade;
    }

    public Response<Void> pay(String sessionId, String card_owner, String card_number, Date expiry_date, String cvv, double price, String deliveryAddress) {
        try {
            User user = userFacade.getUser(sessionId);
            marketFacade.purchase(card_owner, card_number, expiry_date, cvv, deliveryAddress, user, user.getUserShoppingCart());
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<Void> updatePaymentService(IPaymentService paymentService) {
        try {
            marketFacade.updatePaymentService(paymentService);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<Void> updateNotificationService(INotificationService notificationService) {
        try {
            marketFacade.updateNotificationService(notificationService);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<Void> updateSupplyService(ISupplyService supplyService) {
        try {
            marketFacade.updateSupplyService(supplyService);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<Double> calculateCartPrice(String sessionId) {
        try {
            User user = userFacade.getUser(sessionId);
            return new Response<>(marketFacade.calculateCartPrice(user.getUserShoppingCart()));
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<Void> updatePaymentServiceURL(String url) {
        try {
            marketFacade.updatePaymentServiceURL(url);
            return new Response<>(null);
        } catch (IOException e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<Map<Integer, IShoppingBasket>> getShoppingBaskets() {
        try {
            return new Response<>(marketFacade.getShoppingBaskets());
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<IShoppingBasket> getShoppingBasket(int id) {
        try {
            return new Response<>(marketFacade.getShoppingBasket(id));
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<List<IShoppingBasket>> getStoreShoppingBaskets(int storeId) {
        try {
            return new Response<>(marketFacade.getStoreShoppingBaskets(storeId));
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<List<IShoppingBasket>> getUserShoppingBaskets(String userName, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            return new Response<>(marketFacade.getUserShoppingBaskets(userName, startDateTime, endDateTime));
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<List<IShoppingBasket>> getUserShoppingBasketsBetween(String userName, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            return new Response<>(marketFacade.getUserShoppingBasketsBetween(userName, startDateTime, endDateTime));
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<Void> addShoppingBasket(IShoppingBasket basket, String userName, double price) {
        try {
            marketFacade.addShoppingBasket(basket, userName, price);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<Void> initFacades(IUserRepository userFacade, IStoreRepository storeFacade) {
        try {
            marketFacade.initFacades(userFacade, storeFacade);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<Integer> getShoppingBasketCount() {
        try {
            return new Response<>(marketFacade.getShoppingBasketCount());
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<Void> checkProductsExist(int storeId, Map<Integer, Item> productsId) {
        try {
            marketFacade.checkProductsExist(storeId, productsId);
            return new Response<>(null);
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<INotificationService> getNotificationService() {
        try {
            return new Response<>(marketFacade.getNotificationService());
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<IStoreRepository> getStoreFacade() {
        try {
            return new Response<>(marketFacade.getStoreFacade());
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }

    public Response<List<IShoppingBasket>> getMyShoppingBasketHistory(String sessionId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            return new Response<>(marketFacade.getMyShoppingBasketHistory(sessionId, startDateTime, endDateTime));
        } catch (Exception e) {
            return new Response<>(e.getMessage());
        }
    }
}
