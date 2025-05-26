package Domain.Store.Discounts;

import java.util.Map;
import java.util.Set;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

// This class represents a discount which is bounded by the maximum of multiple discounts.
// According to the fifth type of complext discount described in v2 ducument.

public class MaxDiscount extends CompositeDiscount {

    public MaxDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
        super(itemFacade, discounts);
        // TODO: inject composite discount condition if needed
    }

    public MaxDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        super(itemFacade, Set.of(discount1, discount2));
        // TODO: inject composite discount condition if needed
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculatePrice'");
    }

    @Override
    public boolean isQualified(String productId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isQualified'");
    }
    
    
}
