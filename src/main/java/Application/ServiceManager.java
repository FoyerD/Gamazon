package Application;



import org.springframework.stereotype.Service;

import Domain.FacadeManager;
import Infrastructure.NotificationService;


public class ServiceManager {
    private ItemService itemService;
    private StoreService storeService;
    private ProductService productService;
    private MarketService marketService;
    private TokenService tokenService;
    private UserService userService;
    private CustomerServiceService customerService;
    private ShoppingService shoppingService;
    private FacadeManager facadeManager;
    private NotificationService notificationService;

    public ServiceManager(FacadeManager facadeManager) {
        this.facadeManager = facadeManager;
    }

    public TokenService getTokenService() {
        if (tokenService == null) {
            tokenService = new TokenService();
        }
        return tokenService;
    }

    public ItemService getItemService() {
        if (itemService == null) {
            itemService = new ItemService(facadeManager.getItemFacade(),
                                        getTokenService(),
                                        facadeManager.getPermissionManager());
        }
        return itemService;
    }

    public NotificationService getNotificationService() {
        if (notificationService == null) {
            notificationService = new NotificationService();
        }
        return notificationService;
    }

    public StoreService getStoreService() {
        if (storeService == null) {
            storeService = new StoreService(facadeManager.getStoreFacade(),
                                            getTokenService(),
                                            facadeManager.getPermissionManager(),
                                            getNotificationService());
        }
        return storeService;
    }
    public ProductService getProductService() {
        if (productService == null) {
            productService = new ProductService(facadeManager.getProductFacade(),
                                                getTokenService(),
                                                facadeManager.getPermissionManager());
        }
        return productService;
    }

    public MarketService getMarketService() {
        if (marketService == null) {
            marketService = new MarketService(facadeManager.getMarketFacade(),
                                            getTokenService(),
                                            facadeManager.getPermissionManager());
        }
        return marketService;
    }
    public UserService getUserService() {
        if (userService == null) {
            userService = new UserService(facadeManager.getLoginManager(),
                                        getTokenService());
        }
        return userService;
    }

    public CustomerServiceService getCustomerService() {
        if (customerService == null) {
            customerService = new CustomerServiceService(facadeManager.getStoreFacade(),
                                                        getTokenService(),
                                                        facadeManager.getPermissionManager());
        }
        return customerService;
    }

    public ShoppingService getShoppingService() {
        if (shoppingService == null) {
            shoppingService = new ShoppingService(facadeManager.getShoppingCartFacade(),
                                                getTokenService(),
                                                facadeManager.getStoreFacade(),
                                                facadeManager.getPermissionManager());
        }
        return shoppingService;
    }
}
