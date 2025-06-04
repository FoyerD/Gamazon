package Domain;

import Domain.Repos.IAuctionRepository;
import Domain.Repos.IFeedbackRepository;
import Domain.Repos.IItemRepository;
import Domain.Repos.IPermissionRepository;
import Domain.Repos.IPolicyRepository;
import Domain.Repos.IProductRepository;
import Domain.Repos.IReceiptRepository;
import Domain.Repos.IShoppingBasketRepository;
import Domain.Repos.IShoppingCartRepository;
import Domain.Repos.IStoreRepository;
import Domain.Repos.IUserRepository;

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

