package Domain.Store;
import java.util.List;

import Domain.ILockbasedRepository;

public abstract class IAuctionRepository extends ILockbasedRepository<Auction, String> {
    public abstract List<Auction> getAllStoreAuctions(String storeId);
    public abstract List<Auction> getAllProductAuctions(String productId);
}
