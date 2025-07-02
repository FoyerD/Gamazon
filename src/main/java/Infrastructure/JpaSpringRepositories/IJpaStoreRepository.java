package Infrastructure.JpaSpringRepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Domain.Store.Store;

public interface IJpaStoreRepository extends JpaRepository<Store, String> {

    @Query("SELECT s FROM Store s WHERE s.name = ?1")
    abstract public Store getStoreByName(String name);

    @Query("SELECT s FROM Store s")
    public List<Store> getAllStores();
    
}
