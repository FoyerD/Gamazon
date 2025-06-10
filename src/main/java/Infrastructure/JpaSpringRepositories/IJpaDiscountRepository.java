package Infrastructure.JpaSpringRepositories;

import Domain.Store.Discounts.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IJpaDiscountRepository extends JpaRepository<Discount, String> {

    Discount getDiscountById(String id);

    List<Discount> findAllByStoreId(String storeId);

    @Query("""
    SELECT d FROM Discount d
    WHERE d.storeId = :storeId
    AND d.id NOT IN (
        SELECT c.id FROM CompositeDiscount parent JOIN parent.discounts c
    )
    """)
    List<Discount> findHeadDiscountsByStoreId(@Param("storeId") String storeId);


    @Query("""
    SELECT COUNT(d) FROM Discount d
    WHERE d.id NOT IN (
        SELECT c.id FROM CompositeDiscount parent JOIN parent.discounts c
    )
    """)
    int countAllHeadDiscounts();
}
