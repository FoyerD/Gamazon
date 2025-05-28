package Infrastructure.JpaSpringRepositories;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Domain.Store.Product;

public interface IJpaProductRepository extends JpaRepository<Product, String> {

    public Product getByName(String name);
    
    @Query("SELECT p FROM Product p")
    public Set<Product> getAll();

}
