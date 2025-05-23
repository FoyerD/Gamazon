package Domain.Store.Discounts;

import java.util.Map;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

// This class represents a discount which is bounded by the maximum of multiple discounts.
// According to the fifth type of complext discount described in v2 ducument.

public class MaxDiscount extends Discount {

    public MaxDiscount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier) {
        super(itemFacade, discountPercentage, qualifier);
        //TODO Auto-generated constructor stub
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculatePrice'");
    }
    
    
}
