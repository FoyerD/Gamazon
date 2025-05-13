package Domain.Store;
import java.util.List;
import java.util.UUID;

import Domain.Store.IProductRepository;
import Domain.Store.IStoreRepository;

public class ProductFacade {
    private IProductRepository productRepository;

    public ProductFacade(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    public ProductFacade() {
        this.productRepository = null;
    }
    public void setProductRepository(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public boolean isInitialized() {
        return this.productRepository != null;
    }
    public Product getProduct(String productId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        return productRepository.get(productId);
    }

    public Product getProductByName(String name) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        return productRepository.getByName(name);
    }
    public void addProduct(String name, List<String> categories, List<String> catDesc) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if(this.getProductByName(name) != null) {
            throw new RuntimeException("Product already exists");
        }
        String productId = UUID.randomUUID().toString();
        productRepository.add(product);
    }
    
}
