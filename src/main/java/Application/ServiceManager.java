package Application;



import Application.utils.Response;
import Domain.FacadeManager;
import Domain.ExternalServices.INotificationService;



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
    private INotificationService notificationService;
    private PolicyService policyService;

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

    public PolicyService getPolicyService() {
        if (policyService == null) {
            policyService = new PolicyService(facadeManager.getPolicyFacade(),
                                            getTokenService(),
                                            facadeManager.getPermissionManager(),
                                            facadeManager.getRepositoryManager().getItemRepository(),
                                            facadeManager.getShoppingCartFacade());
        }
        return policyService;
    }

    public INotificationService getINotificationService() {
        if (notificationService == null) {
            notificationService = new INotificationService() {
                @Override
                public Response<Boolean> sendNotification(String userId, String message) {
                    // Implementation for sending notification
                    System.out.println("Sending notification to user " + userId + ": " + message);
                    return new Response<>(true);
                }

            };
        }
        return notificationService;
    }

    public StoreService getStoreService() {
        if (storeService == null) {
            storeService = new StoreService(facadeManager.getStoreFacade(),
                                            getTokenService(),
                                            facadeManager.getPermissionManager(),
                                            getINotificationService(),
                                            facadeManager.getShoppingCartFacade(),
                                            facadeManager.getItemFacade(),
                                            facadeManager.getOfferManager(),
                                            facadeManager.getLoginManager(),
                                            facadeManager.getPaymentService());
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
                                                getINotificationService(),
                                                facadeManager.getStoreFacade(),
                                                facadeManager.getPermissionManager(),
                                                facadeManager.getLoginManager(),
                                                facadeManager.getOfferManager(),
                                                facadeManager.getItemFacade());
        }
        return shoppingService;
    }
    
    public void injectINotificationService(INotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
