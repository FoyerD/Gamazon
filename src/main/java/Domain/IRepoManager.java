package Domain;

import Domain.Shopping.IReceiptRepository;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IPolicyRepository;
import Domain.Store.IProductRepository;
import Domain.Store.IStoreRepository;
import Domain.User.IUserRepository;
import Domain.management.IPermissionRepository;

public interface IRepoManager {
    public IStoreRepository getStoreRepository();
    public IItemRepository getItemRepository();
    public IPermissionRepository getPermissionRepository();
    public IReceiptRepository getReceiptRepository();
    public IShoppingBasketRepository getShoppingBasketRepository();
    public IShoppingCartRepository getShoppingCartRepository();
    public IAuctionRepository getAuctionRepository();
    public IFeedbackRepository getFeedbackRepository();
    public IProductRepository getProductRepository();
    public IUserRepository getUserRepository();
    public IPolicyRepository getPolicyRepository();
}

