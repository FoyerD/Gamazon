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
import Application.Responses.Response;
import Application.Responses.ResponseT;
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

    public Response pay(String sessionId, String card_owner, String card_number, Date expiry_date, String cvv, double price, String deliveryAddress) {
        try {
            User user = userFacade.getUser(sessionId);
            marketFacade.purchase(card_owner, card_number, expiry_date, cvv, deliveryAddress, user, user.getUserShoppingCart());
            return new Response();
        } catch (Exception e) {
            return new Response(e.getMessage());
        }
    }
    
    public Response updatePaymentService(IPaymentService paymentService) {
        try {
            marketFacade.updatePaymentService(paymentService);
            return new Response();
        } catch (Exception e) {
            return new Response(e.getMessage());
        }
    }

    public Response updateNotificationService(INotificationService notificationService) {
        try {
            marketFacade.updateNotificationService(notificationService);
            return new Response();
        } catch (Exception e) {
            return new Response(e.getMessage());
        }
    }

    public Response updateSupplyService(ISupplyService supplyService) {
        try {
            marketFacade.updateSupplyService(supplyService);
            return new Response();
        } catch (Exception e) {
            return new Response(e.getMessage());
        }
    }

    public ResponseT<Double> calculateCartPrice(String sessionId) {
        try {
            User user = userFacade.getUser(sessionId);
            return new ResponseT<>(marketFacade.calculateCartPrice(user.getUserShoppingCart()));
        } catch (Exception e) {
            return new ResponseT<>(e.getMessage());
        }
    }

    public Response updatePaymentServiceURL(String url) {
        try {
            marketFacade.updatePaymentServiceURL(url);
            return new Response();
        } catch (IOException e) {
            return new Response(e.getMessage());
        }
    }

    public ResponseT<Map<Integer, IShoppingBasket>> getShoppingBaskets() {
        try {
            return new ResponseT<>(marketFacade.getShoppingBaskets());
        } catch (Exception e) {
            return new ResponseT<>(e.getMessage());
        }
    }

    public ResponseT<IShoppingBasket> getShoppingBasket(int id) {
        try {
            return new ResponseT<>(marketFacade.getShoppingBasket(id));
        } catch (Exception e) {
            return new ResponseT<>(e.getMessage());
        }
    }

    public ResponseT<List<IShoppingBasket>> getStoreShoppingBaskets(int storeId) {
        try {
            return new ResponseT<>(marketFacade.getStoreShoppingBaskets(storeId));
        } catch (Exception e) {
            return new ResponseT<>(e.getMessage());
        }
    }

    public ResponseT<List<IShoppingBasket>> getUserShoppingBaskets(String userName, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            return new ResponseT<>(marketFacade.getUserShoppingBaskets(userName, startDateTime, endDateTime));
        } catch (Exception e) {
            return new ResponseT<>(e.getMessage());
        }
    }

    public ResponseT<List<IShoppingBasket>> getUserShoppingBasketsBetween(String userName, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            return new ResponseT<>(marketFacade.getUserShoppingBasketsBetween(userName, startDateTime, endDateTime));
        } catch (Exception e) {
            return new ResponseT<>(e.getMessage());
        }
    }

    public Response addShoppingBasket(IShoppingBasket basket, String userName, double price) {
        try {
            marketFacade.addShoppingBasket(basket, userName, price);
            return new Response();
        } catch (Exception e) {
            return new Response(e.getMessage());
        }
    }

    public Response initFacades(IUserRepository userFacade, IStoreRepository storeFacade) {
        try {
            marketFacade.initFacades(userFacade, storeFacade);
            return new Response();
        } catch (Exception e) {
            return new Response(e.getMessage());
        }
    }

    public ResponseT<Integer> getShoppingBasketCount() {
        try {
            return new ResponseT<>(marketFacade.getShoppingBasketCount());
        } catch (Exception e) {
            return new ResponseT<>(e.getMessage());
        }
    }

    public Response checkProductsExist(int storeId, Map<Integer, Item> productsId) {
        try {
            marketFacade.checkProductsExist(storeId, productsId);
            return new Response();
        } catch (Exception e) {
            return new Response(e.getMessage());
        }
    }

    public ResponseT<INotificationService> getNotificationService() {
        try {
            return new ResponseT<>(marketFacade.getNotificationService());
        } catch (Exception e) {
            return new ResponseT<>(e.getMessage());
        }
    }

    public ResponseT<IStoreRepository> getStoreFacade() {
        try {
            return new ResponseT<>(marketFacade.getStoreFacade());
        } catch (Exception e) {
            return new ResponseT<>(e.getMessage());
        }
    }

    public ResponseT<List<IShoppingBasket>> getMyShoppingBasketHistory(String sessionId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            return new ResponseT<>(marketFacade.getMyShoppingBasketHistory(sessionId, startDateTime, endDateTime));
        } catch (Exception e) {
            return new ResponseT<>(e.getMessage());
        }
    }
}
