package Infrastructure.JpaSpringRepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Store.Discounts.Discount;
import Domain.Store.Discounts.IDiscountRepository;

@Repository
@Profile({"prod", "dbtest"})
public class JpaDiscountRepository extends IDiscountRepository {

    private final IJpaDiscountRepository jpaRepo;

    public JpaDiscountRepository(IJpaDiscountRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public boolean add(String discountID, Discount discount) {
        if (discount == null || discountID == null || discountID.trim().isEmpty())
            return false;
        if (!discountID.equals(discount.getId()))
            throw new IllegalArgumentException("ID mismatch");
        jpaRepo.save(discount);
        return true;
    }

    @Override
    public Discount get(String id) {
        return jpaRepo.findById(id).orElse(null);
    }

    @Override
    public Discount remove(String id) {
        Optional<Discount> discount = jpaRepo.findById(id);
        discount.ifPresent(jpaRepo::delete);
        return discount.orElse(null);
    }

    @Override
    public boolean exists(String id) {
        return jpaRepo.existsById(id);
    }

    @Override
    public void deleteAll() {
        jpaRepo.deleteAll();
    }

    @Override
    public Discount update(String id, Discount discount) {
        if (discount == null || !id.equals(discount.getId()))
            return null;
        return jpaRepo.save(discount);
    }

    @Override
    public List<Discount> getStoreDiscounts(String storeId) {
        return jpaRepo.findHeadDiscountsByStoreId(storeId);
    }

    @Override
    public int size() {
        return jpaRepo.countAllHeadDiscounts();
    }
}
