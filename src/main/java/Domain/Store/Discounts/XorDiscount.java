package Domain.Store.Discounts;

import java.util.Map;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class XorDiscount extends Discount {

    private Discount discount1;
    private Discount discount2;

    public XorDiscount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier, Discount discount1, Discount discount2) {
        super(itemFacade, discountPercentage, qualifier);
        this.discount1 = discount1;
        this.discount2 = discount2;
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculatePrice'");
    }

}
