package Infrastructure.JpaSpringRepositories;

import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Repos.IProductRepository;
import Domain.Store.Product;

@Repository
@Profile({"prod", "dbtest"})
public class JpaProductRepository extends IProductRepository {

    private final IJpaProductRepository jpaProductRepository;

    public JpaProductRepository(IJpaProductRepository jpaProductRepository) {
        this.jpaProductRepository = jpaProductRepository;
    }

    @Override
    public Product getByName(String name) {
        return jpaProductRepository.getByName(name);
    }

    @Override
    public Set<Product> getAll() {
        return jpaProductRepository.getAll();
    }

    @Override
    public boolean add(String id, Product value) {
        jpaProductRepository.save(value);
        return true;
    }

    @Override
    public Product remove(String id) {
        Product p = jpaProductRepository.findById(id).orElse(null);
        if (p != null) {
            jpaProductRepository.deleteById(id);
        }
        return p;
    }

    @Override
    public Product get(String id) {
        return jpaProductRepository.findById(id).orElse(null);
    }

    @Override
    public Product update(String id, Product value) {
        return jpaProductRepository.save(value);
    }

    @Override
    public void deleteAll() {
        jpaProductRepository.deleteAll();
    }
    
}
