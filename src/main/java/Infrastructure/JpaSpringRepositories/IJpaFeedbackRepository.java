package Infrastructure.JpaSpringRepositories;

import Domain.Store.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IJpaFeedbackRepository extends JpaRepository<Feedback, String> {
    List<Feedback> findAllByStoreId(String storeId);
    List<Feedback> findAllByProductId(String productId);
    List<Feedback> findAllByCustomerId(String customerId);
}