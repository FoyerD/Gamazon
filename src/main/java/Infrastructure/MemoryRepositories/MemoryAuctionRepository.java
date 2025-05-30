package Infrastructure.MemoryRepositories;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import Domain.Repos.IAuctionRepository;
import Domain.Store.Auction;

@Repository
public class MemoryAuctionRepository extends IAuctionRepository {
    private Map<String, Auction> auctions;
    
    public MemoryAuctionRepository() {
        this.auctions = new java.util.concurrent.ConcurrentHashMap<>();
    }

    @Override
    public List<Auction> getAllStoreAuctions(String storeId) {
        if (!isIdValid(storeId)) throw new IllegalArgumentException("ID cannot be null");
        
        return this.auctions.values().stream()
                .filter(auction -> auction.getStoreId().equals(storeId))
                .toList();
    }

    @Override
    public List<Auction> getAllProductAuctions(String productId) {
        if(!isIdValid(productId)) throw new IllegalArgumentException("ID cannot be null");

        return this.auctions.values().stream()
                .filter(auction -> auction.getProductId().equals(productId))
                .toList();
    }

    @Override
    public boolean add(String id, Auction entity) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");
        if (!id.equals(entity.getAuctionId())) throw new IllegalArgumentException("ID does not match the auction ID");
        if (this.auctions.containsKey(id)) throw new IllegalArgumentException("Item with this ID already exists");

        return this.auctions.put(id, entity) == null;
    }

    @Override
    public Auction remove(String id) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");
        return auctions.remove(id);
    }

    @Override
    public Auction update(String id, Auction entity) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");
        if (!id.equals(entity.getAuctionId())) throw new IllegalArgumentException("ID does not match the auction ID");
        if (!this.auctions.containsKey(id)) throw new IllegalArgumentException("Item with this ID does not exist");
        return this.auctions.put(id, entity);
    }

    @Override
    public Auction get(String id) {
        if (!isIdValid(id)) throw new IllegalArgumentException("ID cannot be null");
        return (Auction)this.auctions.get(id);
    }
    
    @Override
    public void deleteAll() {
        this.auctions.clear();
        this.deleteAllLocks();
    }
}
