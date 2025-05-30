package UI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import Domain.Repos.IItemRepository;
import Domain.Repos.IProductRepository;
import Domain.Repos.IStoreRepository;
import jakarta.transaction.Transactional;

@Component
public class DatabaseCleaner implements CommandLineRunner, Ordered {

    private final IProductRepository productRepo;
    private final IItemRepository itemRepo;
    private final IStoreRepository storeRepo;

    @Value("${app.clean-on-start:false}")
    private boolean cleanOnStart;

    public DatabaseCleaner(IProductRepository productRepo,
                           IItemRepository itemRepo,
                            IStoreRepository storeRepo) {
        this.productRepo = productRepo;
        this.itemRepo = itemRepo;
        this.storeRepo = storeRepo;
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

        System.out.println("🧹 Database wiped clean (before app init)");
    }
}

