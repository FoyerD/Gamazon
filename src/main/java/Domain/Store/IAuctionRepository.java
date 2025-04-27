package Domain.Store;
import java.util.List;

import Domain.IRepository;

public interface IAuctionRepository extends IRepository<Auction, String> {
    public List<Auction> getAllStoreAuctions(String storeId);
    public List<Auction> getAllProductAuctions(String productId);
}
