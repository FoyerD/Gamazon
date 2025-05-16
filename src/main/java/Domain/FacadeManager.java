package Domain;

import Domain.ExternalServices.IPaymentService;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Store.ItemFacade;
import Domain.Store.ProductFacade;
import Domain.Store.StoreFacade;
import Domain.management.IMarketFacade;
import Domain.management.MarketFacade;
import Infrastructure.PaymentService;

public class FacadeManager {
    private IRepoManager repoManager;
    private IMarketFacade marketFacade;
    private IShoppingCartFacade CartFacade;
    private StoreFacade storeFacade;
    private ItemFacade itemFacade;
    private ProductFacade productFacade;
    private IPaymentService paymentService;


    public FacadeManager(IRepoManager repoManager, String paymentServiceURL) {
        this.repoManager = repoManager;
        this.paymentService = new PaymentService(paymentServiceURL);
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
}
