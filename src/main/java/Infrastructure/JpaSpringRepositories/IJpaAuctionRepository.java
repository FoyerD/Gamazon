package Infrastructure.JpaSpringRepositories;

import Domain.Store.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IJpaAuctionRepository extends JpaRepository<Auction, String> {
    List<Auction> getByStoreId(String storeId);
    List<Auction> getByProductId(String productId);
}
