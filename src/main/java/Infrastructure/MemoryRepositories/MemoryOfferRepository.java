package Infrastructure.MemoryRepositories;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Repos.IOfferRepository;
import Domain.Shopping.Offer;

@Repository
@Profile({"dev", "memorytest"})
public class MemoryOfferRepository extends IOfferRepository {
    private final Map<String, Offer> offers;

    public MemoryOfferRepository() {
        super();
        this.offers = new ConcurrentHashMap<>();
    }

    @Override
    public boolean add(String id, Offer offer) {
        if (!this.isIdValid(id))
            throw new IllegalArgumentException("ID cannot be null");
        if (this.offers.containsKey(id))
            throw new IllegalArgumentException("Offer with this ID already exists");
        if (offer == null)
            throw new IllegalArgumentException("Offer cannot be null");
        if (!id.equals(offer.getId()))
            throw new IllegalArgumentException("ID does not match the offer ID");

        this.addLock(id);
        return this.offers.put(id, offer) == null;
    }

    @Override
    public Offer remove(String id) {
        if (!this.isIdValid(id))
            throw new IllegalArgumentException("ID cannot be null");

        this.removeLock(id);
        return this.offers.remove(id);
    }

    @Override
    public Offer get(String id) {
        if (!this.isIdValid(id))
            throw new IllegalArgumentException("ID cannot be null");

        return this.offers.get(id);
    }

    @Override
    public Offer update(String id, Offer offer) {
        if (!this.offers.containsKey(id))
            throw new IllegalArgumentException("Offer with this ID does not exist");
        if (!this.isIdValid(id))
            throw new IllegalArgumentException("ID cannot be null");
        if (offer == null)
            throw new IllegalArgumentException("Offer cannot be null");
        if (!id.equals(offer.getId()))
            throw new IllegalArgumentException("ID does not match the offer ID");

        return this.offers.put(id, offer);
    }

    @Override
    public void deleteAll() {
        this.offers.clear();
        this.deleteAllLocks();
    }

    @Override
    public List<Offer> getOffersOfStore(String storeId) {
        return this.offers.values().stream().filter(offer -> offer.getStoreId().equals(storeId)).toList();
    }

    @Override
    public List<Offer> getOffersOfMember(String memberId) {
        return this.offers.values().stream().filter(offer -> offer.getMemberId().equals(memberId)).toList();
    }
}
