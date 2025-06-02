package UI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import Domain.Repos.IItemRepository;
import Domain.Repos.IPolicyRepository;
import Domain.Repos.IProductRepository;
import Domain.Repos.IReceiptRepository;
import Domain.Repos.IShoppingBasketRepository;
import Domain.Repos.IShoppingCartRepository;
import Domain.Repos.IStoreRepository;
import Domain.Repos.IUserRepository;
import jakarta.transaction.Transactional;

@Component
public class DatabaseCleaner implements CommandLineRunner, Ordered {

    private final IProductRepository productRepo;
    private final IItemRepository itemRepo;
    private final IStoreRepository storeRepo;
    private final IUserRepository userRepo;
    private final IReceiptRepository receiptRepo;
    private final IShoppingBasketRepository shoppingBasketRepo;
    private final IShoppingCartRepository shoppingCartRepo;
    private final IPolicyRepository policyRepo;

    @Value("${app.clean-on-start:false}")
    private boolean cleanOnStart;

    public DatabaseCleaner(IProductRepository productRepo,
                           IItemRepository itemRepo,
                            IStoreRepository storeRepo,
                            IUserRepository userRepo, IReceiptRepository receiptRepo,
                            IShoppingBasketRepository shoppingBasketRepo, 
                            IShoppingCartRepository shoppingCartRepo, IPolicyRepository policyRepo) {
        this.policyRepo = policyRepo;
        this.productRepo = productRepo;
        this.itemRepo = itemRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.receiptRepo = receiptRepo;
        this.shoppingBasketRepo = shoppingBasketRepo;
        this.shoppingCartRepo = shoppingCartRepo;
    }

    @Override
    public int getOrder() {
        return 0; // Lower means higher priority
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!cleanOnStart) return;

        itemRepo.deleteAll();
        productRepo.deleteAll();
        storeRepo.deleteAll();
        userRepo.deleteAll();
        receiptRepo.deleteAll();
        shoppingBasketRepo.deleteAll();
        shoppingCartRepo.deleteAll();
        policyRepo.deleteAll();
        
        System.out.println("🧹 Database wiped clean (before app init)");
    }
}

