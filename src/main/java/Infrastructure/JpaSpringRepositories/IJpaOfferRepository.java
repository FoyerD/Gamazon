package Infrastructure.JpaSpringRepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Domain.Shopping.Offer;

public interface IJpaOfferRepository extends JpaRepository<Offer, String> {
    
    /**
     * Retrieves all offers for a specific store.
     * 
     * @param storeId the ID of the store
     * @return a list of offers associated with the specified store
     */
    @Query("SELECT o FROM Offer o WHERE o.storeId = :storeId")
    public List<Offer> getOffersOfStore(@Param("storeId") String storeId);

    /**
     * Retrieves all offers made by a specific member.
     * 
     * @param memberId the ID of the member
     * @return a list of offers made by the specified member
     */
    @Query("SELECT o FROM Offer o WHERE o.memberId = :memberId")
    public List<Offer> getOffersOfMember(@Param("memberId") String memberId);
    
}
