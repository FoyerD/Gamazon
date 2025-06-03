package Infrastructure.JpaSpringRepositories;

import Domain.Store.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IJpaPolicyRepository extends JpaRepository<Policy, String> {
    List<Policy> findByStoreId(String storeId);
}