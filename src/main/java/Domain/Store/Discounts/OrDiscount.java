package Domain.Store.Discounts;

import java.util.Map;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class OrDiscount extends Discount {

    public OrDiscount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier) {
        super(itemFacade, discountPercentage, qualifier);
        //TODO Auto-generated constructor stub
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculatePrice'");
    }

}
