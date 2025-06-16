package Infrastructure.JpaSpringRepositories;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Repos.IStoreRepository;
import Domain.Store.Store;

@Repository
@Profile({"prod", "dbtest"})
public class JpaStoreRepository extends IStoreRepository {

    private final IJpaStoreRepository jpaStoreRepository;

    public JpaStoreRepository(IJpaStoreRepository jpaStoreRepository) {
        this.jpaStoreRepository = jpaStoreRepository;
    }

    @Override
    public boolean add(String id, Store value) {
        jpaStoreRepository.save(value);
        return true;
    }

    @Override
    public Store remove(String id) {
        Optional<Store> store = jpaStoreRepository.findById(id);
        store.ifPresent(s -> jpaStoreRepository.deleteById(id));
        return store.orElse(null);
    }

    @Override
    public Store get(String id) {
        return jpaStoreRepository.findById(id).orElse(null);
    }

    @Override
    public Store update(String id, Store value) {
        return jpaStoreRepository.save(value); // save works for both insert/update
    }

    @Override
    public Store getStoreByName(String name) {
        return jpaStoreRepository.getStoreByName(name);
    }

    @Override
    public void deleteAll() {
        jpaStoreRepository.deleteAll();
    }
}