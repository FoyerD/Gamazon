package Domain;

import Application.utils.Response;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.OfferManager;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Store.ItemFacade;
import Domain.Store.ProductFacade;
import Domain.Store.StoreFacade;
import Domain.User.LoginManager;
import Domain.management.IMarketFacade;
import Domain.management.MarketFacade;
import Domain.management.PermissionManager;
import Domain.management.PolicyFacade;
public class FacadeManager {
    private IRepoManager repoManager;
    private IMarketFacade marketFacade;
    private IShoppingCartFacade CartFacade;
    private StoreFacade storeFacade;
    private ItemFacade itemFacade;
    private ProductFacade productFacade;
    private IExternalPaymentService paymentService;
    private LoginManager loginManager;
    private PermissionManager permissionManager;
    private OfferManager offerManager;
    private INotificationService notificationService;
    private PolicyFacade policyFacade;

    public FacadeManager(IRepoManager repoManager, IExternalPaymentService paymentService) {
        this.repoManager = repoManager;
        this.paymentService = paymentService;
    }

    public IExternalPaymentService getPaymentService() {
        return paymentService;
    }

    public IMarketFacade getMarketFacade() {
        if (marketFacade == null) {
            marketFacade = MarketFacade.getInstance();
            marketFacade.initFacades(repoManager.getUserRepository(),
                                    getShoppingCartFacade(),
                                    getPermissionManager());

        }
        return marketFacade;
    }

    public INotificationService getNotificationService() {
        if (notificationService == null) {
            notificationService = (userId, message) -> {
                // Mock implementation of the notification service
                System.out.println("Notification sent to user " + userId + ": " + message);
                return Response.success(true);
            };
        }
        return notificationService;
    }

    public StoreFacade getStoreFacade() {
        if (storeFacade == null) {
            storeFacade = new StoreFacade(repoManager.getStoreRepository(),
                                        repoManager.getFeedbackRepository(),
                                        repoManager.getItemRepository(),
                                        repoManager.getUserRepository(),
                                        repoManager.getAuctionRepository(), getNotificationService());
        }
        return storeFacade;
    }

    public ItemFacade getItemFacade() {
        if (itemFacade == null) {
            itemFacade = new ItemFacade(repoManager.getItemRepository(),
                                        repoManager.getProductRepository(),
                                        repoManager.getStoreRepository());
        }
        return itemFacade;
    }

    public IShoppingCartFacade getShoppingCartFacade() {
        if (CartFacade == null) {
            CartFacade = new ShoppingCartFacade(repoManager.getShoppingCartRepository(),
                                                repoManager.getShoppingBasketRepository(),
                                                getPaymentService(),
                                                getItemFacade(),
                                                getStoreFacade(),
                                                repoManager.getReceiptRepository(),
                                                repoManager.getProductRepository(),
                                                getPolicyFacade(),
                                                repoManager.getUserRepository());
        }
        return CartFacade;
    }

    public ProductFacade getProductFacade() {
        if (productFacade == null) {
            productFacade = new ProductFacade(repoManager.getProductRepository());
        }
        return productFacade;
    }

    public LoginManager getLoginManager() {
        if (loginManager == null) {
            loginManager = new LoginManager(repoManager.getUserRepository());
        }
        return loginManager;
    }
    public PermissionManager getPermissionManager() {
        if (permissionManager == null) {
            permissionManager = new PermissionManager(repoManager.getPermissionRepository());
        }
        return permissionManager;
    }

    public OfferManager getOfferManager() {
        if (offerManager == null) {
            offerManager = new OfferManager(repoManager.getOfferRepository(), getPermissionManager());
        }
        return offerManager;
    }

    public PolicyFacade getPolicyFacade() {
        if (policyFacade == null) {
            policyFacade = new PolicyFacade(repoManager.getPolicyRepository(),
                                            repoManager.getUserRepository(),
                                            getItemFacade(),
                                            getProductFacade());
        }
        return policyFacade;
    }

    public IRepoManager getRepositoryManager()
    {
        return repoManager;
    }
}
