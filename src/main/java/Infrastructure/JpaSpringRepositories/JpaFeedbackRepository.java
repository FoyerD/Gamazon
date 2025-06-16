package Infrastructure.JpaSpringRepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Repos.IFeedbackRepository;
import Domain.Store.Feedback;

@Repository
@Profile({"prod", "dbtest"})
public class JpaFeedbackRepository extends IFeedbackRepository {

    private final IJpaFeedbackRepository jpaRepo;

    @Autowired
    public JpaFeedbackRepository(IJpaFeedbackRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Feedback remove(String id) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");
        removeLock(id);
        Optional<Feedback> existing = jpaRepo.findById(id);
        existing.ifPresent(jpaRepo::delete);
        return existing.orElse(null);
    }

    @Override
    public Feedback get(String id) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");
        return jpaRepo.findById(id).orElse(null);
    }

    @Override
    public Feedback update(String id, Feedback item) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");
        if (item == null || !id.equals(item.getFeedbackId()))
            throw new IllegalArgumentException("Invalid item or mismatched ID");
        if (!jpaRepo.existsById(id))
            throw new IllegalArgumentException("Feedback not found");
        return jpaRepo.save(item);
    }

    @Override
    public boolean add(String id, Feedback item) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");
        if (item == null || !id.equals(item.getFeedbackId()))
            throw new IllegalArgumentException("Invalid item or mismatched ID");
        if (jpaRepo.existsById(id))
            throw new IllegalArgumentException("Item with this ID already exists");
        addLock(id);
        jpaRepo.save(item);
        return true;
    }

    @Override
    public List<Feedback> getAllFeedbacksByStoreId(String storeId) {
        return jpaRepo.findAllByStoreId(storeId);
    }

    @Override
    public List<Feedback> getAllFeedbacksByProductId(String productId) {
        return jpaRepo.findAllByProductId(productId);
    }

    @Override
    public List<Feedback> getAllFeedbacksByUserId(String userId) {
        return jpaRepo.findAllByCustomerId(userId);
    }

    @Override
    public void deleteAll() {
        jpaRepo.deleteAll();
        deleteAllLocks();
    }
}
