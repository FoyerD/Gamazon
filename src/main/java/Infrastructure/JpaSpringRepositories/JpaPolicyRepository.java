package Infrastructure.JpaSpringRepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Repos.IPolicyRepository;
import Domain.Store.Policy;

@Repository
@Profile({"prod", "dbtest"})
public class JpaPolicyRepository extends IPolicyRepository {

    private final IJpaPolicyRepository jpaRepo;

    public JpaPolicyRepository(IJpaPolicyRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public boolean add(String id, Policy policy) {
        if (id == null || policy == null || jpaRepo.existsById(id)) return false;
        jpaRepo.save(policy);
        return true;
    }

    @Override
    public Policy remove(String id) {
        Optional<Policy> opt = jpaRepo.findById(id);
        opt.ifPresent(p -> jpaRepo.deleteById(id));
        return opt.orElse(null);
    }

    @Override
    public Policy get(String id) {
        return jpaRepo.findById(id).orElse(null);
    }

    @Override
    public Policy update(String id, Policy policy) {
        if (!jpaRepo.existsById(id)) return null;
        return jpaRepo.save(policy);
    }

    @Override
    public List<Policy> getAllStorePolicies(String storeId) {
        return jpaRepo.findByStoreId(storeId);
    }

    @Override
    public void deleteAll() {
        jpaRepo.deleteAll();
    }
}