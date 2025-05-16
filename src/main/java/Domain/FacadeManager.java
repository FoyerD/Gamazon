package Domain;


import Domain.ExternalServices.IPaymentService;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Store.ItemFacade;
import Domain.Store.ProductFacade;
import Domain.Store.StoreFacade;
import Domain.User.LoginManager;
import Domain.User.PasswordChecker;
import Domain.management.IMarketFacade;
import Domain.management.MarketFacade;
import Domain.management.PermissionManager;

public class FacadeManager {
    private IRepoManager repoManager;
    private IMarketFacade marketFacade;
    private IShoppingCartFacade CartFacade;
    private StoreFacade storeFacade;
    private ItemFacade itemFacade;
    private ProductFacade productFacade;
    private IPaymentService paymentService;
    private LoginManager loginManager;
    private PermissionManager permissionManager;
    private PasswordChecker passwordChecker;

    public FacadeManager(IRepoManager repoManager, IPaymentService paymentService) {
        this.repoManager = repoManager;
        this.paymentService = paymentService;
    }

    public IPaymentService getPaymentService() {
        return paymentService;
    }

    public IMarketFacade getMarketFacade() {
        if (marketFacade == null) {
            marketFacade = MarketFacade.getInstance();
            marketFacade.initFacades(repoManager.getUserRepository(),
                                    repoManager.getItemRepository(),
                                    getStoreFacade(),
                                    getShoppingCartFacade());

        }
        return marketFacade;
    }

    public StoreFacade getStoreFacade() {
        if (storeFacade == null) {
            storeFacade = new StoreFacade(repoManager.getStoreRepository(),
                                        repoManager.getFeedbackRepository(),
                                        repoManager.getItemRepository(),
                                        repoManager.getUserRepository(),
                                        repoManager.getAuctionRepository());
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
                                                repoManager.getProductRepository());
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
}
