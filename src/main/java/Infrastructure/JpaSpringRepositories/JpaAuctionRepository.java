package Infrastructure.JpaSpringRepositories;

import Domain.Repos.IAuctionRepository;
import Domain.Store.Auction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("prod")
public class JpaAuctionRepository extends IAuctionRepository {

    private final IJpaAuctionRepository jpaAuctionRepository;

    public JpaAuctionRepository(IJpaAuctionRepository jpaAuctionRepository) {
        this.jpaAuctionRepository = jpaAuctionRepository;
    }

    @Override
    public List<Auction> getAllStoreAuctions(String storeId) {
        return jpaAuctionRepository.getByStoreId(storeId);
    }

    @Override
    public List<Auction> getAllProductAuctions(String productId) {
        return jpaAuctionRepository.getByProductId(productId);
    }

    @Override
    public boolean add(String id, Auction entity) {
        if (jpaAuctionRepository.existsById(id)) return false;
        jpaAuctionRepository.save(entity);
        return true;
    }

    @Override
    public Auction remove(String id) {
        Optional<Auction> found = jpaAuctionRepository.findById(id);
        found.ifPresent(auction -> jpaAuctionRepository.deleteById(id));
        return found.orElse(null);
    }

    @Override
    public Auction update(String id, Auction entity) {
        return jpaAuctionRepository.save(entity);
    }

    @Override
    public Auction get(String id) {
        return jpaAuctionRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteAll() {
        jpaAuctionRepository.deleteAll();
        this.deleteAllLocks();
    }
}
