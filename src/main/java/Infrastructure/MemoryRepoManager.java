package Infrastructure;

import Domain.IRepoManager;
import Domain.Shopping.IReceiptRepository;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IProductRepository;
import Domain.Store.IStoreRepository;
import Domain.Store.Discounts.IDiscountRepository;
import Domain.Store.Discounts.Conditions.IConditionRepository;
import Domain.User.IUserRepository;
import Domain.management.IPermissionRepository;
import Infrastructure.Repositories.MemoryAuctionRepository;
import Infrastructure.Repositories.MemoryConditionRepository;
import Infrastructure.Repositories.MemoryDiscountRepository;
import Infrastructure.Repositories.MemoryFeedbackRepository;
import Infrastructure.Repositories.MemoryItemRepository;
import Infrastructure.Repositories.MemoryPermissionRepository;
import Infrastructure.Repositories.MemoryProductRepository;
import Infrastructure.Repositories.MemoryReceiptRepository;
import Infrastructure.Repositories.MemoryShoppingBasketRepository;
import Infrastructure.Repositories.MemoryShoppingCartRepository;
import Infrastructure.Repositories.MemoryStoreRepository;
import Infrastructure.Repositories.MemoryUserRepository;

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
    private MemoryConditionRepository conditionRepository;

    
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

    @Override
    public IConditionRepository getConditionRepository() {
        if (conditionRepository == null) {
            conditionRepository = new MemoryConditionRepository();
        }
        return conditionRepository;
    }
    
}
