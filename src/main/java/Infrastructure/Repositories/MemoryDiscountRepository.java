package Infrastructure.Repositories;

import Domain.Store.Discounts.Discount;
import java.util.Map;
import java.util.Set;

public class MemoryDiscountRepository {

    private Map<String, Set<Discount>> discounts; // maps storeId to a set of discounts

}
