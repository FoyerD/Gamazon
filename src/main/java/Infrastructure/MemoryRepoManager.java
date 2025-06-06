package Infrastructure;

import Domain.IRepoManager;
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
import Domain.Store.Discounts.IDiscountRepository;
import Infrastructure.MemoryRepositories.MemoryAuctionRepository;
import Infrastructure.MemoryRepositories.MemoryDiscountRepository;
import Infrastructure.MemoryRepositories.MemoryFeedbackRepository;
import Infrastructure.MemoryRepositories.MemoryItemRepository;
import Infrastructure.MemoryRepositories.MemoryPermissionRepository;
import Infrastructure.MemoryRepositories.MemoryPolicyRepository;
import Infrastructure.MemoryRepositories.MemoryProductRepository;
import Infrastructure.MemoryRepositories.MemoryReceiptRepository;
import Infrastructure.MemoryRepositories.MemoryShoppingBasketRepository;
import Infrastructure.MemoryRepositories.MemoryShoppingCartRepository;
import Infrastructure.MemoryRepositories.MemoryStoreRepository;
import Infrastructure.MemoryRepositories.MemoryUserRepository;


public class MemoryRepoManager implements IRepoManager {
    private MemoryItemRepository itemRepository;
    private MemoryStoreRepository storeRepository;
    private MemoryPermissionRepository permissionRepository;
    private MemoryReceiptRepository receiptRepository;
    private MemoryShoppingBasketRepository shoppingBasketRepository;
    private MemoryShoppingCartRepository shoppingCartRepository;
    private MemoryAuctionRepository auctionRepository;
    private MemoryFeedbackRepository feedbackRepository;
    private MemoryProductRepository productRepository;
    private MemoryUserRepository userRepository;
    private MemoryDiscountRepository discountRepository;
    // private MemoryConditionRepository conditionRepository;
    private MemoryPolicyRepository policyRepository;
    
    @Override
    public IStoreRepository getStoreRepository() {
        if (storeRepository == null) {
            storeRepository = new MemoryStoreRepository();
        }
        return storeRepository;
    }
    @Override
    public IItemRepository getItemRepository() {
        if (itemRepository == null) {
            itemRepository = new MemoryItemRepository();
        }
        return itemRepository;
    }
    @Override
    public IPermissionRepository getPermissionRepository() {
        if (permissionRepository == null) {
            permissionRepository = new MemoryPermissionRepository();
        }
        return permissionRepository;
    }
    @Override
    public IReceiptRepository getReceiptRepository() {
        if (receiptRepository == null) {
            receiptRepository = new MemoryReceiptRepository();
        }
        return receiptRepository;
    }
    @Override
    public IShoppingBasketRepository getShoppingBasketRepository() {
        if (shoppingBasketRepository == null) {
            shoppingBasketRepository = new MemoryShoppingBasketRepository();
        }
        return shoppingBasketRepository;
    }
    @Override
    public IShoppingCartRepository getShoppingCartRepository() {
        if (shoppingCartRepository == null) {
            shoppingCartRepository = new MemoryShoppingCartRepository();
        }
        return shoppingCartRepository;
    }
    @Override
    public IAuctionRepository getAuctionRepository() {
        if (auctionRepository == null) {
            auctionRepository = new MemoryAuctionRepository();
        }
        return auctionRepository;
    }
    @Override
    public IFeedbackRepository getFeedbackRepository() {
        if (feedbackRepository == null) {
            feedbackRepository = new MemoryFeedbackRepository();
        }
        return feedbackRepository;
    }
    @Override
    public IProductRepository getProductRepository() {
        if (productRepository == null) {
            productRepository = new MemoryProductRepository();
        }
        return productRepository;
    }
    @Override
    public IUserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new MemoryUserRepository();
        }
        return userRepository;
    }

    @Override
    public IDiscountRepository getDiscountRepository() {
        if (discountRepository == null) {
            discountRepository = new MemoryDiscountRepository();
        }
        return discountRepository;
    }

    // @Override
    // public IConditionRepository getConditionRepository() {
    //     if (conditionRepository == null) {
    //         conditionRepository = new MemoryConditionRepository();
    //     }
    //     return conditionRepository;
    // }
    
    public IPolicyRepository getPolicyRepository() {
        if (policyRepository == null) {
            policyRepository = new MemoryPolicyRepository();
        }
        return policyRepository;
    }
}
