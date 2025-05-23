package Domain.Store.Discounts;

import java.util.Map;
import java.util.Set;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class AndDiscount extends Discount {

    private Set<Discount> discounts;

    public AndDiscount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier, Set<Discount> discounts) {
        super(itemFacade, discountPercentage, qualifier);
        this.discounts = discounts;
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculatePrice'");
    }
    
}
