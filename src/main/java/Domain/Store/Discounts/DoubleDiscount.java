package Domain.Store.Discounts;

import java.util.Map;
import java.util.Set;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

// this class represents the combination of two discounts both are applied.
// according to the sixth type of complext discount described in v2 ducument.

public class DoubleDiscount extends Discount{

    Set<Discount> discounts; // the set of discounts that are applied

    public DoubleDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
        super(itemFacade, 0, null);
        this.discounts = discounts;
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
