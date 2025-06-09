package Infrastructure.JpaSpringRepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Repos.IOfferRepository;
import Domain.Shopping.Offer;

@Repository
@Profile("prod")
public class JpaOfferRepository extends IOfferRepository {

    private final IJpaOfferRepository jpaOfferRepository;

    public JpaOfferRepository(IJpaOfferRepository jpaOfferRepository) {
        this.jpaOfferRepository = jpaOfferRepository;
    }

    @Override
    public boolean add(String id, Offer value) {
        if (jpaOfferRepository.existsById(id))
            return false;
        jpaOfferRepository.save(value);
        return true;
    }

    @Override
    public Offer remove(String id) {
        Optional<Offer> existing = jpaOfferRepository.findById(id);
        existing.ifPresent(jpaOfferRepository::delete);
        return existing.orElse(null);
    }

    @Override
    public Offer get(String id) {
        return jpaOfferRepository.findById(id).orElse(null);
    }

    @Override
    public Offer update(String id, Offer value) {
        if (!jpaOfferRepository.existsById(id))
            return null;
        return jpaOfferRepository.save(value);
    }

    @Override
    public void deleteAll() {
        jpaOfferRepository.deleteAll();
    }

    @Override
    public List<Offer> getOffersOfStore(String storeId) {
        return jpaOfferRepository.getOffersOfStore(storeId);
    }

    @Override
    public List<Offer> getOffersOfMember(String memberId) {
        return jpaOfferRepository.getOffersOfMember(memberId);
    }
}
